package com.ninad.library.domain;

import java.util.Objects;

public record LoanRejected(String reason) implements LoanResult {
    public LoanRejected {
        Objects.requireNonNull(reason, "reason must not be null");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        reason = reason.trim();
    }
}
