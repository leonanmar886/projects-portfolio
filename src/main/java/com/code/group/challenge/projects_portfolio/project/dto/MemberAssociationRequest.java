package com.code.group.challenge.projects_portfolio.project.dto;

import jakarta.validation.constraints.NotNull;

public class MemberAssociationRequest {
    @NotNull
    private Long memberId;

    public MemberAssociationRequest() {}

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
}

