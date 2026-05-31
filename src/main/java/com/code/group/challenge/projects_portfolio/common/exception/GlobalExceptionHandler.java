package com.code.group.challenge.projects_portfolio.common.exception;

import com.code.group.challenge.projects_portfolio.project.exception.InvalidStatusTransitionException;
import com.code.group.challenge.projects_portfolio.project.exception.MemberAllocationException;
import com.code.group.challenge.projects_portfolio.project.exception.ProjectDeletionException;
import com.code.group.challenge.projects_portfolio.project.exception.ProjectNotFoundException;
import com.code.group.challenge.projects_portfolio.project.exception.ProjectValidationException;
import com.code.group.challenge.projects_portfolio.member.exception.MemberRoleChangeException;
import com.code.group.challenge.projects_portfolio.member.exception.MemberDeletionException;
import com.code.group.challenge.projects_portfolio.member.exception.MemberNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> buildBody(HttpStatus status, String message, String path) {
        return Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "path", path
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var body = buildBody(HttpStatus.BAD_REQUEST, "Validation error", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ProjectNotFoundException ex, HttpServletRequest request) {
        var body = buildBody(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<Object> handleMemberNotFound(MemberNotFoundException ex, HttpServletRequest request) {
        var body = buildBody(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler({InvalidStatusTransitionException.class, ProjectValidationException.class})
    public ResponseEntity<Object> handleUnprocessable(RuntimeException ex, HttpServletRequest request) {
        var body = buildBody(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(MemberAllocationException.class)
    public ResponseEntity<Object> handleConflict(MemberAllocationException ex, HttpServletRequest request) {
        var body = buildBody(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(ProjectDeletionException.class)
    public ResponseEntity<Object> handleDeletion(ProjectDeletionException ex, HttpServletRequest request) {
        var body = buildBody(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler({MemberRoleChangeException.class, MemberDeletionException.class})
    public ResponseEntity<Object> handleMemberConflict(RuntimeException ex, HttpServletRequest request) {
        var body = buildBody(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex, HttpServletRequest request) {
        var body = buildBody(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
