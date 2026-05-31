package com.code.group.challenge.projects_portfolio.member.service.impl;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import com.code.group.challenge.projects_portfolio.member.exception.MemberNotFoundException;
import com.code.group.challenge.projects_portfolio.member.exception.MemberRoleChangeException;
import com.code.group.challenge.projects_portfolio.member.exception.MemberDeletionException;
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
    public Member getById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
    }

    @Override
    public boolean existsById(Long id) {
        return memberRepository.existsById(id);
    }

    @Override
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Override
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
    public int countActiveProjectsForMember(Long memberId) {
        var excluded = List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO);
        return projectRepository.countActiveProjectsForMember(memberId, excluded);
    }

    @Override
    public Member update(Long id, com.code.group.challenge.projects_portfolio.member.dto.MemberUpdateRequest req) {
        var m = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
        boolean changingToManager = req.getRole() != null && !req.getRole().isBlank() && !m.getRole().getValue().equalsIgnoreCase(req.getRole()) && req.getRole().equalsIgnoreCase("gerente");
        if (changingToManager) {
            int active = countActiveProjectsForMember(id);
            if (active > 0) throw new MemberRoleChangeException("Cannot change role to gerente while member is allocated in active projects");
        }
        if (req.getName() != null) m.setName(req.getName());
        if (req.getRole() != null) m.setRole(com.code.group.challenge.projects_portfolio.member.domain.MemberRole.fromString(req.getRole()));
        return memberRepository.save(m);
    }

    @Override
    public void delete(Long id) {
        var m = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
        var excluded = List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO);
        int managed = projectRepository.countActiveProjectsManagedBy(id, excluded);
        int active = countActiveProjectsForMember(id);
        if (managed > 0 || active > 0) throw new MemberDeletionException("Member is linked to active projects and cannot be removed");
        memberRepository.delete(m);
    }
}
