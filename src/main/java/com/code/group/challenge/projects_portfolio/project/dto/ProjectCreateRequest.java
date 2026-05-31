package com.code.group.challenge.projects_portfolio.project.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjectCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate estimatedEndDate;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal budget;

    private String description;

    @NotNull
    private Long managerId;

    public ProjectCreateRequest() {}

    // getters and setters

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEstimatedEndDate() { return estimatedEndDate; }
    public void setEstimatedEndDate(LocalDate estimatedEndDate) { this.estimatedEndDate = estimatedEndDate; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
}

