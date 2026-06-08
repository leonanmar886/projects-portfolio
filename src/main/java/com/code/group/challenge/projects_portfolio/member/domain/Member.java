package com.code.group.challenge.projects_portfolio.member.domain;

import com.code.group.challenge.projects_portfolio.member.exception.MemberDeletionException;
import com.code.group.challenge.projects_portfolio.member.exception.MemberRoleChangeException;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Version
    @Column(nullable = false)
    private Long version;

    public Member() {
    }

    public Member(Long id, String name, MemberRole role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

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

    public MemberRole getRole() {
        return role;
    }

    public void setRole(MemberRole role) {
        this.role = role;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public void validateRoleChangeToManager(String newRole, int activeProjects) {
        boolean changingToManager = newRole != null && !newRole.isBlank() && !role.getValue().equalsIgnoreCase(newRole) && newRole.equalsIgnoreCase("gerente");
        if (changingToManager && activeProjects > 0) {
            throw new MemberRoleChangeException("Cannot change role to gerente while member is allocated in active projects");
        }
    }

    public void validateRemovable(int managedActiveProjects, int activeProjects) {
        if (managedActiveProjects > 0 || activeProjects > 0) {
            throw new MemberDeletionException("Member is linked to active projects and cannot be removed");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

