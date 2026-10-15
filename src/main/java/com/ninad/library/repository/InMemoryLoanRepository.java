package com.ninad.library.repository;

import com.ninad.library.domain.Fine;
import com.ninad.library.domain.Loan;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryLoanRepository implements LoanRepository {
    private final Map<Long, Loan> loans = new LinkedHashMap<>();
    private final Map<Long, Fine> fines = new LinkedHashMap<>();

    @Override
    public void saveLoan(Loan loan) {
        Objects.requireNonNull(loan, "loan must not be null");
        loans.put(loan.id(), loan);
    }

    @Override
    public Optional<Loan> findLoanById(long id) {
        return Optional.ofNullable(loans.get(id));
    }

    @Override
    public Optional<Loan> findActiveLoanByBookId(String bookId) {
        return loans.values().stream()
                .filter(loan -> loan.bookId().equals(bookId) && !loan.isReturned())
                .findFirst();
    }

    @Override
    public List<Loan> findByMemberId(String memberId) {
        return loans.values().stream()
                .filter(loan -> loan.memberId().equals(memberId))
                .toList();
    }

    @Override
    public void saveFine(Fine fine) {
        Objects.requireNonNull(fine, "fine must not be null");
        fines.put(fine.id(), fine);
    }

    @Override
    public Optional<Fine> findFineById(long id) {
        return Optional.ofNullable(fines.get(id));
    }

    @Override
    public List<Fine> findFinesByLoanId(long loanId) {
        return fines.values().stream()
                .filter(fine -> fine.loanId() == loanId)
                .toList();
    }
}
