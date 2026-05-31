package com.code.group.challenge.projects_portfolio.project.repository;

import com.code.group.challenge.projects_portfolio.project.domain.Project;
import com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    @Query("select count(p) from Project p join p.members m where m.id = :memberId and p.status not in (:excluded)")
    int countActiveProjectsForMember(@Param("memberId") Long memberId, @Param("excluded") java.util.List<ProjectStatus> excluded);

    @Query("select count(p) from Project p where p.manager.id = :managerId and p.status not in (:excluded)")
    int countActiveProjectsManagedBy(@Param("managerId") Long managerId, @Param("excluded") java.util.List<ProjectStatus> excluded);
}
