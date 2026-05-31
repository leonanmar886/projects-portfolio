package com.code.group.challenge.projects_portfolio.member.service;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import com.code.group.challenge.projects_portfolio.member.domain.MemberRole;
import com.code.group.challenge.projects_portfolio.member.dto.MemberUpdateRequest;
import com.code.group.challenge.projects_portfolio.member.exception.MemberDeletionException;
import com.code.group.challenge.projects_portfolio.member.exception.MemberNotFoundException;
import com.code.group.challenge.projects_portfolio.member.exception.MemberRoleChangeException;
import com.code.group.challenge.projects_portfolio.member.repository.MemberRepository;
import com.code.group.challenge.projects_portfolio.member.service.impl.MemberServiceImpl;
import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import com.code.group.challenge.projects_portfolio.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MemberServiceTest {

    private MemberRepository memberRepository;
    private ProjectRepository projectRepository;
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberRepository = Mockito.mock(MemberRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);
        memberService = new MemberServiceImpl(memberRepository, projectRepository);
    }

    @Test
    void createShouldSave() {
        var m = new Member(null, "Ana", MemberRole.FUNCIONARIO);
        when(memberRepository.save(any())).thenAnswer(inv -> {
            var arg = inv.getArgument(0, Member.class);
            arg.setId(1L);
            return arg;
        });

        var saved = memberService.create(m);
        assertNotNull(saved.getId());
        assertEquals("Ana", saved.getName());
    }

    @Test
    void updateToGerenteWhenAllocatedShouldThrow() {
        var existing = new Member(1L, "Ana", MemberRole.FUNCIONARIO);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.countActiveProjectsForMember(1L, List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO))).thenReturn(1);

        var req = new MemberUpdateRequest();
        req.setRole("gerente");

        assertThrows(MemberRoleChangeException.class, () -> memberService.update(1L, req));
    }

    @Test
    void updateToGerenteWhenNotAllocatedShouldSucceed() {
        var existing = new Member(1L, "Ana", MemberRole.FUNCIONARIO);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.countActiveProjectsForMember(1L, List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO))).thenReturn(0);
        when(memberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0, Member.class));

        var req = new MemberUpdateRequest();
        req.setRole("gerente");

        var updated = memberService.update(1L, req);
        assertEquals(MemberRole.GERENTE, updated.getRole());
    }

    @Test
    void deleteWhenMemberAllocatedShouldThrow() {
        var existing = new Member(1L, "Ana", MemberRole.FUNCIONARIO);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.countActiveProjectsManagedBy(1L, List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO))).thenReturn(0);
        when(projectRepository.countActiveProjectsForMember(1L, List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO))).thenReturn(1);

        assertThrows(MemberDeletionException.class, () -> memberService.delete(1L));
    }

    @Test
    void deleteWhenMemberIsActiveManagerShouldThrow() {
        var existing = new Member(1L, "Ana", MemberRole.GERENTE);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.countActiveProjectsManagedBy(1L, List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO))).thenReturn(1);
        when(projectRepository.countActiveProjectsForMember(1L, List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO))).thenReturn(0);

        assertThrows(MemberDeletionException.class, () -> memberService.delete(1L));
    }

    @Test
    void deleteWhenNoActiveLinksShouldSucceed() {
        var existing = new Member(1L, "Ana", MemberRole.FUNCIONARIO);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(projectRepository.countActiveProjectsManagedBy(1L, List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO))).thenReturn(0);
        when(projectRepository.countActiveProjectsForMember(1L, List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO))).thenReturn(0);

        assertDoesNotThrow(() -> memberService.delete(1L));
        verify(memberRepository).delete(existing);
    }
}
