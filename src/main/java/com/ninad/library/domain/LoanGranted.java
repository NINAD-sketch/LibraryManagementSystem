package com.ninad.library.domain;

import java.util.Objects;

public record LoanGranted(Loan loan) implements LoanResult {
    public LoanGranted {
        Objects.requireNonNull(loan, "loan must not be null");
    }
}
