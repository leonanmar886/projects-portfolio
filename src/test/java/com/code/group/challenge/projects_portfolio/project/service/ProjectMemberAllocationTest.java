package com.code.group.challenge.projects_portfolio.project.service;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import com.code.group.challenge.projects_portfolio.member.domain.MemberRole;
import com.code.group.challenge.projects_portfolio.member.service.MemberService;
import com.code.group.challenge.projects_portfolio.project.domain.Project;
import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import com.code.group.challenge.projects_portfolio.project.dto.MemberAssociationRequest;
import com.code.group.challenge.projects_portfolio.project.exception.MemberAllocationException;
import com.code.group.challenge.projects_portfolio.project.mapper.ProjectMapper;
import com.code.group.challenge.projects_portfolio.project.repository.ProjectRepository;
import com.code.group.challenge.projects_portfolio.project.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProjectMemberAllocationTest {

    private ProjectRepository projectRepository;
    private MemberService memberService;
    private ProjectMapper projectMapper;
    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        projectRepository = Mockito.mock(ProjectRepository.class);
        memberService = Mockito.mock(MemberService.class);
        projectMapper = new ProjectMapper(memberService);
        projectService = new ProjectServiceImpl(projectRepository, projectMapper, memberService);
    }

    @Test
    void successfulAllocation() {
        var project = new Project();
        project.setId(1L);
        project.setName("P");
        project.setStartDate(LocalDate.of(2025,1,1));
        project.setEstimatedEndDate(LocalDate.of(2025,6,30));
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(ProjectStatus.EM_ANALISE);
        project.setManager(new Member(1L, "M", MemberRole.GERENTE));

        var member = new Member(2L, "Ana", MemberRole.FUNCIONARIO);

        when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
        when(memberService.getById(2L)).thenReturn(member);
        when(memberService.countActiveProjectsForMember(2L)).thenReturn(0);
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0, Project.class));

        var req = new MemberAssociationRequest();
        req.setMemberId(2L);
        var resp = projectService.addMember(1L, req);
        assertTrue(resp.getMembers().stream().anyMatch(m -> m.getId().equals(2L)));
    }

    @Test
    void allocationFailsForManagerRole() {
        var project = new Project();
        project.setId(1L);
        project.setName("P");
        project.setStartDate(LocalDate.of(2025,1,1));
        project.setEstimatedEndDate(LocalDate.of(2025,6,30));
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(ProjectStatus.EM_ANALISE);
        project.setManager(new Member(1L, "M", MemberRole.GERENTE));

        var member = new Member(2L, "Carlos", MemberRole.GERENTE);

        when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
        when(memberService.getById(2L)).thenReturn(member);

        var req = new MemberAssociationRequest(); req.setMemberId(2L);
        assertThrows(RuntimeException.class, () -> projectService.addMember(1L, req));
    }

    @Test
    void allocationFailsWhenMemberHas3ActiveProjects() {
        var project = new Project();
        project.setId(1L);
        project.setName("P");
        project.setStartDate(LocalDate.of(2025,1,1));
        project.setEstimatedEndDate(LocalDate.of(2025,6,30));
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(ProjectStatus.EM_ANALISE);
        project.setManager(new Member(1L, "M", MemberRole.GERENTE));

        var member = new Member(2L, "Ana", MemberRole.FUNCIONARIO);

        when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
        when(memberService.getById(2L)).thenReturn(member);
        when(memberService.countActiveProjectsForMember(2L)).thenReturn(3);

        var req = new MemberAssociationRequest(); req.setMemberId(2L);
        assertThrows(MemberAllocationException.class, () -> projectService.addMember(1L, req));
    }

    @Test
    void allocationFailsWhenProjectHas10Members() {
        var project = new Project();
        project.setId(1L);
        project.setName("P");
        project.setStartDate(LocalDate.of(2025,1,1));
        project.setEstimatedEndDate(LocalDate.of(2025,6,30));
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(ProjectStatus.EM_ANALISE);
        project.setManager(new Member(1L, "M", MemberRole.GERENTE));

        // add 10 dummy members
        IntStream.rangeClosed(1,10).forEach(i -> project.getMembers().add(new Member((long)i, "X"+i, MemberRole.FUNCIONARIO)));

        var member = new Member(11L, "New", MemberRole.FUNCIONARIO);
        when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
        when(memberService.getById(11L)).thenReturn(member);

        var req = new MemberAssociationRequest(); req.setMemberId(11L);
        assertThrows(MemberAllocationException.class, () -> projectService.addMember(1L, req));
    }

    @Test
    void removeMemberShouldFailWhenActiveProjectWouldBecomeEmpty() {
        var project = new Project();
        project.setId(1L);
        project.setName("P");
        project.setStartDate(LocalDate.of(2025,1,1));
        project.setEstimatedEndDate(LocalDate.of(2025,6,30));
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(ProjectStatus.EM_ANDAMENTO);
        project.setManager(new Member(1L, "M", MemberRole.GERENTE));
        project.getMembers().add(new Member(2L, "Ana", MemberRole.FUNCIONARIO));

        when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));

        assertThrows(RuntimeException.class, () -> projectService.removeMember(1L, 2L));
    }

    @Test
    void removeMemberShouldSucceedWhenProjectKeepsAtLeastOneMember() {
        var project = new Project();
        project.setId(1L);
        project.setName("P");
        project.setStartDate(LocalDate.of(2025,1,1));
        project.setEstimatedEndDate(LocalDate.of(2025,6,30));
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(ProjectStatus.PLANEJADO);
        project.setManager(new Member(1L, "M", MemberRole.GERENTE));
        project.getMembers().add(new Member(2L, "Ana", MemberRole.FUNCIONARIO));
        project.getMembers().add(new Member(3L, "Bia", MemberRole.FUNCIONARIO));

        when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0, Project.class));

        var response = projectService.removeMember(1L, 2L);
        assertEquals(1, response.getMembers().size());
        assertTrue(response.getMembers().stream().anyMatch(m -> m.getId().equals(3L)));
    }
}

