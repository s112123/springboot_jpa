package org.demo.server.infra.mq.repository;

import org.demo.server.infra.mq.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 메세지 내역 목록
     *
     * @param memberId 메세지 내역을 조회하는 회원 ID
     * @return 메세지 내역 목록
     */
    List<Message> findByConsumer_MemberId(Long memberId);

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
}
