package com.ninad.library.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

public record Loan(long id, String bookId, String memberId,
                   LocalDate borrowedOn, LocalDate dueOn,
                   Optional<LocalDate> returnedOn) {
    public Loan {
        if (id <= 0) {
            throw new IllegalArgumentException("loan id must be positive");
        }
        bookId = requireText(bookId, "bookId");
        memberId = requireText(memberId, "memberId");
        Objects.requireNonNull(borrowedOn, "borrowedOn must not be null");
        Objects.requireNonNull(dueOn, "dueOn must not be null");
        Objects.requireNonNull(returnedOn, "returnedOn must not be null; use Optional.empty()");
        if (dueOn.isBefore(borrowedOn)) {
            throw new IllegalArgumentException("due date cannot be before borrowed date");
        }
        if (returnedOn.isPresent() && returnedOn.orElseThrow().isBefore(borrowedOn)) {
            throw new IllegalArgumentException("return date cannot be before borrowed date");
        }
    }

    public boolean isReturned() {
        return returnedOn.isPresent();
    }

    public long overdueDays(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        if (date.isBefore(borrowedOn)) {
            throw new IllegalArgumentException("date cannot be before borrowed date");
        }
        LocalDate effectiveDate = returnedOn.orElse(date);
        return Math.max(0, ChronoUnit.DAYS.between(dueOn, effectiveDate));
    }

    public Loan markReturned(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        if (isReturned()) {
            throw new IllegalStateException("loan has already been returned");
        }
        if (date.isBefore(borrowedOn)) {
            throw new IllegalArgumentException("return date cannot be before borrowed date");
        }
        return new Loan(id, bookId, memberId, borrowedOn, dueOn, Optional.of(date));
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
