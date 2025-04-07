package org.demo.server.module.chat.repository;

import org.demo.server.module.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 채팅 메세지 조회
     *
     * @param chatRoomId 메세지를 조회할 채팅방 식별자
     * @return 메세지 목록
     */
    List<ChatMessage> findByChatRoom_ChatRoomId(Long chatRoomId);
}
