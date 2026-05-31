package com.code.group.challenge.projects_portfolio.project.service.impl;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import com.code.group.challenge.projects_portfolio.member.domain.MemberRole;
import com.code.group.challenge.projects_portfolio.member.service.MemberService;
import com.code.group.challenge.projects_portfolio.project.domain.Project;
import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import com.code.group.challenge.projects_portfolio.project.dto.MemberAssociationRequest;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectCreateRequest;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectResponse;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectUpdateRequest;
import com.code.group.challenge.projects_portfolio.project.exception.InvalidStatusTransitionException;
import com.code.group.challenge.projects_portfolio.project.exception.MemberAllocationException;
import com.code.group.challenge.projects_portfolio.project.exception.ProjectDeletionException;
import com.code.group.challenge.projects_portfolio.project.exception.ProjectNotFoundException;
import com.code.group.challenge.projects_portfolio.project.exception.ProjectValidationException;
import com.code.group.challenge.projects_portfolio.project.mapper.ProjectMapper;
import com.code.group.challenge.projects_portfolio.project.repository.ProjectRepository;
import com.code.group.challenge.projects_portfolio.project.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final MemberService memberService;

    private final List<ProjectStatus> sequence = Arrays.asList(
            ProjectStatus.EM_ANALISE,
            ProjectStatus.ANALISE_REALIZADA,
            ProjectStatus.ANALISE_APROVADA,
            ProjectStatus.INICIADO,
            ProjectStatus.PLANEJADO,
            ProjectStatus.EM_ANDAMENTO,
            ProjectStatus.ENCERRADO
    );

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMapper projectMapper, MemberService memberService) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.memberService = memberService;
    }

    @Override
    public ProjectResponse create(ProjectCreateRequest req) {
        // validation: startDate < estimatedEndDate, budget > 0
        if (req.getStartDate() != null && req.getEstimatedEndDate() != null && !req.getStartDate().isBefore(req.getEstimatedEndDate())) {
            throw new ProjectValidationException("startDate must be before estimatedEndDate");
        }
        if (req.getBudget() == null || req.getBudget().doubleValue() <= 0) {
            throw new ProjectValidationException("budget must be greater than 0");
        }
        Project p = projectMapper.toEntity(req);
        var saved = projectRepository.save(p);
        return projectMapper.toResponse(saved);
    }

    @Override
    public ProjectResponse getById(Long id) {
        var p = projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
        return projectMapper.toResponse(p);
    }

    @Override
    public Page<ProjectResponse> list(Pageable pageable, ProjectStatus status, Long managerId, String riskLevel, String name) {
        // Build JPA Specification based on status/manager/name so DB filters while riskLevel is applied in memory
        org.springframework.data.jpa.domain.Specification<Project> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> preds = new java.util.ArrayList<>();
            if (status != null) preds.add(cb.equal(root.get("status"), status));
            if (managerId != null) preds.add(cb.equal(root.get("manager").get("id"), managerId));
            if (name != null && !name.isBlank()) preds.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            return preds.isEmpty() ? null : cb.and(preds.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        if (riskLevel == null || riskLevel.isBlank()) {
            var page = projectRepository.findAll(spec, pageable);
            var content = page.stream().map(projectMapper::toResponse).collect(Collectors.toList());
            return new PageImpl<ProjectResponse>(content, pageable, page.getTotalElements());
        }

        // riskLevel provided: fetch all matching projects, compute risk in memory, then paginate
        var all = projectRepository.findAll(spec);
        var mapped = all.stream().map(projectMapper::toResponse).filter(r -> r.getRiskLevel() != null && r.getRiskLevel().equalsIgnoreCase(riskLevel)).collect(Collectors.toList());
        int total = mapped.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        java.util.List<ProjectResponse> pageContent = start <= end ? mapped.subList(start, end) : java.util.Collections.emptyList();
        return new PageImpl<ProjectResponse>(pageContent, pageable, total);
    }

    @Override
    public ProjectResponse changeStatus(Long id, ProjectStatus newStatus) {
        var p = projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
        var current = p.getStatus();
        if (newStatus == ProjectStatus.CANCELADO) {
            if (current == ProjectStatus.ENCERRADO || current == ProjectStatus.CANCELADO) {
                throw new InvalidStatusTransitionException("Invalid status transition from " + current + " to " + newStatus);
            }
            p.setStatus(ProjectStatus.CANCELADO);
            var saved = projectRepository.save(p);
            return projectMapper.toResponse(saved);
        }
        int idx = sequence.indexOf(current);
        if (idx == -1) throw new InvalidStatusTransitionException("Current status is invalid: " + current);
        int nextIdx = idx + 1;
        if (nextIdx >= sequence.size() || sequence.get(nextIdx) != newStatus) {
            throw new InvalidStatusTransitionException("Invalid status transition from " + current + " to " + newStatus);
        }
        p.setStatus(newStatus);
        if (newStatus == ProjectStatus.ENCERRADO) {
            if (p.getActualEndDate() == null) p.setActualEndDate(LocalDate.now());
        }
        var saved = projectRepository.save(p);
        return projectMapper.toResponse(saved);
    }

    @Override
    public ProjectResponse addMember(Long projectId, MemberAssociationRequest req) {
        var p = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
        var member = memberService.getById(req.getMemberId());
        if (member.getRole() != MemberRole.FUNCIONARIO) {
            throw new ProjectValidationException("Only members with role FUNCIONARIO can be allocated");
        }
        if (p.getMembers().size() >= 10) {
            throw new MemberAllocationException("Project already has maximum of 10 members");
        }
        int activeCount = memberService.countActiveProjectsForMember(member.getId());
        if (activeCount >= 3) {
            throw new MemberAllocationException("Member is already allocated in 3 active projects");
        }
        if (p.getMembers().stream().anyMatch(m -> m.getId().equals(member.getId()))) {
            throw new MemberAllocationException("Member already allocated in this project");
        }
        p.getMembers().add(member);
        var saved = projectRepository.save(p);
        return projectMapper.toResponse(saved);
    }

    @Override
    public ProjectResponse removeMember(Long projectId, Long memberId) {
        var p = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
        var removed = p.getMembers().removeIf(m -> m.getId().equals(memberId));
        if (!removed) return projectMapper.toResponse(p);
        // If project in INICIADO, PLANEJADO, EM_ANDAMENTO it must have at least 1 member after removal
        if (p.getStatus() == ProjectStatus.INICIADO || p.getStatus() == ProjectStatus.PLANEJADO || p.getStatus() == ProjectStatus.EM_ANDAMENTO) {
            if (p.getMembers().isEmpty()) {
                throw new ProjectValidationException("Project must have at least one member in active status");
            }
        }
        var saved = projectRepository.save(p);
        return projectMapper.toResponse(saved);
    }

    @Override
    public ProjectResponse update(Long id, ProjectUpdateRequest req) {
        var p = projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
        if (p.getStatus() == ProjectStatus.ENCERRADO || p.getStatus() == ProjectStatus.CANCELADO) {
            throw new ProjectValidationException("Cannot update a project that is ENCERRADO or CANCELADO");
        }
        // validate dates and budget
        if (req.getStartDate() != null && req.getEstimatedEndDate() != null && !req.getStartDate().isBefore(req.getEstimatedEndDate())) {
            throw new ProjectValidationException("startDate must be before estimatedEndDate");
        }
        if (req.getBudget() != null && req.getBudget().doubleValue() <= 0) {
            throw new ProjectValidationException("budget must be greater than 0");
        }
        if (req.getManagerId() != null) {
            // ensure manager exists
            memberService.getById(req.getManagerId());
        }
        projectMapper.updateEntity(req, p);
        var updated = projectRepository.save(p);
        return projectMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        var p = projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
        if (p.getStatus() == ProjectStatus.INICIADO || p.getStatus() == ProjectStatus.EM_ANDAMENTO || p.getStatus() == ProjectStatus.ENCERRADO) {
            throw new ProjectDeletionException("Cannot delete project with status INICIADO, EM_ANDAMENTO or ENCERRADO");
        }
        projectRepository.delete(p);
    }

    @Override
    public com.code.group.challenge.projects_portfolio.project.dto.PortfolioReportResponse getReport() {
        var all = projectRepository.findAll();
        var countByStatus = new java.util.HashMap<String, Long>();
        var budgetByStatus = new java.util.HashMap<String, java.math.BigDecimal>();
        java.util.List<Long> closedDurations = new java.util.ArrayList<>();
        var memberIds = new java.util.HashSet<Long>();

        for (var p : all) {
            var st = p.getStatus() == null ? "UNKNOWN" : p.getStatus().name();
            countByStatus.put(st, countByStatus.getOrDefault(st, 0L) + 1);
            var b = p.getBudget() == null ? java.math.BigDecimal.ZERO : p.getBudget();
            budgetByStatus.put(st, budgetByStatus.getOrDefault(st, java.math.BigDecimal.ZERO).add(b));
            if (p.getStatus() == ProjectStatus.ENCERRADO && p.getActualEndDate() != null && p.getStartDate() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(p.getStartDate(), p.getActualEndDate());
                closedDurations.add(days);
            }
            p.getMembers().forEach(m -> memberIds.add(m.getId()));
        }

        double avg = closedDurations.isEmpty() ? 0.0 : closedDurations.stream().mapToLong(Long::longValue).average().orElse(0.0);

        var resp = new com.code.group.challenge.projects_portfolio.project.dto.PortfolioReportResponse();
        resp.setCountByStatus(countByStatus);
        resp.setBudgetByStatus(budgetByStatus);
        resp.setAverageClosedDurationDays(avg);
        resp.setTotalUniqueMembers((long) memberIds.size());
        return resp;
    }
}
