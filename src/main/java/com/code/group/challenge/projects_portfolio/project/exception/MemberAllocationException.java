package com.code.group.challenge.projects_portfolio.project.exception;

import com.code.group.challenge.projects_portfolio.common.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class MemberAllocationException extends ApplicationException {
    public MemberAllocationException(String message) { super(HttpStatus.UNPROCESSABLE_CONTENT, message); }
}

