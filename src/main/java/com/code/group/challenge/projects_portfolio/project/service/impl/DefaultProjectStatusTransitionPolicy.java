package com.code.group.challenge.projects_portfolio.project.service.impl;

import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import com.code.group.challenge.projects_portfolio.project.exception.InvalidStatusTransitionException;
import com.code.group.challenge.projects_portfolio.project.service.ProjectStatusTransitionPolicy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DefaultProjectStatusTransitionPolicy implements ProjectStatusTransitionPolicy {

    private final List<ProjectStatus> sequence = Arrays.asList(
            ProjectStatus.EM_ANALISE,
            ProjectStatus.ANALISE_REALIZADA,
            ProjectStatus.ANALISE_APROVADA,
            ProjectStatus.INICIADO,
            ProjectStatus.PLANEJADO,
            ProjectStatus.EM_ANDAMENTO,
            ProjectStatus.ENCERRADO
    );

    @Override
    public void assertCanTransition(ProjectStatus current, ProjectStatus target) {
        if (target == ProjectStatus.CANCELADO) {
            if (current == ProjectStatus.ENCERRADO || current == ProjectStatus.CANCELADO) {
                throw new InvalidStatusTransitionException("Invalid status transition from " + current + " to " + target);
            }
            return;
        }
        int idx = sequence.indexOf(current);
        if (idx == -1) {
            throw new InvalidStatusTransitionException("Current status is invalid: " + current);
        }
        int nextIdx = idx + 1;
        if (nextIdx >= sequence.size() || sequence.get(nextIdx) != target) {
            throw new InvalidStatusTransitionException("Invalid status transition from " + current + " to " + target);
        }
    }
}
