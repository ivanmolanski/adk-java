package com.mdaesthetics.viral.dto;

import java.util.List;

/** Generic container for paginated API responses */
public record PaginatedResult<T>(
        List<T> items,
        String nextCursor,
        boolean hasMore
) {}