package com.code.group.challenge.projects_portfolio.project.mapper;

import com.code.group.challenge.projects_portfolio.member.dto.MemberResponse;
import com.code.group.challenge.projects_portfolio.member.mapper.MemberMapper;
import com.code.group.challenge.projects_portfolio.member.service.MemberService;
import com.code.group.challenge.projects_portfolio.project.domain.Project;
import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import com.code.group.challenge.projects_portfolio.project.domain.RiskLevel;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectCreateRequest;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectResponse;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectUpdateRequest;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    private final MemberService memberService;

    public ProjectMapper(MemberService memberService) {
        this.memberService = memberService;
    }

    public Project toEntity(ProjectCreateRequest req) {
        Project p = new Project();
        p.setName(req.getName());
        p.setStartDate(req.getStartDate());
        p.setEstimatedEndDate(req.getEstimatedEndDate());
        p.setBudget(req.getBudget());
        p.setDescription(req.getDescription());
        p.setStatus(ProjectStatus.EM_ANALISE);
        var manager = memberService.getById(req.getManagerId());
        p.setManager(manager);
        return p;
    }

    public ProjectResponse toResponse(Project p) {
        if (p == null) return null;
        ProjectResponse r = new ProjectResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setStartDate(p.getStartDate());
        r.setEstimatedEndDate(p.getEstimatedEndDate());
        r.setActualEndDate(p.getActualEndDate());
        r.setBudget(p.getBudget());
        r.setDescription(p.getDescription());
        var mgr = p.getManager();
        r.setManager(mgr == null ? null : new MemberResponse(mgr.getId(), mgr.getName(), mgr.getRole().getValue()));
        r.setStatus(p.getStatus().name());
        r.setMembers(p.getMembers().stream().map(m -> new MemberResponse(m.getId(), m.getName(), m.getRole().getValue())).collect(Collectors.toSet()));
        // compute risk
        r.setRiskLevel(computeRisk(p));
        return r;
    }

    public void updateEntity(ProjectUpdateRequest req, Project p) {
        if (req.getName() != null) p.setName(req.getName());
        if (req.getStartDate() != null) p.setStartDate(req.getStartDate());
        if (req.getEstimatedEndDate() != null) p.setEstimatedEndDate(req.getEstimatedEndDate());
        if (req.getActualEndDate() != null) p.setActualEndDate(req.getActualEndDate());
        if (req.getBudget() != null) p.setBudget(req.getBudget());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getManagerId() != null) {
            var mgr = memberService.getById(req.getManagerId());
            p.setManager(mgr);
        }
        // do not touch status or members here
    }

    private String computeRisk(Project p) {
        if (p.getBudget() == null || p.getStartDate() == null || p.getEstimatedEndDate() == null) return null;
        long months = ChronoUnit.MONTHS.between(p.getStartDate(), p.getEstimatedEndDate());
        // edge conditions: exactly 3 months is BAIXO, exactly 100000 is BAIXO
        var budget = p.getBudget();
        if (budget.compareTo(new java.math.BigDecimal("100000")) <= 0 && months <= 3) return RiskLevel.BAIXO.name();
        if ((budget.compareTo(new java.math.BigDecimal("100001")) >= 0 && budget.compareTo(new java.math.BigDecimal("500000")) <= 0)
                || (months > 3 && months <= 6)) return RiskLevel.MEDIO.name();
        if (budget.compareTo(new java.math.BigDecimal("500000")) > 0 || months > 6) return RiskLevel.ALTO.name();
        // fallback
        return RiskLevel.MEDIO.name();
    }
}
