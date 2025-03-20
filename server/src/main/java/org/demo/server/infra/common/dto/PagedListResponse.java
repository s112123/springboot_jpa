package org.demo.server.infra.common.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PagedListResponse<T> {

    private long size;
    private List<T> data;
    private long currentPage;
    private long totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean isFirst;
    private boolean isLast;

    public PagedListResponse(Page<T> data) {
        this.size = data.getTotalElements();
        this.data = data.getContent();
        this.currentPage = data.getNumber() + 1;
        this.totalPages = data.getTotalPages();
        this.hasNext = data.hasNext();
        this.hasPrevious = data.hasPrevious();
        this.isFirst = data.isFirst();
        this.isLast = data.isLast();
    }
}
