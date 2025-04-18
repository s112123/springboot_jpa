package org.demo.server.infra.mq.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.mq.constant.MessageType;
import org.demo.server.infra.mq.dto.details.MessageDetails;
import org.demo.server.infra.mq.entity.Message;
import org.demo.server.infra.mq.repository.MessageRepository;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.member.service.base.MemberFinder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageRepository messageRepository;
    private final MemberFinder memberFinder;
    // 스프링이 관리하는 ObjectMapper 에는 JavaTimeModule 이 등록되어 있다
    private final ObjectMapper objectMapper;

    public MessageService(
            @Qualifier("redisTemplate02") RedisTemplate<String, Object> redisTemplate,
            MessageRepository messageRepository,
            MemberFinder memberFinder,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.messageRepository = messageRepository;
        this.memberFinder = memberFinder;
        this.objectMapper = objectMapper;
    }

    /**
     * 메세지 저장
     *
     * @param message 메세지 내용
     * @return 저장된 메세지
     */
    @Transactional
    public MessageDetails save(MessageType type, Long consumerId, String message, String url) {
        // 메세지 받는 사람 조회
        Member consumer = memberFinder.getMemberById(consumerId);

        // 메세지 저장
        Message savedMessage = messageRepository.save(Message.builder()
                .type(type)
                .consumer(consumer)
                .message(message)
                .read(false)
                .url(url)
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
    public Page<MessageDetails> findAll(Long consumerId, int page) {
        // 요청 페이지 번호가 1보다 작으면 에러가 발생한다
        if (page < 1) {
            throw new IllegalArgumentException("존재하지 않는 페이지입니다");
        }

        // 전체 메세지 목록 조회
        Pageable pageable = PageRequest.of((page - 1), 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        return messageRepository.findByConsumer_MemberId(consumerId, pageable)
                .map(message -> new MessageDetails(message));
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
     * @param notificationId 읽음 처리할 메세지 ID
     */
    @Transactional
    public void updateRead(Long memberId, Long notificationId) {
        // Redis 에서 캐시된 알림 메세지 삭제
        String redisKey = "notification:consumer:" + memberId;
        if (redisTemplate.hasKey(redisKey)) {
            Set<Object> notifications = redisTemplate.opsForSet().members(redisKey);
            notifications.forEach(notification -> {
                MessageDetails messageDetails = objectMapper.convertValue(notification, MessageDetails.class);

                // Set 구조에서 일치하는 요소 삭제
                if (messageDetails.getId().equals(notificationId)) {
                    log.info("redis memberDetails={}", messageDetails);
                    redisTemplate.opsForSet().remove(redisKey, notification);
                }
            });
        }

        // RDB 에서 읽음 처리
        messageRepository.findById(notificationId)
                .ifPresent((message) -> message.setRead(true));
    }

    /**
     * 모든 알림을 읽음 처리
     *
     * @param memberId
     */
    @Transactional
    public void updateAllRead(Long memberId) {
        // Redis 에 캐시된 알림 메세지 삭제
        String redisKey = "notification:consumer:" + memberId;
        if (redisTemplate.hasKey(redisKey)) {
            redisTemplate.delete(redisKey);
        }

        // RDB 에서 모두 읽음 처리
        messageRepository.markAllAsRead(memberId);
    }

    /**
     * 읽지 않은 메세지 개수
     *
     * @param memberId 메세지 개수를 조회할 회원 ID
     * @return 메세지 개수
     */
    @Transactional
    public Long countNotReadMessage(Long memberId) {
        return messageRepository.countByConsumer_MemberIdAndReadFalse(memberId);
    }

}
