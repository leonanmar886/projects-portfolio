package com.code.group.challenge.projects_portfolio.member.service;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import com.code.group.challenge.projects_portfolio.member.dto.MemberUpdateRequest;

public interface MemberCommandService {
    Member create(Member member);
    Member update(Long id, MemberUpdateRequest req);
    void delete(Long id);
}
