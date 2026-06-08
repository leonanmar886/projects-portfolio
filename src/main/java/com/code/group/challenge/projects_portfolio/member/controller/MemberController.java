package com.code.group.challenge.projects_portfolio.member.controller;

import com.code.group.challenge.projects_portfolio.member.dto.MemberCreateRequest;
import com.code.group.challenge.projects_portfolio.member.dto.MemberResponse;
import com.code.group.challenge.projects_portfolio.member.dto.MemberUpdateRequest;
import com.code.group.challenge.projects_portfolio.member.mapper.MemberMapper;
import com.code.group.challenge.projects_portfolio.member.service.MemberCommandService;
import com.code.group.challenge.projects_portfolio.member.service.MemberQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;

    public MemberController(MemberCommandService memberCommandService, MemberQueryService memberQueryService) {
        this.memberCommandService = memberCommandService;
        this.memberQueryService = memberQueryService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberCreateRequest req) {
        var entity = MemberMapper.toEntity(req);
        var saved = memberCommandService.create(entity);
        var resp = MemberMapper.toResponse(saved);
        return ResponseEntity.created(URI.create("/api/members/" + resp.getId())).body(resp);
    }

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<MemberResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                                                       @RequestParam(defaultValue = "20") int size,
                                                                                       @RequestParam(required = false) com.code.group.challenge.projects_portfolio.member.domain.MemberRole role,
                                                                                       @RequestParam(required = false) String name) {
        var p = org.springframework.data.domain.PageRequest.of(page, size);
        var pageRes = memberQueryService.list(p, role, name).map(MemberMapper::toResponse);
        return ResponseEntity.ok(pageRes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getById(@PathVariable Long id) {
        var member = memberQueryService.getById(id);
        return ResponseEntity.ok(MemberMapper.toResponse(member));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> update(@PathVariable Long id, @Valid @RequestBody MemberUpdateRequest req) {
        var updated = memberCommandService.update(id, req);
        return ResponseEntity.ok(MemberMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
