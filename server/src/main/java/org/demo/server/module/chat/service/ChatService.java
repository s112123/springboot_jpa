package org.demo.server.module.chat.service;

import lombok.RequiredArgsConstructor;
import org.demo.server.module.chat.dto.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅 대상자에게 메세지 전송
     *
     * @param message 전송할 메세지 정보
     */
    @Transactional
    public void sendMessage(Message message) {
        // 메세지를 데이터베이스에 저장

        // 채팅 대상자에게 메세지 전송
        messagingTemplate.convertAndSendToUser(message.getTo(), "/chat/subscribe", message.getMessage());
    }
}
