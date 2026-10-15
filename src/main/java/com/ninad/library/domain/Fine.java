package com.ninad.library.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Fine(long id, long loanId, BigDecimal amount, boolean paid) {
    public Fine {
        if (id <= 0 || loanId <= 0) {
            throw new IllegalArgumentException("fine and loan IDs must be positive");
        }
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("fine amount must be greater than zero");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Fine markPaid() {
        return paid ? this : new Fine(id, loanId, amount, true);
    }
}
