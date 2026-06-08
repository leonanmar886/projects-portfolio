package com.code.group.challenge.projects_portfolio.project.exception;

import com.code.group.challenge.projects_portfolio.common.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class ProjectDeletionException extends ApplicationException {
    public ProjectDeletionException(String message) { super(HttpStatus.CONFLICT, message); }
}

