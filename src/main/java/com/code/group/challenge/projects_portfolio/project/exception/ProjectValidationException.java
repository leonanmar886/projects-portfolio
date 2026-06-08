package com.code.group.challenge.projects_portfolio.project.exception;

import com.code.group.challenge.projects_portfolio.common.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class ProjectValidationException extends ApplicationException {
    public ProjectValidationException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}

