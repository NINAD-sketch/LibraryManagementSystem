package com.ninad.library.repository;

import com.ninad.library.domain.Book;
import com.ninad.library.domain.Member;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryCatalogRepository implements CatalogRepository {
    private final Map<String, Book> books = new LinkedHashMap<>();
    private final Map<String, Member> members = new LinkedHashMap<>();

    @Override
    public void saveBook(Book book) {
        Objects.requireNonNull(book, "book must not be null");
        if (books.putIfAbsent(book.id(), book) != null) {
            throw new IllegalArgumentException("book ID already exists: " + book.id());
        }
    }

    @Override
    public void saveMember(Member member) {
        Objects.requireNonNull(member, "member must not be null");
        if (members.putIfAbsent(member.id(), member) != null) {
            throw new IllegalArgumentException("member ID already exists: " + member.id());
        }
    }

    @Override
    public Optional<Book> findBook(String id) {
        return Optional.ofNullable(books.get(id));
    }

    @Override
    public Optional<Member> findMember(String id) {
        return Optional.ofNullable(members.get(id));
    }

    @Override
    public List<Book> findAllBooks() {
        return List.copyOf(new ArrayList<>(books.values()));
    }
}
