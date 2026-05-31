package com.code.group.challenge.projects_portfolio.project.dto;

import java.math.BigDecimal;
import java.util.Map;

public class PortfolioReportResponse {
    private Map<String, Long> countByStatus;
    private Map<String, BigDecimal> budgetByStatus;
    private Double averageClosedDurationDays;
    private Long totalUniqueMembers;

    public PortfolioReportResponse() {}

    public Map<String, Long> getCountByStatus() { return countByStatus; }
    public void setCountByStatus(Map<String, Long> countByStatus) { this.countByStatus = countByStatus; }
    public Map<String, BigDecimal> getBudgetByStatus() { return budgetByStatus; }
    public void setBudgetByStatus(Map<String, BigDecimal> budgetByStatus) { this.budgetByStatus = budgetByStatus; }
    public Double getAverageClosedDurationDays() { return averageClosedDurationDays; }
    public void setAverageClosedDurationDays(Double averageClosedDurationDays) { this.averageClosedDurationDays = averageClosedDurationDays; }
    public Long getTotalUniqueMembers() { return totalUniqueMembers; }
    public void setTotalUniqueMembers(Long totalUniqueMembers) { this.totalUniqueMembers = totalUniqueMembers; }
}

