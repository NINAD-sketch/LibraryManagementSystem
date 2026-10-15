package com.ninad.library.service;

import com.ninad.library.domain.Book;
import com.ninad.library.domain.Fine;
import com.ninad.library.domain.Loan;
import com.ninad.library.domain.LoanGranted;
import com.ninad.library.domain.LoanRejected;
import com.ninad.library.domain.LoanResult;
import com.ninad.library.domain.Member;
import com.ninad.library.repository.CatalogRepository;
import com.ninad.library.repository.LoanRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class LibraryService {
    private final CatalogRepository catalogRepository;
    private final LoanRepository loanRepository;
    private final int loanPeriodDays;
    private final int maxActiveLoans;
    private final BigDecimal dailyFineRate;
    private final AtomicLong nextLoanId = new AtomicLong(1);
    private final AtomicLong nextFineId = new AtomicLong(1);

    public LibraryService(CatalogRepository catalogRepository,
                          LoanRepository loanRepository,
                          int loanPeriodDays,
                          int maxActiveLoans,
                          BigDecimal dailyFineRate) {
        this.catalogRepository = Objects.requireNonNull(catalogRepository, "catalogRepository must not be null");
        this.loanRepository = Objects.requireNonNull(loanRepository, "loanRepository must not be null");
        if (loanPeriodDays <= 0) {
            throw new IllegalArgumentException("loan period must be positive");
        }
        if (maxActiveLoans <= 0) {
            throw new IllegalArgumentException("active-loan limit must be positive");
        }
        this.dailyFineRate = Objects.requireNonNull(dailyFineRate, "dailyFineRate must not be null");
        if (dailyFineRate.signum() <= 0) {
            throw new IllegalArgumentException("daily fine rate must be positive");
        }
        this.loanPeriodDays = loanPeriodDays;
        this.maxActiveLoans = maxActiveLoans;
    }

    public void addBook(Book book) {
        Objects.requireNonNull(book, "book must not be null");
        if (catalogRepository.findBook(book.id()).isPresent()) {
            throw new IllegalArgumentException("book ID already exists: " + book.id());
        }
        catalogRepository.saveBook(book);
    }

    public void registerMember(Member member) {
        Objects.requireNonNull(member, "member must not be null");
        if (catalogRepository.findMember(member.id()).isPresent()) {
            throw new IllegalArgumentException("member ID already exists: " + member.id());
        }
        catalogRepository.saveMember(member);
    }

    public Optional<Book> findBook(String id) {
        return catalogRepository.findBook(id);
    }

    public Optional<Member> findMember(String id) {
        return catalogRepository.findMember(id);
    }

    public List<Book> listBooks() {
        return catalogRepository.findAllBooks();
    }

    public LoanResult borrowBook(String bookId, String memberId, LocalDate borrowedOn) {
        Objects.requireNonNull(borrowedOn, "borrowedOn must not be null");
        if (catalogRepository.findBook(bookId).isEmpty()) {
            return new LoanRejected("Book not found: " + bookId);
        }
        if (catalogRepository.findMember(memberId).isEmpty()) {
            return new LoanRejected("Member not found: " + memberId);
        }
        if (loanRepository.findActiveLoanByBookId(bookId).isPresent()) {
            return new LoanRejected("Book is already on loan: " + bookId);
        }
        long activeLoans = loanRepository.findByMemberId(memberId).stream()
                .filter(loan -> !loan.isReturned())
                .count();
        if (activeLoans >= maxActiveLoans) {
            return new LoanRejected("Member has reached the active-loan limit");
        }

        Loan loan = new Loan(nextLoanId.getAndIncrement(), bookId, memberId,
                borrowedOn, borrowedOn.plusDays(loanPeriodDays), Optional.empty());
        loanRepository.saveLoan(loan);
        return new LoanGranted(loan);
    }

    public String describe(LoanResult result) {
        Objects.requireNonNull(result, "result must not be null");
        return switch (result) {
            case LoanGranted granted -> "Loan granted. Loan ID: " + granted.loan().id();
            case LoanRejected rejected -> "Loan rejected: " + rejected.reason();
        };
    }

    public Optional<Fine> returnBook(long loanId, LocalDate returnedOn) {
        Objects.requireNonNull(returnedOn, "returnedOn must not be null");
        Loan currentLoan = loanRepository.findLoanById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("loan not found: " + loanId));
        long overdueDays = currentLoan.overdueDays(returnedOn);
        Loan returnedLoan = currentLoan.markReturned(returnedOn);
        loanRepository.saveLoan(returnedLoan);

        if (overdueDays == 0) {
            return Optional.empty();
        }
        BigDecimal amount = dailyFineRate.multiply(BigDecimal.valueOf(overdueDays));
        Fine fine = new Fine(nextFineId.getAndIncrement(), loanId, amount, false);
        loanRepository.saveFine(fine);
        return Optional.of(fine);
    }

    public Optional<Fine> payFine(long fineId) {
        return loanRepository.findFineById(fineId).map(fine -> {
            Fine paidFine = fine.markPaid();
            loanRepository.saveFine(paidFine);
            return paidFine;
        });
    }

    public List<Fine> finesForLoan(long loanId) {
        return loanRepository.findFinesByLoanId(loanId);
    }
}
