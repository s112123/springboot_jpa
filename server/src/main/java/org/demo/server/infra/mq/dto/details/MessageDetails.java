package org.demo.server.infra.mq.dto.details;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.demo.server.infra.mq.entity.Message;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@ToString
public class MessageDetails {

    private Long id;
    private Long consumerId;
    private java.lang.String message;
    private boolean read;
    private LocalDateTime createdAt;

    public MessageDetails(Message message) {
        this.id = message.getId();
        this.consumerId = message.getConsumer().getMemberId();
        this.message = message.getMessage();
        this.read = message.isRead();
        this.createdAt = message.getCreatedAt();
    }
}
