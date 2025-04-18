package org.demo.server.infra.mq.repository;

import org.demo.server.infra.mq.entity.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 메세지 내역 목록
     *
     * @param memberId 메세지 내역을 조회하는 회원 ID
     * @return 메세지 내역 목록
     */
    Page<Message> findByConsumer_MemberId(Long memberId, Pageable pageable);

    /**
     * 읽지 않은 메세지 내역 목록
     *
     * @param memberId 메세지 내역을 조회하는 회원 ID
     * @return 읽지 않은 메세지 내역 목록
     */
    List<Message> findByConsumer_MemberIdAndReadFalse(Long memberId);

    /**
     * 안 읽은 메세지가 있는지 여부
     *
     * @param memberId 회원 ID
     * @return 읽지 않은 메세지가 있으면 true, 없으면 false
     */
    boolean existsByConsumer_MemberIdAndReadFalse(Long memberId);

    /**
     * 읽지 않은 메세지 개수
     *
     * @param memberId 메세지 개수를 조회할 회원 ID
     * @return 메세지 개수
     */
    long countByConsumer_MemberIdAndReadFalse(Long memberId);

    /**
     * 모두 읽음 처리
     *
     * @param memberId 알림을 모두 읽음 처리할 회원의 ID
     */
    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.consumer.memberId = :memberId")
    void markAllAsRead(@Param("memberId") Long memberId);
}
