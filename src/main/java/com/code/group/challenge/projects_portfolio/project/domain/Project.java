package com.code.group.challenge.projects_portfolio.project.domain;

import com.code.group.challenge.projects_portfolio.member.domain.Member;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import com.code.group.challenge.projects_portfolio.project.exception.MemberAllocationException;
import com.code.group.challenge.projects_portfolio.project.exception.ProjectDeletionException;
import com.code.group.challenge.projects_portfolio.project.exception.ProjectValidationException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "estimated_end_date", nullable = false)
    private LocalDate estimatedEndDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(nullable = false)
    private BigDecimal budget;

    @Column(length = 2000)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private Member manager;

    @Version
    @Column(nullable = false)
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @ManyToMany
    @JoinTable(name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<Member> members = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public Project() {
    }

    // Getters and setters omitted for brevity

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEstimatedEndDate() {
        return estimatedEndDate;
    }

    public void setEstimatedEndDate(LocalDate estimatedEndDate) {
        this.estimatedEndDate = estimatedEndDate;
    }

    public LocalDate getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(LocalDate actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Member getManager() {
        return manager;
    }

    public void setManager(Member manager) {
        this.manager = manager;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public Set<Member> getMembers() {
        return members;
    }

    public void setMembers(Set<Member> members) {
        this.members = members;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void validateDatesAndBudget(LocalDate start, LocalDate estimatedEnd, BigDecimal budgetValue) {
        if (start != null && estimatedEnd != null && !start.isBefore(estimatedEnd)) {
            throw new ProjectValidationException("startDate must be before estimatedEndDate");
        }
        if (budgetValue == null || budgetValue.doubleValue() <= 0) {
            throw new ProjectValidationException("budget must be greater than 0");
        }
    }

    public void validateUpdatable() {
        if (status == ProjectStatus.ENCERRADO || status == ProjectStatus.CANCELADO) {
            throw new ProjectValidationException("Cannot update a project that is ENCERRADO or CANCELADO");
        }
    }

    public void validateUpdateDatesAndBudget(LocalDate start, LocalDate estimatedEnd, BigDecimal budgetValue) {
        if (start != null && estimatedEnd != null && !start.isBefore(estimatedEnd)) {
            throw new ProjectValidationException("startDate must be before estimatedEndDate");
        }
        if (budgetValue != null && budgetValue.doubleValue() <= 0) {
            throw new ProjectValidationException("budget must be greater than 0");
        }
    }

    public void validateRemovable() {
        if (status == ProjectStatus.INICIADO || status == ProjectStatus.EM_ANDAMENTO || status == ProjectStatus.ENCERRADO) {
            throw new ProjectDeletionException("Cannot delete project with status INICIADO, EM_ANDAMENTO or ENCERRADO");
        }
    }

    public void validateMemberAllocation(com.code.group.challenge.projects_portfolio.member.domain.Member member, int activeProjectsCount) {
        if (member.getRole() != com.code.group.challenge.projects_portfolio.member.domain.MemberRole.FUNCIONARIO) {
            throw new ProjectValidationException("Only members with role FUNCIONARIO can be allocated");
        }
        if (members.size() >= 10) {
            throw new MemberAllocationException("Project already has maximum of 10 members");
        }
        if (activeProjectsCount >= 3) {
            throw new MemberAllocationException("Member is already allocated in 3 active projects");
        }
        if (members.stream().anyMatch(m -> m.getId().equals(member.getId()))) {
            throw new MemberAllocationException("Member already allocated in this project");
        }
    }

    public void validateNotEmptyWhenActive() {
        if ((status == ProjectStatus.INICIADO || status == ProjectStatus.PLANEJADO || status == ProjectStatus.EM_ANDAMENTO) && members.isEmpty()) {
            throw new ProjectValidationException("Project must have at least one member in active status");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

