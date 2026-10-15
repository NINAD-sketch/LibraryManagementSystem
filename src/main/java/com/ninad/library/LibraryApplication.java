package com.ninad.library;

import com.ninad.library.domain.Book;
import com.ninad.library.domain.Fine;
import com.ninad.library.domain.LoanGranted;
import com.ninad.library.domain.LoanRejected;
import com.ninad.library.domain.LoanResult;
import com.ninad.library.domain.Member;
import com.ninad.library.repository.InMemoryCatalogRepository;
import com.ninad.library.repository.InMemoryLoanRepository;
import com.ninad.library.service.LibraryService;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class LibraryApplication {
    private LibraryApplication() {
    }

    public static void main(String[] args) {
        var catalog = new InMemoryCatalogRepository();
        var circulation = new InMemoryLoanRepository();
        var library = new LibraryService(catalog, circulation, 14, 3, new BigDecimal("2.50"));

        library.addBook(new Book("B-101", "Effective Java", "Joshua Bloch", "978-0134685991"));
        library.registerMember(new Member("M-101", "Aarav Patil", "aarav@example.com"));

        LocalDate borrowedOn = LocalDate.of(2026, 1, 5);
        LoanResult result = library.borrowBook("B-101", "M-101", borrowedOn);
        System.out.println(library.describe(result));

        switch (result) {
            case LoanGranted granted -> {
                LocalDate returnedOn = borrowedOn.plusDays(17);
                var fine = library.returnBook(granted.loan().id(), returnedOn);
                if (fine.isPresent()) {
                    Fine createdFine = fine.orElseThrow();
                    System.out.println("Overdue fine: " + createdFine.amount());
                    library.payFine(createdFine.id()).ifPresent(paid ->
                            System.out.println("Fine payment status: " + (paid.paid() ? "PAID" : "UNPAID")));
                } else {
                    System.out.println("Book returned on time; no fine was created.");
                }
            }
            case LoanRejected rejected -> System.out.println(rejected.reason());
        }
    }
}
