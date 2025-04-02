package org.demo.server.module.chat.repository;

import org.demo.server.module.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    Optional<ChatParticipant> findByMember_MemberId(Long memberId);
}
