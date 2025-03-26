package org.demo.server.infra.common.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PagedListResponse<T> {

    // 가져올 데이터 오프셋
    @JsonIgnore
    private int data_offset = 10;
    // 페이지 번호 섹션의 오프셋
    @JsonIgnore
    private int page_offset = 10;
    // 총 데이터 수
    private long size;
    // 데이터
    private List<T> data;
    // 현재 페이지
    private long currentPage;
    // 전체 페이지 번호에서 마지막 페이지 번호
    private long last;
    // 페이지 번호 섹션에서 시작 페이지 번호
    private long start;
    // 페이지 번호 섹션에서 마지막 페이지 번호
    private long end;
    // 이전 페이지 번호 섹션
    private boolean hasPrev;
    // 다음 페이지 번호 섹션
    private boolean hasNext;

    public PagedListResponse(Page<T> data) {
        this.size = data.getTotalElements();
        this.data = data.getContent();
        this.currentPage = data.getNumber() + 1;
        this.last = data.getTotalPages();
        this.start = ((this.currentPage - 1) / this.page_offset) * this.page_offset + 1;
        this.end = Math.min(start + (this.page_offset - 1), this.last);
        this.hasPrev = this.start > 1;
        this.hasNext = this.end < this.last;
    }
}
