package com.code.group.challenge.projects_portfolio.member.exception;

import com.code.group.challenge.projects_portfolio.common.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends ApplicationException {
    public MemberNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "Member not found: " + id);
    }
}

