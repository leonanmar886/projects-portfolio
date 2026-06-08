package com.code.group.challenge.projects_portfolio.project.service;

import com.code.group.challenge.projects_portfolio.project.dto.MemberAssociationRequest;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectCreateRequest;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectResponse;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectUpdateRequest;
import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;

public interface ProjectCommandService {
    ProjectResponse create(ProjectCreateRequest req);
    ProjectResponse changeStatus(Long id, ProjectStatus newStatus);
    ProjectResponse addMember(Long projectId, MemberAssociationRequest req);
    ProjectResponse removeMember(Long projectId, Long memberId);
    ProjectResponse update(Long id, ProjectUpdateRequest req);
    void delete(Long id);
}
