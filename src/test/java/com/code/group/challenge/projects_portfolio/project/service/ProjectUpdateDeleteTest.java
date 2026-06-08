package com.code.group.challenge.projects_portfolio.project.service;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import com.code.group.challenge.projects_portfolio.member.domain.MemberRole;
import com.code.group.challenge.projects_portfolio.member.service.MemberService;
import com.code.group.challenge.projects_portfolio.project.domain.Project;
import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectUpdateRequest;
import com.code.group.challenge.projects_portfolio.project.exception.ProjectDeletionException;
import com.code.group.challenge.projects_portfolio.project.exception.ProjectValidationException;
import com.code.group.challenge.projects_portfolio.project.mapper.ProjectMapper;
import com.code.group.challenge.projects_portfolio.project.repository.ProjectRepository;
import com.code.group.challenge.projects_portfolio.project.service.impl.DefaultProjectStatusTransitionPolicy;
import com.code.group.challenge.projects_portfolio.project.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProjectUpdateDeleteTest {

    private ProjectRepository projectRepository;
    private MemberService memberService;
    private ProjectMapper projectMapper;
    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        projectRepository = Mockito.mock(ProjectRepository.class);
        memberService = Mockito.mock(MemberService.class);
        projectMapper = new ProjectMapper(new ProjectRiskCalculator());
        projectService = new ProjectServiceImpl(projectRepository, projectMapper, memberService, new DefaultProjectStatusTransitionPolicy());
    }

    @Test
    void updateShouldApplyChanges() {
        var project = new Project();
        project.setId(1L);
        project.setName("Old");
        project.setStartDate(LocalDate.of(2025,1,1));
        project.setEstimatedEndDate(LocalDate.of(2025,6,30));
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(ProjectStatus.EM_ANALISE);
        project.setManager(new Member(1L, "M", MemberRole.GERENTE));

        when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
        when(memberService.getById(2L)).thenReturn(new Member(2L, "NewM", MemberRole.GERENTE));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0, Project.class));

        var req = new ProjectUpdateRequest();
        req.setName("NewName");
        req.setBudget(new BigDecimal("2000"));
        req.setManagerId(2L);

        var resp = projectService.update(1L, req);
        assertEquals("NewName", resp.getName());
        assertEquals(new BigDecimal("2000"), resp.getBudget());
        assertEquals(2L, resp.getManager().getId());
    }

    @Test
    void updateForbiddenForClosedOrCancelled() {
        var project = new Project();
        project.setId(1L);
        project.setName("Old");
        project.setStatus(ProjectStatus.ENCERRADO);
        when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));

        var req = new ProjectUpdateRequest(); req.setName("X");
        assertThrows(ProjectValidationException.class, () -> projectService.update(1L, req));
    }

    @Test
    void deleteForbiddenForActiveStatuses() {
        var project = new Project();
        project.setId(1L);
        project.setStatus(ProjectStatus.INICIADO);
        when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));

        assertThrows(ProjectDeletionException.class, () -> projectService.delete(1L));
    }

    @Test
    void deleteAllowedForNonActive() {
        var project = new Project();
        project.setId(2L);
        project.setStatus(ProjectStatus.EM_ANALISE);
        when(projectRepository.findById(2L)).thenReturn(java.util.Optional.of(project));

        // ensure no exception
        projectService.delete(2L);
    }
}

