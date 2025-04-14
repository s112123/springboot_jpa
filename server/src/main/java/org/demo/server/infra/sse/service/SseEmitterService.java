package org.demo.server.infra.sse.service;

import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.mq.dto.details.MessageDetails;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Scope("singleton")
public class SseEmitterService {

    // SseEmitter 저장소
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 구독하기
     *
     * @param memberId 구독하는 회원 ID (= 로그인한 회원 ID)
     * @return SseEmitter
     */
    public SseEmitter subscribe(Long memberId) {
        log.info("[1] emitters.size={}", emitters.size());

        // 기존 emitter 가 있으면 종료 후, 제거
        if (emitters.containsKey(memberId)) {
            emitters.get(memberId).complete();
        }

        // Timeout 10분
        SseEmitter emitter = new SseEmitter(1000L * 10);
        emitters.put(memberId, emitter);
        log.info("[2] emitters.size={}", emitters.size());

        // 더미 데이터 전송
        sendEventToSubscriber(memberId, "ping");

        emitter.onTimeout(() -> {
            log.info("[onTimeout: delete before] emitters.size={}", emitters.size());
            emitters.remove(memberId);
            log.info("[onTimeout: delete after] emitters.size={}", emitters.size());
        });
        emitter.onCompletion(() -> {
            log.info("[onCompletion: delete before] emitters.size={}", emitters.size());
            emitters.remove(memberId);
            log.info("[onCompletion: delete after] emitters.size={}", emitters.size());
        });
        emitter.onError((e) -> emitters.remove(memberId));

        return emitter;
    }

    /**
     * 실시간 이벤트 전송
     *
     * @param memberId 이벤트를 받을 회원 ID
     * @param event 이벤트 내용
     */
    public void sendEventToSubscriber(Long memberId, String event) {
        SseEmitter emitter = emitters.get(memberId);
        if (emitter == null) {
            return;
        }

        try {
            emitter.send(SseEmitter.event().name("notification").data(event));
        } catch (IOException e) {
            log.warn("[SSE ERROR]: 이벤트 전송 실패 → {}", event);
            emitters.remove(memberId);
        }
    }

    /**
     * 온라인 유무
     *
     * @param memberId 온라인 상태인지 확인할 회원 ID
     * @return 온라인 상태이면 true, 그렇지 않으면 false
     */
    public boolean hasConnection(Long memberId) {
        return emitters.containsKey(memberId);
    }

    /**
     * SseEmitter 를 저장소에서 삭제
     *
     * @param memberId SseEmitter 를 삭제할 회원 ID
     */
    public void removeEmitter(Long memberId) {
        SseEmitter emitter = emitters.remove(memberId);
        if (emitter != null) {
            emitter.complete();
        }
    }
}
