package org.demo.server.module.notice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NoticeResponse {

    private Long id;
    private String writer;
    private String content;
    private LocalDateTime createdAt;
}
