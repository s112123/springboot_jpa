package org.demo.server.infra.mq.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageResponse {

    private Long id;
    private String message;
    private boolean read;
    private String url;
    private LocalDateTime createdAt;
}
