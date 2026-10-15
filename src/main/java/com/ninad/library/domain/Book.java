package com.ninad.library.domain;

import java.util.Objects;

public record Book(String id, String title, String author, String isbn) {
    public Book {
        id = requireText(id, "id");
        title = requireText(title, "title");
        author = requireText(author, "author");
        isbn = requireText(isbn, "isbn");
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
