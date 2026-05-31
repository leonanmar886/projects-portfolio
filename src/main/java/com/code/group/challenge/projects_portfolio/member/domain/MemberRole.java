package com.code.group.challenge.projects_portfolio.member.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MemberRole {
    FUNCIONARIO("funcionario"),
    GERENTE("gerente");

    private final String value;

    MemberRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MemberRole fromString(String text) {
        if (text == null) return null;
        for (MemberRole b : MemberRole.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + text);
    }
}

