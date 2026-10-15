package com.ninad.library.repository;

import com.ninad.library.domain.Book;
import com.ninad.library.domain.Member;

import java.util.List;
import java.util.Optional;

public interface CatalogRepository {
    void saveBook(Book book);
    void saveMember(Member member);
    Optional<Book> findBook(String id);
    Optional<Member> findMember(String id);
    List<Book> findAllBooks();
}
