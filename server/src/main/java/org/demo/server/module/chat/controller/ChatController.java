package org.demo.server.module.chat.controller;

import lombok.RequiredArgsConstructor;
import org.demo.server.module.chat.dto.details.ChatMemberDetails;
import org.demo.server.module.chat.dto.details.ChatMessageDetails;
import org.demo.server.module.chat.dto.resquest.ChatRoomRequest;
import org.demo.server.module.chat.entity.ChatMessage;
import org.demo.server.module.chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 채팅 대상자 목록 (= 내가 구독한 사람 목록)
     *
     * @param memberId 회원의 ID
     * @return 채팅 대상자 목록
     */
    @GetMapping("/follows")
    public ResponseEntity<List<ChatMemberDetails>> findFollows(
            @RequestParam("memberId") Long memberId
    ) {
        List<ChatMemberDetails> chatMembers = chatService.getChatMembers(memberId);
        return ResponseEntity.ok().body(chatMembers);
    }

    /**
     * 채팅방 참여하고 채팅방 메세지 목록 가져오기
     *
     * @param chatRoomRequest 채팅방 참여자의 식별자
     * @Return 채팅 메시지 목록
     */
    @PostMapping("/rooms")
    public ResponseEntity<List<ChatMessageDetails>> joinChatRoom(
            @RequestBody ChatRoomRequest chatRoomRequest
    ) {
        List<ChatMessageDetails> chatMessages = chatService.joinChatRoom(chatRoomRequest);
        return ResponseEntity.ok().body(chatMessages);
    }

    /**
     * 채팅방 나가기
     *
     * @param memberId 채팅방에서 나가는 회원 ID
     * @return Void
     */
    @DeleteMapping("/unjoin")
    public ResponseEntity<Void> exitChatRoom(
            @RequestParam("memberId") Long memberId
    ) {
        chatService.exitChatRoom(memberId);
        return ResponseEntity.ok().build();
    }
}
