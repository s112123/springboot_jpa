package org.demo.server.infra.mq.service.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.mq.config.MQConfig;
import org.demo.server.infra.mq.dto.details.MessageDetails;
import org.demo.server.infra.sse.service.SseEmitterService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageConsumer {

    @Qualifier("redisTemplate01")
    private final RedisTemplate<java.lang.String, Object> redisTemplate;
    private final SseEmitterService sseEmitterService;

    /**
     * 좋아요 클릭 시, 발생한 알림 메세지 수신
     *
     * @param messageDetails 메세지 내용
     */
    @RabbitListener(queues = MQConfig.QUEUE_LIKE)
    public void consumeLike(MessageDetails messageDetails) {
        log.info("[LIKE]: {}", messageDetails);

        // 메세지 저장 → Redis (Set)
        redisTemplate.opsForSet().add("notification:consumer:" + messageDetails.getConsumerId(), messageDetails);

        // 메세지를 받을 회원이 온라인 상태이면 SSE 로 실시간 알림 전송
        if (sseEmitterService.hasConnection(messageDetails.getConsumerId())) {
            sseEmitterService.sendEventToSubscriber(messageDetails.getConsumerId(), messageDetails.getMessage());
        }
    }

    /**
     * 구독하기 클릭 시, 발생한 알림 메세지 수신
     *
     * @param messageDetails 메세지 내용
     */
    @RabbitListener(queues = MQConfig.QUEUE_FOLLOW)
    public void consumeFollow(MessageDetails messageDetails) {
        log.info("[FOLLOW]: {}", messageDetails);

        // 메세지 저장 → Redis (Set)
        redisTemplate.opsForSet().add("notification:consumer:" + messageDetails.getConsumerId(), messageDetails);

        // 메세지를 받을 회원이 온라인 상태이면 SSE 로 실시간 알림 전송
        if (sseEmitterService.hasConnection(messageDetails.getConsumerId())) {
            sseEmitterService.sendEventToSubscriber(messageDetails.getConsumerId(), messageDetails.getMessage());
        }
    }

    // 새 글 알림
    @RabbitListener(queues = MQConfig.QUEUE_POST)
    public void consumePost(MessageDetails messageDetails) {
        log.info("[POST]: {}", messageDetails);

        // 메세지 저장 → Redis (Set)
        redisTemplate.opsForSet().add("notification:consumer:" + messageDetails.getConsumerId(), messageDetails);

        // 메세지를 받을 회원이 온라인 상태이면 SSE 로 실시간 알림 전송
        if (sseEmitterService.hasConnection(messageDetails.getConsumerId())) {
            sseEmitterService.sendEventToSubscriber(messageDetails.getConsumerId(), messageDetails.getMessage());
        }
    }
}
