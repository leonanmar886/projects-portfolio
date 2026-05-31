package com.code.group.challenge.projects_portfolio.member.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(Long id) {
        super("Member not found: " + id);
    }
}

