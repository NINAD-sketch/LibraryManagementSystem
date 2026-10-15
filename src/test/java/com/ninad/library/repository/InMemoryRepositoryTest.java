package com.ninad.library.repository;

import com.ninad.library.domain.Book;
import com.ninad.library.domain.Fine;
import com.ninad.library.domain.Loan;
import com.ninad.library.domain.Member;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRepositoryTest {
    @Test
    void catalogRepositorySavesFindsAndListsRecords() {
        InMemoryCatalogRepository repository = new InMemoryCatalogRepository();
        Book book = new Book("B-1", "Clean Code", "Robert Martin", "ISBN-1");
        Member member = new Member("M-1", "Nina", "nina@example.com");

        assertTrue(repository.findBook("B-1").isEmpty());
        assertTrue(repository.findMember("M-1").isEmpty());
        assertTrue(repository.findAllBooks().isEmpty());
        repository.saveBook(book);
        repository.saveMember(member);
        assertEquals(Optional.of(book), repository.findBook("B-1"));
        assertEquals(Optional.of(member), repository.findMember("M-1"));
        assertEquals(1, repository.findAllBooks().size());
        assertThrows(IllegalArgumentException.class, () -> repository.saveBook(book));
        assertThrows(IllegalArgumentException.class, () -> repository.saveMember(member));
        assertThrows(NullPointerException.class, () -> repository.saveBook(null));
        assertThrows(NullPointerException.class, () -> repository.saveMember(null));
    }

    @Test
    void loanRepositoryUpdatesLoansAndQueriesActiveLoansAndFines() {
        InMemoryLoanRepository repository = new InMemoryLoanRepository();
        LocalDate borrowed = LocalDate.of(2026, 1, 1);
        Loan active = new Loan(1, "B-1", "M-1", borrowed, borrowed.plusDays(14), Optional.empty());
        Loan returned = active.markReturned(borrowed.plusDays(4));

        assertTrue(repository.findLoanById(1).isEmpty());
        assertTrue(repository.findActiveLoanByBookId("B-1").isEmpty());
        assertTrue(repository.findByMemberId("M-1").isEmpty());
        repository.saveLoan(active);
        assertEquals(Optional.of(active), repository.findLoanById(1));
        assertEquals(Optional.of(active), repository.findActiveLoanByBookId("B-1"));
        assertEquals(1, repository.findByMemberId("M-1").size());
        repository.saveLoan(returned);
        assertTrue(repository.findActiveLoanByBookId("B-1").isEmpty());
        assertEquals(Optional.of(returned), repository.findLoanById(1));

        Fine fine = new Fine(1, 1, new BigDecimal("5.00"), false);
        assertTrue(repository.findFineById(1).isEmpty());
        assertTrue(repository.findFinesByLoanId(1).isEmpty());
        repository.saveFine(fine);
        assertEquals(Optional.of(fine), repository.findFineById(1));
        assertEquals(1, repository.findFinesByLoanId(1).size());
        Fine paid = fine.markPaid();
        repository.saveFine(paid);
        assertTrue(repository.findFineById(1).orElseThrow().paid());
        assertThrows(NullPointerException.class, () -> repository.saveLoan(null));
        assertThrows(NullPointerException.class, () -> repository.saveFine(null));
    }
}
