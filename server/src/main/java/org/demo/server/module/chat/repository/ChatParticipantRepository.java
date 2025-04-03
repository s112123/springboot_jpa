package org.demo.server.module.chat.repository;

import org.demo.server.module.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    /**
     * 회원이 참여한 식별자 (chat_room_id) 목록 반환
     *
     * @param memberId 회원의 식별자
     * @return 회원이 참여한 채팅방 식별자 (chat_room_id) 목록
     */
    List<ChatParticipant> findByMember_MemberId(Long memberId);
}
