package com.code.group.challenge.projects_portfolio.member.mapper;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import com.code.group.challenge.projects_portfolio.member.domain.MemberRole;
import com.code.group.challenge.projects_portfolio.member.dto.MemberCreateRequest;
import com.code.group.challenge.projects_portfolio.member.dto.MemberResponse;

public class MemberMapper {

    public static Member toEntity(MemberCreateRequest req) {
        Member m = new Member();
        m.setName(req.getName());
        m.setRole(MemberRole.fromString(req.getRole()));
        return m;
    }

    public static MemberResponse toResponse(Member m) {
        if (m == null) return null;
        return new MemberResponse(m.getId(), m.getName(), m.getRole().getValue());
    }
}

