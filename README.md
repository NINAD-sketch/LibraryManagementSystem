# Library Management System — M3.LA.01

## What the system does

This small Java 21 application models the everyday circulation work of a library. It keeps a catalogue of books and members, records loans, calculates late-return fines, and records fine payments. Data is stored in memory so the project stays focused on object-oriented design rather than database setup.

## Who uses it

A librarian uses the system to register books and members, lend available books, accept returns, and collect overdue fines. The console application demonstrates one complete borrow-return-payment flow.

## Four main domain operations

1. **Catalogue management:** add books and register members; look them up by ID.
2. **Borrow a book:** validate the book and member, enforce availability and an active-loan limit, and create a loan.
3. **Return a book:** close the loan and create a fine when the return date is after the due date.
4. **Pay a fine:** mark an outstanding fine as paid; repeating a payment request is safe and leaves it paid.

## Design

- `Book`, `Member`, `Loan`, and `Fine` are Java records with constructor validation.
- `LoanResult` is a sealed interface with `LoanGranted` and `LoanRejected` alternatives. The service uses pattern matching for `switch` to describe either result.
- `Optional` represents searches and the optional return date; missing records are not represented with `null`.
- `CatalogRepository` and `LoanRepository` are repository interfaces. The service receives them through its constructor (composition), while in-memory implementations store data in maps.
- JUnit 5 tests cover domain rules, repository behavior, and service operations. JaCoCo checks that line coverage reaches at least 80% during `mvn verify`.

## Requirements

- JDK 21
- Maven 3.9 or later

## Build and test

```bash
mvn clean verify
```

The command compiles with Java 21, runs the JUnit 5 tests, generates a JaCoCo report, and checks the 80% line-coverage threshold. Open `target/site/jacoco/index.html` to inspect coverage after a successful build.

## Run the demo

```bash
mvn compile exec:java
```

The example registers one book and member, borrows the book, returns it three days after the due date, and pays the resulting fine.

## Scope and assumptions

This is a teaching project, not a production library system. Data is lost when the application stops. There is no authentication, database, reservation queue, or concurrent checkout handling. The fine rate and loan duration are configurable in the service constructor so tests can use deterministic dates.
