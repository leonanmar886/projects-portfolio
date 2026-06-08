package com.code.group.challenge.projects_portfolio.project.service;

import com.code.group.challenge.projects_portfolio.project.domain.Project;
import com.code.group.challenge.projects_portfolio.project.domain.RiskLevel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Component
public class ProjectRiskCalculator {

    public String calculate(Project project) {
        if (project.getBudget() == null || project.getStartDate() == null || project.getEstimatedEndDate() == null) {
            return null;
        }
        long months = ChronoUnit.MONTHS.between(project.getStartDate(), project.getEstimatedEndDate());
        BigDecimal budget = project.getBudget();
        if (budget.compareTo(new BigDecimal("100000")) <= 0 && months <= 3) {
            return RiskLevel.BAIXO.name();
        }
        if ((budget.compareTo(new BigDecimal("100001")) >= 0 && budget.compareTo(new BigDecimal("500000")) <= 0)
                || (months > 3 && months <= 6)) {
            return RiskLevel.MEDIO.name();
        }
        if (budget.compareTo(new BigDecimal("500000")) > 0 || months > 6) {
            return RiskLevel.ALTO.name();
        }
        return RiskLevel.MEDIO.name();
    }
}
