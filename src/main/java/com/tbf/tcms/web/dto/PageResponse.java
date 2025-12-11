package com.tbf.tcms.web.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Global, lightweight page wrapper returned to the frontend.
 * Uses Java 17 record for immutability and concise syntax.
 *
 * Tribal example: When the Ntona (headman) views "all open cases in the village",
 * we page results to keep the UI fast for large communities.
 */
public record PageResponse<T>(
        int currentPage,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        List<T> content
){
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.getContent()
        );
    }
}
