package org.demo.server.module.chat.repository;

import org.demo.server.module.chat.entity.ChatMessageRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageReadRepository extends JpaRepository<ChatMessageRead, Long> {

    /**
     * 안 읽은 메세지가 있는지 여부
     * 만약, 회원 ID 가 1인 회원이 읽지 않은 메세지를 조회하려면 receiverId 에 1, senderId 에 다른 회원 ID 를 넣는다
     *
     * @param senderId 메세지를 보낸 회원 ID
     * @param receiverId 메세지를 받은 회원 ID
     * @return 읽지 않은 메세지가 있는지 여부
     */
    boolean existsBySenderIdAndReceiverIdAndReadFalse(Long senderId, Long receiverId);

    /**
     * 모두 읽음 처리
     * 읽음 여부 데이터는 1회용이고 DB 에 데이터가 많이 저장되므로 읽은 경우 삭제 처리를 한다
     *
     * @param senderId 메세지를 보낸 사람
     * @param receiverId 메세지를 받은 사람
     */
    @Modifying
    @Query("DELETE FROM ChatMessageRead cmr WHERE cmr.senderId = :senderId AND cmr.receiverId = :receiverId")
    void markAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}
