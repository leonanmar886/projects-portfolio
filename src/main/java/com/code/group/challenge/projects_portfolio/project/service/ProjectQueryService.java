package com.code.group.challenge.projects_portfolio.project.service;

import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectQueryService {
    ProjectResponse getById(Long id);
    Page<ProjectResponse> list(Pageable pageable, ProjectStatus status, Long managerId, String riskLevel, String name);
}
