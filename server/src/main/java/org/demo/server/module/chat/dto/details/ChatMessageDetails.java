package org.demo.server.module.chat.dto.details;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.server.module.chat.entity.ChatMessage;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ChatMessageDetails {

    private String message;
    private Long memberId;
    private LocalDateTime createdAt;

    public ChatMessageDetails(ChatMessage chatMessage) {
        this.message = chatMessage.getMessage();
        this.memberId = chatMessage.getMember().getMemberId();
        this.createdAt = chatMessage.getCreatedAt();
    }
}
