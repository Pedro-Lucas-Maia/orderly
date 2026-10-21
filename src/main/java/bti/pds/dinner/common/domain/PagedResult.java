package bti.pds.dinner.common.domain;

import java.util.List;
import java.util.function.Function;

public record PagedResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean isFirst,
        boolean isLast
) {
    public <R> PagedResult<R> map(Function<T, R> mapper) {
        return new PagedResult<>(
                content.stream().map(mapper).toList(),
                page, size, totalElements, totalPages, isFirst, isLast
        );
    }
}
