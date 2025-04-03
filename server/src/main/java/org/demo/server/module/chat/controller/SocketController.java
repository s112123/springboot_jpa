package org.demo.server.module.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.module.chat.dto.Message;
import org.demo.server.module.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SocketController {

    private final ChatService chatService;

    /**
     * 채팅 대상자에게 메세지 전송
     *
     * @param message 전송할 메세지 정보
     */
    @MessageMapping("/chat/message/send")
    public void publishMessage(
            @Payload Message message
    ) {
        // 채팅 대상자에게 메세지를 전송
        chatService.sendMessage(message);
    }
}
