package com.code.group.challenge.projects_portfolio.project.service.impl;

import com.code.group.challenge.projects_portfolio.member.service.MemberService;
import com.code.group.challenge.projects_portfolio.project.domain.Project;
import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import com.code.group.challenge.projects_portfolio.project.dto.MemberAssociationRequest;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectCreateRequest;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectResponse;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectUpdateRequest;
import com.code.group.challenge.projects_portfolio.project.exception.ProjectNotFoundException;
import com.code.group.challenge.projects_portfolio.project.mapper.ProjectMapper;
import com.code.group.challenge.projects_portfolio.project.repository.ProjectRepository;
import com.code.group.challenge.projects_portfolio.project.service.ProjectCommandService;
import com.code.group.challenge.projects_portfolio.project.service.ProjectQueryService;
import com.code.group.challenge.projects_portfolio.project.service.ProjectStatusTransitionPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectCommandService, ProjectQueryService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final MemberService memberService;

    private final ProjectStatusTransitionPolicy projectStatusTransitionPolicy;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              ProjectMapper projectMapper,
                              MemberService memberService,
                              ProjectStatusTransitionPolicy projectStatusTransitionPolicy) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.memberService = memberService;
        this.projectStatusTransitionPolicy = projectStatusTransitionPolicy;
    }

    @Override
    public ProjectResponse create(ProjectCreateRequest req) {
        var manager = memberService.getById(req.getManagerId());
        Project p = projectMapper.toEntity(req, manager);
        p.validateDatesAndBudget(req.getStartDate(), req.getEstimatedEndDate(), req.getBudget());
        var saved = projectRepository.save(p);
        return projectMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getById(Long id) {
        var p = projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
        return projectMapper.toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
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
            var content = page.stream().map(projectMapper::toResponse).toList();
            return new PageImpl<ProjectResponse>(content, pageable, page.getTotalElements());
        }

        // riskLevel provided: fetch all matching projects, compute risk in memory, then paginate
        var all = projectRepository.findAll(spec);
        var mapped = all.stream().map(projectMapper::toResponse).filter(r -> r.getRiskLevel() != null && r.getRiskLevel().equalsIgnoreCase(riskLevel)).toList();
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
        projectStatusTransitionPolicy.assertCanTransition(current, newStatus);
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
        int activeCount = memberService.countActiveProjectsForMember(member.getId());
        p.validateMemberAllocation(member, activeCount);
        p.getMembers().add(member);
        var saved = projectRepository.save(p);
        return projectMapper.toResponse(saved);
    }

    @Override
    public ProjectResponse removeMember(Long projectId, Long memberId) {
        var p = projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
        var removed = p.getMembers().removeIf(m -> m.getId().equals(memberId));
        if (!removed) return projectMapper.toResponse(p);
        p.validateNotEmptyWhenActive();
        var saved = projectRepository.save(p);
        return projectMapper.toResponse(saved);
    }

    @Override
    public ProjectResponse update(Long id, ProjectUpdateRequest req) {
        var p = projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
        p.validateUpdatable();
        p.validateUpdateDatesAndBudget(req.getStartDate(), req.getEstimatedEndDate(), req.getBudget());
        com.code.group.challenge.projects_portfolio.member.domain.Member manager = null;
        if (req.getManagerId() != null) {
            // ensure manager exists
            manager = memberService.getById(req.getManagerId());
        }
        projectMapper.updateEntity(req, p, manager);
        var updated = projectRepository.save(p);
        return projectMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        var p = projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
        p.validateRemovable();
        projectRepository.delete(p);
    }

}
