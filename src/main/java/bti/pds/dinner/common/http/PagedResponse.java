package bti.pds.dinner.common.http;

import bti.pds.dinner.common.domain.PagedResult;

import java.util.List;
import java.util.function.Function;

public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean isFirst,
        boolean last
) {
    public static <E, T> PagedResponse<T> from(PagedResult<E> pagedResult, Function<E, T> mapper) {
        return new PagedResponse<>(
                pagedResult.content().stream().map(mapper).toList(),
                pagedResult.page(),
                pagedResult.size(),
                pagedResult.totalElements(),
                pagedResult.totalPages(),
                pagedResult.isFirst(),
                pagedResult.isLast()
        );
    }
}
