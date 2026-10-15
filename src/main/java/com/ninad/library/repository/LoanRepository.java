package com.ninad.library.repository;

import com.ninad.library.domain.Fine;
import com.ninad.library.domain.Loan;

import java.util.List;
import java.util.Optional;

public interface LoanRepository {
    void saveLoan(Loan loan);
    Optional<Loan> findLoanById(long id);
    Optional<Loan> findActiveLoanByBookId(String bookId);
    List<Loan> findByMemberId(String memberId);
    void saveFine(Fine fine);
    Optional<Fine> findFineById(long id);
    List<Fine> findFinesByLoanId(long loanId);
}
