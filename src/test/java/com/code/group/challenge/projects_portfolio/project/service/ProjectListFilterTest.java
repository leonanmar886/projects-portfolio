package com.code.group.challenge.projects_portfolio.project.service;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import com.code.group.challenge.projects_portfolio.member.domain.MemberRole;
import com.code.group.challenge.projects_portfolio.member.service.MemberService;
import com.code.group.challenge.projects_portfolio.project.domain.Project;
import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectResponse;
import com.code.group.challenge.projects_portfolio.project.mapper.ProjectMapper;
import com.code.group.challenge.projects_portfolio.project.repository.ProjectRepository;
import com.code.group.challenge.projects_portfolio.project.service.impl.DefaultProjectStatusTransitionPolicy;
import com.code.group.challenge.projects_portfolio.project.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProjectListFilterTest {

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
    void filterByStatusAndName() {
        var p1 = new Project(); p1.setId(1L); p1.setName("Alpha"); p1.setStatus(ProjectStatus.EM_ANALISE);
        when(projectRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(p1)));

        var page = projectService.list(org.springframework.data.domain.PageRequest.of(0,10), ProjectStatus.EM_ANALISE, null, null, "Alp");
        assertEquals(1, page.getTotalElements());
        ProjectResponse r = page.getContent().get(0);
        assertEquals(1L, r.getId());
    }

    @Test
    void riskShouldBeBaixoAtExactThresholds() {
        var mgr = new Member(5L, "M", MemberRole.GERENTE);
        var p = new Project();
        p.setId(4L);
        p.setName("BaixoThreshold");
        p.setStatus(ProjectStatus.EM_ANALISE);
        p.setManager(mgr);
        p.setStartDate(LocalDate.of(2025,1,1));
        p.setEstimatedEndDate(LocalDate.of(2025,4,1));
        p.setBudget(new BigDecimal("100000"));

        when(projectRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(p)));

        var page = projectService.list(org.springframework.data.domain.PageRequest.of(0,10), null, null, null, null);
        assertEquals("BAIXO", page.getContent().get(0).getRiskLevel());
    }

    @Test
    void riskShouldBeMedioWhenBudgetInMidRange() {
        var mgr = new Member(5L, "M", MemberRole.GERENTE);
        var p = new Project();
        p.setId(5L);
        p.setName("MedioBudget");
        p.setStatus(ProjectStatus.EM_ANALISE);
        p.setManager(mgr);
        p.setStartDate(LocalDate.of(2025,1,1));
        p.setEstimatedEndDate(LocalDate.of(2025,3,1));
        p.setBudget(new BigDecimal("100001"));

        when(projectRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(p)));

        var page = projectService.list(org.springframework.data.domain.PageRequest.of(0,10), null, null, null, null);
        assertEquals("MEDIO", page.getContent().get(0).getRiskLevel());
    }

    @Test
    void riskShouldBeAltoWhenBudgetAboveThreshold() {
        var mgr = new Member(5L, "M", MemberRole.GERENTE);
        var p = new Project();
        p.setId(6L);
        p.setName("AltoBudget");
        p.setStatus(ProjectStatus.EM_ANALISE);
        p.setManager(mgr);
        p.setStartDate(LocalDate.of(2025,1,1));
        p.setEstimatedEndDate(LocalDate.of(2025,3,1));
        p.setBudget(new BigDecimal("500001"));

        when(projectRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(p)));

        var page = projectService.list(org.springframework.data.domain.PageRequest.of(0,10), null, null, null, null);
        assertEquals("ALTO", page.getContent().get(0).getRiskLevel());
    }
}

