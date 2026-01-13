package com.be.server.core.common.base;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
@Getter
public class PageableObject<T> {

    private final List<T> data;

    private final long totalPages;

    private final int currentPage;

    private final long totalElements;;

    // Constructor nhận Page
    public PageableObject(Page<T> page) {
        this.data = page.getContent();
        this.totalPages = page.getTotalPages();
        this.currentPage = page.getNumber();
        this.totalElements = page.getTotalElements();
    }

    // Constructor nhận List + page info
    public PageableObject(List<T> data, int currentPage, long totalPages, long totalElements) {
        this.data = data;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public static <T> PageableObject<T> of(Page<?> page) {
        return new PageableObject<>((Page<T>) page);
    }

    public static <T> PageableObject<T> of(List<T> data, int currentPage, long totalPages, long totalElements) {
        return new PageableObject<>(data, currentPage, totalPages, totalElements);
    }
}
