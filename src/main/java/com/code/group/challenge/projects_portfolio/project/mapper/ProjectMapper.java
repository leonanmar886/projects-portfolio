package com.code.group.challenge.projects_portfolio.project.mapper;

import com.code.group.challenge.projects_portfolio.member.dto.MemberResponse;
import com.code.group.challenge.projects_portfolio.project.domain.Project;
import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectCreateRequest;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectResponse;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectUpdateRequest;
import com.code.group.challenge.projects_portfolio.project.service.ProjectRiskCalculator;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    private final ProjectRiskCalculator projectRiskCalculator;

    public ProjectMapper(ProjectRiskCalculator projectRiskCalculator) {
        this.projectRiskCalculator = projectRiskCalculator;
    }

    public Project toEntity(ProjectCreateRequest req, com.code.group.challenge.projects_portfolio.member.domain.Member manager) {
        Project p = new Project();
        p.setName(req.getName());
        p.setStartDate(req.getStartDate());
        p.setEstimatedEndDate(req.getEstimatedEndDate());
        p.setBudget(req.getBudget());
        p.setDescription(req.getDescription());
        p.setStatus(ProjectStatus.EM_ANALISE);
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
        r.setRiskLevel(projectRiskCalculator.calculate(p));
        return r;
    }

    public void updateEntity(ProjectUpdateRequest req, Project p, com.code.group.challenge.projects_portfolio.member.domain.Member manager) {
        if (req.getName() != null) p.setName(req.getName());
        if (req.getStartDate() != null) p.setStartDate(req.getStartDate());
        if (req.getEstimatedEndDate() != null) p.setEstimatedEndDate(req.getEstimatedEndDate());
        if (req.getActualEndDate() != null) p.setActualEndDate(req.getActualEndDate());
        if (req.getBudget() != null) p.setBudget(req.getBudget());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (manager != null) {
            p.setManager(manager);
        }
        // do not touch status or members here
    }
}
