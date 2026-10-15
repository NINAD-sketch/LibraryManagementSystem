package com.ninad.library.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LoanTest {
    private final LocalDate borrowedOn = LocalDate.of(2026, 1, 1);
    private final LocalDate dueOn = borrowedOn.plusDays(14);

    @Test
    void loanTracksDueDateAndReturnsANewValue() {
        Loan loan = new Loan(1, "B-1", "M-1", borrowedOn, dueOn, Optional.empty());

        assertFalse(loan.isReturned());
        assertEquals(0, loan.overdueDays(dueOn));
        assertEquals(3, loan.overdueDays(dueOn.plusDays(3)));

        Loan returned = loan.markReturned(dueOn.plusDays(3));
        assertTrue(returned.isReturned());
        assertEquals(Optional.of(dueOn.plusDays(3)), returned.returnedOn());
        assertFalse(loan.isReturned(), "the original record remains unchanged");
        assertEquals(3, returned.overdueDays(dueOn.plusDays(10)));
    }

    @Test
    void rejectsInvalidLoanDatesAndIds() {
        assertThrows(IllegalArgumentException.class,
                () -> new Loan(0, "B-1", "M-1", borrowedOn, dueOn, Optional.empty()));
        assertThrows(IllegalArgumentException.class,
                () -> new Loan(1, "B-1", "M-1", borrowedOn, borrowedOn.minusDays(1), Optional.empty()));
        assertThrows(NullPointerException.class,
                () -> new Loan(1, "B-1", "M-1", borrowedOn, dueOn, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Loan(1, "B-1", "M-1", borrowedOn, dueOn,
                        Optional.of(borrowedOn.minusDays(1))));
        assertThrows(IllegalArgumentException.class, () -> new Loan(1, " ", "M-1",
                borrowedOn, dueOn, Optional.empty()));
        assertThrows(IllegalArgumentException.class,
                () -> new Loan(1, "B-1", "M-1", borrowedOn, dueOn, Optional.empty())
                        .overdueDays(borrowedOn.minusDays(1)));
    }

    @Test
    void aLoanCannotBeReturnedTwiceOrBeforeBorrowing() {
        Loan loan = new Loan(1, "B-1", "M-1", borrowedOn, dueOn, Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> loan.markReturned(borrowedOn.minusDays(1)));
        Loan returned = loan.markReturned(borrowedOn.plusDays(1));
        assertThrows(IllegalStateException.class, () -> returned.markReturned(borrowedOn.plusDays(2)));
    }

    @Test
    void recordsValidateTheirDataAndFineCanBePaid() {
        assertEquals("B-1", new Book("B-1", " Title ", " Author ", " ISBN ").id());
        assertEquals("Title", new Book("B-1", " Title ", " Author ", " ISBN ").title());
        assertThrows(NullPointerException.class, () -> new Book(null, "Title", "Author", "ISBN"));
        assertThrows(IllegalArgumentException.class, () -> new Book("B-1", " ", "Author", "ISBN"));
        assertThrows(IllegalArgumentException.class, () -> new Member("M-1", "Name", "not-an-email"));
        assertThrows(NullPointerException.class, () -> new Member("M-1", null, "a@example.com"));
        assertEquals("a@example.com", new Member("M-1", " Name ", " a@example.com ").email());

        Fine fine = new Fine(1, 1, new BigDecimal("7.505"), false);
        assertEquals(new BigDecimal("7.51"), fine.amount());
        assertTrue(fine.markPaid().paid());
        Fine paid = fine.markPaid();
        assertSame(paid, paid.markPaid());
        assertThrows(IllegalArgumentException.class, () -> new Fine(0, 1, BigDecimal.ONE, false));
        assertThrows(IllegalArgumentException.class, () -> new Fine(1, 1, BigDecimal.ZERO, false));
        assertThrows(NullPointerException.class, () -> new Fine(1, 1, null, false));
        assertThrows(NullPointerException.class, () -> new LoanGranted(null));
        assertThrows(NullPointerException.class, () -> new LoanRejected(null));
        assertThrows(IllegalArgumentException.class, () -> new LoanRejected(" "));
        assertEquals("not allowed", new LoanRejected(" not allowed ").reason());
    }
}
