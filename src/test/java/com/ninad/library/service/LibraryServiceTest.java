package com.ninad.library.service;

import com.ninad.library.LibraryApplication;
import com.ninad.library.domain.Book;
import com.ninad.library.domain.Fine;
import com.ninad.library.domain.Loan;
import com.ninad.library.domain.LoanGranted;
import com.ninad.library.domain.LoanRejected;
import com.ninad.library.domain.LoanResult;
import com.ninad.library.domain.Member;
import com.ninad.library.repository.InMemoryCatalogRepository;
import com.ninad.library.repository.InMemoryLoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LibraryServiceTest {
    private InMemoryCatalogRepository catalog;
    private InMemoryLoanRepository loans;
    private LibraryService library;
    private final LocalDate start = LocalDate.of(2026, 1, 5);

    @BeforeEach
    void setUp() {
        catalog = new InMemoryCatalogRepository();
        loans = new InMemoryLoanRepository();
        library = new LibraryService(catalog, loans, 14, 3, new BigDecimal("2.50"));
    }

    @Test
    void registersAndFindsBooksAndMembersAndRejectsDuplicateIds() {
        assertTrue(library.findBook("B-1").isEmpty());
        assertTrue(library.findMember("M-1").isEmpty());
        Book book = book("B-1");
        Member member = member("M-1");
        library.addBook(book);
        library.registerMember(member);
        assertEquals(book, library.findBook("B-1").orElseThrow());
        assertEquals(member, library.findMember("M-1").orElseThrow());
        assertEquals(1, library.listBooks().size());
        assertThrows(IllegalArgumentException.class, () -> library.addBook(book));
        assertThrows(IllegalArgumentException.class, () -> library.registerMember(member));
        assertThrows(NullPointerException.class, () -> library.addBook(null));
        assertThrows(NullPointerException.class, () -> library.registerMember(null));
    }

    @Test
    void borrowingUsesSealedResultsAndRejectsMissingOrUnavailableItems() {
        LoanResult missingBook = library.borrowBook("B-404", "M-1", start);
        assertInstanceOf(LoanRejected.class, missingBook);
        assertTrue(library.describe(missingBook).contains("Book not found"));

        library.addBook(book("B-1"));
        LoanResult missingMember = library.borrowBook("B-1", "M-404", start);
        assertInstanceOf(LoanRejected.class, missingMember);
        assertTrue(library.describe(missingMember).contains("Member not found"));

        library.registerMember(member("M-1"));
        LoanResult grantedResult = library.borrowBook("B-1", "M-1", start);
        assertInstanceOf(LoanGranted.class, grantedResult);
        Loan granted = ((LoanGranted) grantedResult).loan();
        assertEquals(start.plusDays(14), granted.dueOn());
        assertTrue(library.describe(grantedResult).contains("Loan granted"));

        LoanResult unavailable = library.borrowBook("B-1", "M-1", start.plusDays(1));
        assertInstanceOf(LoanRejected.class, unavailable);
        assertTrue(library.describe(unavailable).contains("already on loan"));
        assertThrows(NullPointerException.class, () -> library.borrowBook("B-1", "M-1", null));
        assertThrows(NullPointerException.class, () -> library.describe(null));
    }

    @Test
    void enforcesMemberActiveLoanLimit() {
        library = new LibraryService(catalog, loans, 14, 1, new BigDecimal("2.50"));
        library.addBook(book("B-1"));
        library.addBook(book("B-2"));
        library.registerMember(member("M-1"));

        assertInstanceOf(LoanGranted.class, library.borrowBook("B-1", "M-1", start));
        LoanResult second = library.borrowBook("B-2", "M-1", start);
        assertInstanceOf(LoanRejected.class, second);
        assertTrue(library.describe(second).contains("active-loan limit"));
    }

    @Test
    void onTimeReturnCreatesNoFineAndReturnedBookCanBeBorrowedAgain() {
        arrangeOneBookAndMember();
        Loan loan = ((LoanGranted) library.borrowBook("B-1", "M-1", start)).loan();

        assertTrue(library.returnBook(loan.id(), loan.dueOn()).isEmpty());
        assertTrue(loans.findLoanById(loan.id()).orElseThrow().isReturned());
        assertTrue(library.borrowBook("B-1", "M-1", loan.dueOn().plusDays(1)) instanceof LoanGranted);
        assertThrows(IllegalStateException.class, () -> library.returnBook(loan.id(), loan.dueOn().plusDays(2)));
        assertThrows(IllegalArgumentException.class, () -> library.returnBook(999, start));
        assertThrows(NullPointerException.class, () -> library.returnBook(loan.id(), null));
    }

    @Test
    void overdueReturnCreatesFineAndFinePaymentIsIdempotent() {
        arrangeOneBookAndMember();
        Loan loan = ((LoanGranted) library.borrowBook("B-1", "M-1", start)).loan();

        Fine fine = library.returnBook(loan.id(), loan.dueOn().plusDays(3)).orElseThrow();
        assertEquals(new BigDecimal("7.50"), fine.amount());
        assertFalse(fine.paid());
        assertEquals(1, library.finesForLoan(loan.id()).size());
        assertTrue(library.payFine(999).isEmpty());
        Fine paid = library.payFine(fine.id()).orElseThrow();
        assertTrue(paid.paid());
        assertTrue(library.payFine(fine.id()).orElseThrow().paid());
        assertTrue(library.finesForLoan(999).isEmpty());
    }

    @Test
    void rejectsInvalidServiceConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> new LibraryService(catalog, loans, 0, 3, new BigDecimal("1.00")));
        assertThrows(IllegalArgumentException.class,
                () -> new LibraryService(catalog, loans, 14, 0, new BigDecimal("1.00")));
        assertThrows(IllegalArgumentException.class,
                () -> new LibraryService(catalog, loans, 14, 3, BigDecimal.ZERO));
        assertThrows(NullPointerException.class,
                () -> new LibraryService(null, loans, 14, 3, new BigDecimal("1.00")));
        assertThrows(NullPointerException.class,
                () -> new LibraryService(catalog, null, 14, 3, new BigDecimal("1.00")));
        assertThrows(NullPointerException.class,
                () -> new LibraryService(catalog, loans, 14, 3, null));
    }

    @Test
    void demoRunsThroughBorrowReturnAndPaymentFlow() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try (PrintStream replacement = new PrintStream(captured)) {
            System.setOut(replacement);
            LibraryApplication.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }
        String output = captured.toString();
        assertTrue(output.contains("Loan granted"));
        assertTrue(output.contains("Overdue fine: 7.50"));
        assertTrue(output.contains("Fine payment status: PAID"));
    }

    private void arrangeOneBookAndMember() {
        library.addBook(book("B-1"));
        library.registerMember(member("M-1"));
    }

    private Book book(String id) {
        return new Book(id, "Clean Code", "Robert Martin", "ISBN-" + id);
    }

    private Member member(String id) {
        return new Member(id, "Test Member", id.toLowerCase() + "@example.com");
    }
}
