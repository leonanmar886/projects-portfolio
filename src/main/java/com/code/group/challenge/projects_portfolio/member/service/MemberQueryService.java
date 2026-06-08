package com.code.group.challenge.projects_portfolio.member.service;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import com.code.group.challenge.projects_portfolio.member.domain.MemberRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberQueryService {
    Member getById(Long id);
    boolean existsById(Long id);
    List<Member> findAll();
    int countActiveProjectsForMember(Long memberId);
    Page<Member> list(Pageable pageable, MemberRole role, String name);
}
