package com.code.group.challenge.projects_portfolio.project.service;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import com.code.group.challenge.projects_portfolio.member.domain.MemberRole;
import com.code.group.challenge.projects_portfolio.member.service.MemberService;
import com.code.group.challenge.projects_portfolio.project.domain.Project;
import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import com.code.group.challenge.projects_portfolio.project.exception.InvalidStatusTransitionException;
import com.code.group.challenge.projects_portfolio.project.mapper.ProjectMapper;
import com.code.group.challenge.projects_portfolio.project.repository.ProjectRepository;
import com.code.group.challenge.projects_portfolio.project.service.impl.DefaultProjectStatusTransitionPolicy;
import com.code.group.challenge.projects_portfolio.project.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProjectStatusTransitionTest {

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
    void validTransitionShouldSucceed() {
        var project = new Project();
        project.setId(1L);
        project.setName("P");
        project.setStartDate(LocalDate.of(2025,1,1));
        project.setEstimatedEndDate(LocalDate.of(2025,6,30));
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(ProjectStatus.EM_ANALISE);
        project.setManager(new Member(1L, "M", MemberRole.GERENTE));

        when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0, Project.class));

        var resp = projectService.changeStatus(1L, ProjectStatus.ANALISE_REALIZADA);
        assertEquals("ANALISE_REALIZADA", resp.getStatus());
    }

    @Test
    void invalidTransitionShouldThrow() {
        var project = new Project();
        project.setId(2L);
        project.setName("P2");
        project.setStartDate(LocalDate.of(2025,1,1));
        project.setEstimatedEndDate(LocalDate.of(2025,6,30));
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(ProjectStatus.EM_ANALISE);
        project.setManager(new Member(1L, "M", MemberRole.GERENTE));

        when(projectRepository.findById(2L)).thenReturn(java.util.Optional.of(project));

        assertThrows(InvalidStatusTransitionException.class, () -> {
            projectService.changeStatus(2L, ProjectStatus.INICIADO);
        });
    }

    @Test
    void canceladoFromEncerradoShouldThrow() {
        var project = new Project();
        project.setId(6L);
        project.setName("P6");
        project.setStartDate(LocalDate.of(2025,1,1));
        project.setEstimatedEndDate(LocalDate.of(2025,6,30));
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(ProjectStatus.ENCERRADO);
        project.setManager(new Member(1L, "M", MemberRole.GERENTE));

        when(projectRepository.findById(6L)).thenReturn(java.util.Optional.of(project));

        assertThrows(InvalidStatusTransitionException.class, () -> projectService.changeStatus(6L, ProjectStatus.CANCELADO));
    }

    @Test
    void canceladoFromCanceladoShouldThrow() {
        var project = new Project();
        project.setId(7L);
        project.setName("P7");
        project.setStartDate(LocalDate.of(2025,1,1));
        project.setEstimatedEndDate(LocalDate.of(2025,6,30));
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(ProjectStatus.CANCELADO);
        project.setManager(new Member(1L, "M", MemberRole.GERENTE));

        when(projectRepository.findById(7L)).thenReturn(java.util.Optional.of(project));

        assertThrows(InvalidStatusTransitionException.class, () -> projectService.changeStatus(7L, ProjectStatus.CANCELADO));
    }

    @Test
    void canceladoShouldBeAllowedFromAllOpenStatuses() {
        var openStatuses = java.util.List.of(
                ProjectStatus.EM_ANALISE,
                ProjectStatus.ANALISE_REALIZADA,
                ProjectStatus.ANALISE_APROVADA,
                ProjectStatus.INICIADO,
                ProjectStatus.PLANEJADO,
                ProjectStatus.EM_ANDAMENTO
        );

        for (int i = 0; i < openStatuses.size(); i++) {
            long id = 100L + i;
            var project = new Project();
            project.setId(id);
            project.setName("PX" + i);
            project.setStartDate(LocalDate.of(2025,1,1));
            project.setEstimatedEndDate(LocalDate.of(2025,6,30));
            project.setBudget(new BigDecimal("1000"));
            project.setStatus(openStatuses.get(i));
            project.setManager(new Member(1L, "M", MemberRole.GERENTE));

            when(projectRepository.findById(id)).thenReturn(java.util.Optional.of(project));
            when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0, Project.class));

            var resp = projectService.changeStatus(id, ProjectStatus.CANCELADO);
            assertEquals("CANCELADO", resp.getStatus());
        }
    }

    @Test
    void canceladoShouldBeAllowedFromIntermediateStatus() {
        var project = new Project();
        project.setId(3L);
        project.setName("P3");
        project.setStartDate(LocalDate.of(2025,1,1));
        project.setEstimatedEndDate(LocalDate.of(2025,6,30));
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(ProjectStatus.PLANEJADO);
        project.setManager(new Member(1L, "M", MemberRole.GERENTE));

        when(projectRepository.findById(3L)).thenReturn(java.util.Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0, Project.class));

        var resp = projectService.changeStatus(3L, ProjectStatus.CANCELADO);
        assertEquals("CANCELADO", resp.getStatus());
    }

    @Test
    void whenClosingProjectWithoutActualEndDateShouldFillCurrentDate() {
        var project = new Project();
        project.setId(4L);
        project.setName("P4");
        project.setStartDate(LocalDate.of(2025,1,1));
        project.setEstimatedEndDate(LocalDate.of(2025,6,30));
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(ProjectStatus.EM_ANDAMENTO);
        project.setActualEndDate(null);
        project.setManager(new Member(1L, "M", MemberRole.GERENTE));

        when(projectRepository.findById(4L)).thenReturn(java.util.Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0, Project.class));

        var before = LocalDate.now();
        var resp = projectService.changeStatus(4L, ProjectStatus.ENCERRADO);
        var after = LocalDate.now();

        assertEquals("ENCERRADO", resp.getStatus());
        assertNotNull(resp.getActualEndDate());
        assertFalse(resp.getActualEndDate().isBefore(before));
        assertFalse(resp.getActualEndDate().isAfter(after));
    }

    @Test
    void whenClosingProjectWithActualEndDateShouldKeepExistingDate() {
        var existingEndDate = LocalDate.of(2025, 7, 1);
        var project = new Project();
        project.setId(5L);
        project.setName("P5");
        project.setStartDate(LocalDate.of(2025,1,1));
        project.setEstimatedEndDate(LocalDate.of(2025,6,30));
        project.setBudget(new BigDecimal("1000"));
        project.setStatus(ProjectStatus.EM_ANDAMENTO);
        project.setActualEndDate(existingEndDate);
        project.setManager(new Member(1L, "M", MemberRole.GERENTE));

        when(projectRepository.findById(5L)).thenReturn(java.util.Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0, Project.class));

        var resp = projectService.changeStatus(5L, ProjectStatus.ENCERRADO);
        assertEquals(existingEndDate, resp.getActualEndDate());
    }
}
