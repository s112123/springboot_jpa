package org.demo.server.module.chat.controller;

import lombok.RequiredArgsConstructor;
import org.demo.server.module.chat.dto.resquest.ChatRoomRequest;
import org.demo.server.module.chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 채팅방 참여
     *
     * @param chatRoomRequest 채팅방 참여자의 식별자
     * @return Void
     */
    @PostMapping("/rooms")
    public ResponseEntity<Void> joinChatRoom(@RequestBody ChatRoomRequest chatRoomRequest) {
        chatService.joinChatRoom(chatRoomRequest);
        return ResponseEntity.ok().build();
    }
}
