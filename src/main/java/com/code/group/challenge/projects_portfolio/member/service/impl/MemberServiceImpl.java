package com.code.group.challenge.projects_portfolio.member.service.impl;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import com.code.group.challenge.projects_portfolio.member.exception.MemberNotFoundException;
import com.code.group.challenge.projects_portfolio.member.repository.MemberRepository;
import com.code.group.challenge.projects_portfolio.member.service.MemberService;
import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import com.code.group.challenge.projects_portfolio.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;

    public MemberServiceImpl(MemberRepository memberRepository, ProjectRepository projectRepository) {
        this.memberRepository = memberRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public Member create(Member member) {
        return memberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public Member getById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return memberRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Member> list(org.springframework.data.domain.Pageable pageable, com.code.group.challenge.projects_portfolio.member.domain.MemberRole role, String name) {
        org.springframework.data.jpa.domain.Specification<Member> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (role != null) predicates.add(cb.equal(root.get("role"), role));
            if (name != null && !name.isBlank()) predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return memberRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public int countActiveProjectsForMember(Long memberId) {
        return loadActiveProjectsForMember(memberId);
    }

    @Override
    public Member update(Long id, com.code.group.challenge.projects_portfolio.member.dto.MemberUpdateRequest req) {
        var m = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
        int active = loadActiveProjectsForMember(id);
        m.validateRoleChangeToManager(req.getRole(), active);
        if (req.getName() != null) m.setName(req.getName());
        if (req.getRole() != null) m.setRole(com.code.group.challenge.projects_portfolio.member.domain.MemberRole.fromString(req.getRole()));
        return memberRepository.save(m);
    }

    @Override
    public void delete(Long id) {
        var m = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
        var excluded = List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO);
        int managed = projectRepository.countActiveProjectsManagedBy(id, excluded);
        int active = loadActiveProjectsForMember(id);
        m.validateRemovable(managed, active);
        memberRepository.delete(m);
    }

    private int loadActiveProjectsForMember(Long memberId) {
        var excluded = List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO);
        return projectRepository.countActiveProjectsForMember(memberId, excluded);
    }
}
