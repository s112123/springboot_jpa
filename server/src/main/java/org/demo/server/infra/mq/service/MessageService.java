package org.demo.server.infra.mq.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.demo.server.infra.mq.dto.details.MessageDetails;
import org.demo.server.infra.mq.entity.Message;
import org.demo.server.infra.mq.repository.MessageRepository;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    @Qualifier("redisTemplate01")
    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageRepository messageRepository;
    private final MemberFinder memberFinder;
    // 스프링이 관리하는 ObjectMapper 에는 JavaTimeModule 이 등록되어 있다
    private final ObjectMapper objectMapper;

    /**
     * 메세지 저장
     *
     * @param message 메세지 내용
     * @return 저장된 메세지
     */
    @Transactional
    public MessageDetails save(Long consumerId, String message) {
        // 메세지 받는 사람 조회
        Member consumer = memberFinder.getMemberById(consumerId);

        // 메세지 저장
        Message savedMessage = messageRepository.save(Message.builder()
                .consumer(consumer)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build());
        return new MessageDetails(savedMessage);
    }

    /**
     * 전체 메세지 목록 조회
     *
     * @param consumerId 메세지 내역을 조회하는 회원 ID
     * @return 메세지 내역 목록
     */
    @Transactional(readOnly = true)
    public List<MessageDetails> findAll(Long consumerId) {
        return messageRepository.findByConsumer_MemberId(consumerId).stream()
                .map(message -> new MessageDetails(message))
                .collect(Collectors.toList());
    }

    /**
     * 읽지 않은 메세지 목록 조회
     *
     * @param consumerId 메세지 내역을 조회하는 회원 ID
     * @return 메세지 내역 목록
     */
    @Transactional(readOnly = true)
    public List<MessageDetails> findAllNotRead(Long consumerId) {
        // Redis 에 캐시된 것이 확인
        String redisKey = "notification:consumer:" + consumerId;
        if (redisTemplate.hasKey(redisKey)) {
            Set<Object> elements =
                    redisTemplate.opsForSet().members("notification:consumer:" + consumerId);
            List<MessageDetails> messageDetailsList = elements.stream()
                    .map(element -> objectMapper.convertValue(element, MessageDetails.class))
                    .collect(Collectors.toList());
            return messageDetailsList;
        }

        // Redis 에 없으면, RDB 에서 있는지 확인
        return messageRepository.findByConsumer_MemberIdAndReadFalse(consumerId).stream()
                .map(message -> new MessageDetails(message))
                .collect(Collectors.toList());
    }

    /**
     * 안 읽은 메세지가 있는지 여부
     *
     * @param consumerId 메세지를 받은 회원 ID
     * @return 읽지 않은 메세지가 있으면 true, 없으면 false
     */
    @Transactional(readOnly = true)
    public boolean existsNotReadMessage(Long consumerId) {
        // Redis 에 캐시된 것이 확인하고 있으면 true
        String redisKey = "notification:consumer:" + consumerId;
        if (redisTemplate.hasKey(redisKey)) {
            return true;
        }

        // Redis 에 없으면, RDB 에서 있는지 확인
        return messageRepository.existsByConsumer_MemberIdAndReadFalse(consumerId);
    }

    /**
     * 메세지 읽음 처리
     *
     * @param messageId 읽음 처리할 메세지 ID
     */
    @Transactional
    public void updateRead(Long messageId) {
        messageRepository.findById(messageId)
                .ifPresent((message) -> message.setRead(true));
    }
}
