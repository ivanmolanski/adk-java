package com.mdaesthetics.viral.model;

import java.util.List;

/**
 * Generic container for paginated results.
 */
public record PaginatedResult<T>(
    List<T> items,
    String nextCursor,
    boolean hasMore
) {
}