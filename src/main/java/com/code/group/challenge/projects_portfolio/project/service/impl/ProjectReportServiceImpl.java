package com.code.group.challenge.projects_portfolio.project.service.impl;

import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import com.code.group.challenge.projects_portfolio.project.dto.PortfolioReportResponse;
import com.code.group.challenge.projects_portfolio.project.repository.ProjectRepository;
import com.code.group.challenge.projects_portfolio.project.service.ProjectReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectReportServiceImpl implements ProjectReportService {

    private final ProjectRepository projectRepository;

    public ProjectReportServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public PortfolioReportResponse getReport() {
        var all = projectRepository.findAll();
        var countByStatus = new java.util.HashMap<String, Long>();
        var budgetByStatus = new java.util.HashMap<String, java.math.BigDecimal>();
        java.util.List<Long> closedDurations = new java.util.ArrayList<>();
        var memberIds = new java.util.HashSet<Long>();

        for (var p : all) {
            var st = p.getStatus() == null ? "UNKNOWN" : p.getStatus().name();
            countByStatus.put(st, countByStatus.getOrDefault(st, 0L) + 1);
            var b = p.getBudget() == null ? java.math.BigDecimal.ZERO : p.getBudget();
            budgetByStatus.put(st, budgetByStatus.getOrDefault(st, java.math.BigDecimal.ZERO).add(b));
            if (p.getStatus() == ProjectStatus.ENCERRADO && p.getActualEndDate() != null && p.getStartDate() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(p.getStartDate(), p.getActualEndDate());
                closedDurations.add(days);
            }
            p.getMembers().forEach(m -> memberIds.add(m.getId()));
        }

        double avg = closedDurations.isEmpty() ? 0.0 : closedDurations.stream().mapToLong(Long::longValue).average().orElse(0.0);

        var resp = new PortfolioReportResponse();
        resp.setCountByStatus(countByStatus);
        resp.setBudgetByStatus(budgetByStatus);
        resp.setAverageClosedDurationDays(avg);
        resp.setTotalUniqueMembers((long) memberIds.size());
        return resp;
    }
}
