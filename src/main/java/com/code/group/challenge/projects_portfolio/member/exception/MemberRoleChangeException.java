package com.code.group.challenge.projects_portfolio.member.exception;

import com.code.group.challenge.projects_portfolio.common.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class MemberRoleChangeException extends ApplicationException {
    public MemberRoleChangeException(String message) { super(HttpStatus.CONFLICT, message); }
}

