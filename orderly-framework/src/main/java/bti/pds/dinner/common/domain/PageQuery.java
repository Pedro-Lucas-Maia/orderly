package bti.pds.dinner.common.domain;

public record PageQuery(int page, int size) {
    public PageQuery {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
    }
}
