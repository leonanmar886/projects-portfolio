package com.code.group.challenge.projects_portfolio.project.mapper;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import com.code.group.challenge.projects_portfolio.member.domain.MemberRole;
import com.code.group.challenge.projects_portfolio.project.domain.Project;
import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import com.code.group.challenge.projects_portfolio.project.service.ProjectRiskCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectRiskClassificationTest {

    private ProjectMapper projectMapper;

    @BeforeEach
    void setUp() {
        projectMapper = new ProjectMapper(new ProjectRiskCalculator());
    }

    @Test
    void riskShouldBeBaixoWhenBudget80000And2Months() {
        var project = project("P1", new BigDecimal("80000"), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1));
        assertEquals("BAIXO", projectMapper.toResponse(project).getRiskLevel());
    }

    @Test
    void riskShouldBeBaixoWhenBudget80000And3Months() {
        var project = project("P2", new BigDecimal("80000"), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 4, 1));
        assertEquals("BAIXO", projectMapper.toResponse(project).getRiskLevel());
    }

    @Test
    void riskShouldBeMedioWhenBudget80000And4Months() {
        var project = project("P3", new BigDecimal("80000"), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 1));
        assertEquals("MEDIO", projectMapper.toResponse(project).getRiskLevel());
    }

    @Test
    void riskShouldBeBaixoWhenBudget100000And2Months() {
        var project = project("P4", new BigDecimal("100000"), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1));
        assertEquals("BAIXO", projectMapper.toResponse(project).getRiskLevel());
    }

    @Test
    void riskShouldBeMedioWhenBudget100001And2Months() {
        var project = project("P5", new BigDecimal("100001"), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1));
        assertEquals("MEDIO", projectMapper.toResponse(project).getRiskLevel());
    }

    @Test
    void riskShouldBeMedioWhenBudget200000And2Months() {
        var project = project("P6", new BigDecimal("200000"), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1));
        assertEquals("MEDIO", projectMapper.toResponse(project).getRiskLevel());
    }

    @Test
    void riskShouldBeAltoWhenBudget600000And2Months() {
        var project = project("P7", new BigDecimal("600000"), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1));
        assertEquals("ALTO", projectMapper.toResponse(project).getRiskLevel());
    }

    @Test
    void riskShouldBeAltoWhenBudget80000And8Months() {
        var project = project("P8", new BigDecimal("80000"), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 9, 1));
        assertEquals("ALTO", projectMapper.toResponse(project).getRiskLevel());
    }

    private Project project(String name, BigDecimal budget, LocalDate startDate, LocalDate estimatedEndDate) {
        var project = new Project();
        project.setName(name);
        project.setBudget(budget);
        project.setStartDate(startDate);
        project.setEstimatedEndDate(estimatedEndDate);
        project.setManager(new Member(1L, "Manager", MemberRole.GERENTE));
        project.setStatus(ProjectStatus.EM_ANALISE);
        return project;
    }
}
