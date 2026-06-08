package com.code.group.challenge.projects_portfolio.project.exception;

import com.code.group.challenge.projects_portfolio.common.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class InvalidStatusTransitionException extends ApplicationException {
    public InvalidStatusTransitionException(String message) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, message);
    }
}

