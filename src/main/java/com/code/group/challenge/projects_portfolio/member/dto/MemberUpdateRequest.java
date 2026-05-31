package com.code.group.challenge.projects_portfolio.member.dto;

public class MemberUpdateRequest {
    private String name;
    private String role;

    public MemberUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

