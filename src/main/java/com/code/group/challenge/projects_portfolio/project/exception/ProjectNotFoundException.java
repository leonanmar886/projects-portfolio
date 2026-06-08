package com.code.group.challenge.projects_portfolio.project.exception;

import com.code.group.challenge.projects_portfolio.common.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class ProjectNotFoundException extends ApplicationException {
    public ProjectNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "Project not found: " + id);
    }
}

