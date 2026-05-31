package com.code.group.challenge.projects_portfolio.project.service;

import com.code.group.challenge.projects_portfolio.project.dto.MemberAssociationRequest;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectCreateRequest;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectResponse;
import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectResponse create(ProjectCreateRequest req);
    ProjectResponse getById(Long id);
    Page<ProjectResponse> list(Pageable pageable, ProjectStatus status, Long managerId, String riskLevel, String name);
    ProjectResponse changeStatus(Long id, ProjectStatus newStatus);
    ProjectResponse addMember(Long projectId, MemberAssociationRequest req);
    ProjectResponse removeMember(Long projectId, Long memberId);
    ProjectResponse update(Long id, com.code.group.challenge.projects_portfolio.project.dto.ProjectUpdateRequest req);
    void delete(Long id);
    com.code.group.challenge.projects_portfolio.project.dto.PortfolioReportResponse getReport();
}
