package com.code.group.challenge.projects_portfolio.project.dto;

import com.code.group.challenge.projects_portfolio.member.dto.MemberResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class ProjectResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate estimatedEndDate;
    private LocalDate actualEndDate;
    private BigDecimal budget;
    private String description;
    private MemberResponse manager;
    private String status;
    private String riskLevel;
    private Set<MemberResponse> members = new HashSet<>();

    public ProjectResponse() {}

    // getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public MemberResponse getManager() { return manager; }
    public void setManager(MemberResponse manager) { this.manager = manager; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Set<MemberResponse> getMembers() { return members; }
    public void setMembers(Set<MemberResponse> members) { this.members = members; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
}
