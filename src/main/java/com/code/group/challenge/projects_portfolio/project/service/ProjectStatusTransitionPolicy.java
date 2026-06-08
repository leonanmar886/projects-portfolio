package com.code.group.challenge.projects_portfolio.project.service;

import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;

public interface ProjectStatusTransitionPolicy {
    void assertCanTransition(ProjectStatus current, ProjectStatus target);
}
