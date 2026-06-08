package com.code.group.challenge.projects_portfolio.project.service;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import com.code.group.challenge.projects_portfolio.member.domain.MemberRole;
import com.code.group.challenge.projects_portfolio.member.service.MemberService;
import com.code.group.challenge.projects_portfolio.project.domain.Project;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectCreateRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProjectServiceTest {

    private ProjectRepository projectRepository;
    private MemberService memberService;
    private ProjectMapper projectMapper;
    private ProjectCommandService projectService;

    @BeforeEach
    void setUp() {
        projectRepository = Mockito.mock(ProjectRepository.class);
        memberService = Mockito.mock(MemberService.class);
        projectMapper = new ProjectMapper(new ProjectRiskCalculator());
        projectService = new ProjectServiceImpl(projectRepository, projectMapper, memberService, new DefaultProjectStatusTransitionPolicy());
    }

    @Test
    void createShouldSave() {
        var req = new ProjectCreateRequest();
        req.setName("Teste");
        req.setStartDate(LocalDate.of(2025,1,1));
        req.setEstimatedEndDate(LocalDate.of(2025,6,30));
        req.setBudget(new BigDecimal("1000"));
        req.setManagerId(1L);

        when(memberService.getById(1L)).thenReturn(new Member(1L, "Manager", MemberRole.GERENTE));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
            var p = inv.getArgument(0, Project.class);
            p.setId(1L);
            return p;
        });

        ProjectResponse resp = projectService.create(req);
        assertNotNull(resp);
        assertEquals(1L, resp.getId());
        assertEquals("Teste", resp.getName());
    }
}
