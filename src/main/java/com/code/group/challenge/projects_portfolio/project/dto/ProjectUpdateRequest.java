package com.code.group.challenge.projects_portfolio.project.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjectUpdateRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate estimatedEndDate;
    private LocalDate actualEndDate;
    private BigDecimal budget;
    private String description;
    private Long managerId;

    public ProjectUpdateRequest() {}

    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEstimatedEndDate() { return estimatedEndDate; }
    public void setEstimatedEndDate(LocalDate estimatedEndDate) { this.estimatedEndDate = estimatedEndDate; }
    public LocalDate getActualEndDate() { return actualEndDate; }
    public void setActualEndDate(LocalDate actualEndDate) { this.actualEndDate = actualEndDate; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
}

