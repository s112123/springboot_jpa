package org.demo.server.module.chat.controller;

import lombok.RequiredArgsConstructor;
import org.demo.server.module.chat.dto.resquest.ChatRoomRequest;
import org.demo.server.module.chat.entity.ChatRoom;
import org.demo.server.module.chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 채팅방 생성
    @PostMapping("/rooms")
    public ResponseEntity<?> createChatRoom(@RequestBody ChatRoomRequest chatRoomRequest) {
        chatService.createChatRoom(chatRoomRequest);
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/rooms")
    public List<ChatRoom> findAll() {
        return chatService.findAll();
    }
}
