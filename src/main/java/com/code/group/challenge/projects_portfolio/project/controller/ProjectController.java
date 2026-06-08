package com.code.group.challenge.projects_portfolio.project.controller;

import com.code.group.challenge.projects_portfolio.project.dto.MemberAssociationRequest;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectCreateRequest;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectResponse;
import com.code.group.challenge.projects_portfolio.project.dto.ProjectUpdateRequest;
import com.code.group.challenge.projects_portfolio.project.dto.StatusChangeRequest;
import com.code.group.challenge.projects_portfolio.project.service.ProjectCommandService;
import com.code.group.challenge.projects_portfolio.project.service.ProjectQueryService;
import com.code.group.challenge.projects_portfolio.project.service.ProjectReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectCommandService projectCommandService;
    private final ProjectQueryService projectQueryService;
    private final ProjectReportService projectReportService;

    public ProjectController(ProjectCommandService projectCommandService,
                             ProjectQueryService projectQueryService,
                             ProjectReportService projectReportService) {
        this.projectCommandService = projectCommandService;
        this.projectQueryService = projectQueryService;
        this.projectReportService = projectReportService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectCreateRequest req) {
        var resp = projectCommandService.create(req);
        return ResponseEntity.created(URI.create("/api/projects/" + resp.getId())).body(resp);
    }

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(required = false) com.code.group.challenge.projects_portfolio.project.domain.ProjectStatus status,
                                                       @RequestParam(required = false) Long managerId,
                                                       @RequestParam(required = false) String riskLevel,
                                                       @RequestParam(required = false) String name) {
        Pageable p = PageRequest.of(page, size);
        var resp = projectQueryService.list(p, status, managerId, riskLevel, name);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable Long id) {
        var resp = projectQueryService.getById(id);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/report")
    public ResponseEntity<com.code.group.challenge.projects_portfolio.project.dto.PortfolioReportResponse> report() {
        var r = projectReportService.getReport();
        return ResponseEntity.ok(r);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProjectResponse> changeStatus(@PathVariable Long id, @Valid @RequestBody StatusChangeRequest req) {
        var resp = projectCommandService.changeStatus(id, req.getStatus());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectResponse> addMember(@PathVariable Long id, @Valid @RequestBody MemberAssociationRequest req) {
        var resp = projectCommandService.addMember(id, req);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<ProjectResponse> removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        var resp = projectCommandService.removeMember(id, memberId);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest req) {
        var resp = projectCommandService.update(id, req);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
