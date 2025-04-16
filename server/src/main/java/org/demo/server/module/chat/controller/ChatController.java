package org.demo.server.module.chat.controller;

import lombok.RequiredArgsConstructor;
import org.demo.server.module.chat.dto.details.ChatMessageDetails;
import org.demo.server.module.chat.dto.resquest.ChatRoomRequest;
import org.demo.server.module.chat.entity.ChatMessage;
import org.demo.server.module.chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 채팅방 참여하고 채팅방 메세지 목록 가져오기
     *
     * @param chatRoomRequest 채팅방 참여자의 식별자
     * @Return 채팅 메시지 목록
     */
    @PostMapping("/rooms")
    public ResponseEntity<List<ChatMessageDetails>> joinChatRoom(@RequestBody ChatRoomRequest chatRoomRequest) {
        List<ChatMessageDetails> chatMessages = chatService.joinChatRoom(chatRoomRequest);
        return ResponseEntity.ok().body(chatMessages);
    }
}
