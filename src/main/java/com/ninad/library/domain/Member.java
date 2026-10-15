package com.ninad.library.domain;

import java.util.Objects;

public record Member(String id, String name, String email) {
    public Member {
        id = requireText(id, "id");
        name = requireText(name, "name");
        email = requireText(email, "email");
        if (!email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            throw new IllegalArgumentException("email must contain a local part and domain");
        }
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
