package org.demo.server.infra.mq.dto.details;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.demo.server.infra.mq.constant.MessageType;
import org.demo.server.infra.mq.dto.response.MessageResponse;
import org.demo.server.infra.mq.entity.Message;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@ToString
public class MessageDetails {

    private Long id;
    private Long publisherId;
    private Long consumerId;
    private MessageType type;
    private String message;
    private boolean read;
    private String url;
    private LocalDateTime createdAt;

    public MessageDetails(Message message) {
        this.id = message.getId();
        this.publisherId = message.getSenderId();
        this.consumerId = message.getConsumer().getMemberId();
        this.type = message.getType();
        this.message = message.getMessage();
        this.read = message.isRead();
        this.url = message.getUrl();
        this.createdAt = message.getCreatedAt();
    }

    /**
     * 클라이언트에 반환하는 Response 객체로 변환 (DTO → DTO)
     *
     * @return MessageResponse
     */
    public MessageResponse toResponse() {
        return MessageResponse.builder()
                .id(this.id)
                .message(this.message)
                .read(this.read)
                .url(this.url)
                .createdAt(this.createdAt)
                .build();
    }
}
