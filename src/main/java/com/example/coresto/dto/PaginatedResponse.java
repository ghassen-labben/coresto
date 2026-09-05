package com.example.coresto.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * <p>Provides page metadata alongside the actual content, following
 * a Spring Page-like structure.
 *
 * @param <T> the type of elements in the page
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {

    /** The items in the current page. */
    private List<T> content;

    /** Zero-based page number. */
    private int pageNumber;

    /** Requested page size (max items per page). */
    private int pageSize;

    /** Total number of elements across all pages. */
    private long totalElements;

    /** Total number of pages. */
    private int totalPages;

    /** Whether this is the first page. */
    private boolean first;

    /** Whether this is the last page. */
    private boolean last;

    /** Number of elements in the current page (may be less than pageSize on the last page). */
    private int numberOfElements;

    /**
     * Factory method to build a PaginatedResponse from the raw ingredients.
     */
    public static <T> PaginatedResponse<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;

        return PaginatedResponse.<T>builder()
                .content(content)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(pageNumber == 0)
                .last(pageNumber >= totalPages - 1 || totalPages == 0)
                .numberOfElements(content.size())
                .build();
    }
}
