package com.code.group.challenge.projects_portfolio.member.service;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberService {
    Member create(Member member);
    Member getById(Long id);
    boolean existsById(Long id);
    List<Member> findAll();
    int countActiveProjectsForMember(Long memberId);
    Member update(Long id, com.code.group.challenge.projects_portfolio.member.dto.MemberUpdateRequest req);
    void delete(Long id);
    Page<Member> list(Pageable pageable, com.code.group.challenge.projects_portfolio.member.domain.MemberRole role, String name);
}
