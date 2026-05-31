package com.code.group.challenge.projects_portfolio.member.controller;

import com.code.group.challenge.projects_portfolio.member.dto.MemberCreateRequest;
import com.code.group.challenge.projects_portfolio.member.dto.MemberResponse;
import com.code.group.challenge.projects_portfolio.member.dto.MemberUpdateRequest;
import com.code.group.challenge.projects_portfolio.member.mapper.MemberMapper;
import com.code.group.challenge.projects_portfolio.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberCreateRequest req) {
        var entity = MemberMapper.toEntity(req);
        var saved = memberService.create(entity);
        var resp = MemberMapper.toResponse(saved);
        return ResponseEntity.created(URI.create("/api/members/" + resp.getId())).body(resp);
    }

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<MemberResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                                                       @RequestParam(defaultValue = "20") int size,
                                                                                       @RequestParam(required = false) com.code.group.challenge.projects_portfolio.member.domain.MemberRole role,
                                                                                       @RequestParam(required = false) String name) {
        var p = org.springframework.data.domain.PageRequest.of(page, size);
        var pageRes = memberService.list(p, role, name).map(MemberMapper::toResponse);
        return ResponseEntity.ok(pageRes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getById(@PathVariable Long id) {
        var member = memberService.getById(id);
        return ResponseEntity.ok(MemberMapper.toResponse(member));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> update(@PathVariable Long id, @Valid @RequestBody MemberUpdateRequest req) {
        var updated = memberService.update(id, req);
        return ResponseEntity.ok(MemberMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
