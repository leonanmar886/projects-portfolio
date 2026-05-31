package com.code.group.challenge.projects_portfolio.project.dto;

import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public class StatusChangeRequest {
    @NotNull
    private ProjectStatus status;

    public StatusChangeRequest() {}

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
}

