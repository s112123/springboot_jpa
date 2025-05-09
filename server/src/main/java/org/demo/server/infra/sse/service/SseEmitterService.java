package org.demo.server.infra.sse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
        log.info("[1] 현재 접속 중인 SSE 사용자 수: {}", emitters.size());

        // 기존 emitter 가 있으면 종료 후, 제거
        if (emitters.containsKey(memberId)) {
            emitters.remove(memberId).complete();
        }

        // Timeout 30초
        SseEmitter emitter = new SseEmitter(1000L * 30);
        emitters.put(memberId, emitter);

        // 더미 데이터 전송
        sendEventToSubscriber(memberId, "ping");

        emitter.onTimeout(() -> emitters.remove(memberId));
        emitter.onCompletion(() -> emitters.keySet().forEach(id -> log.info("[onCompletion] 현재 접속자: {}", id)));
        emitter.onError((e) -> emitters.remove(memberId));

        log.info("[2] 현재 접속 중인 SSE 사용자 수: {}", emitters.size());
        Set<Map.Entry<Long, SseEmitter>> entries = emitters.entrySet();
        for (Map.Entry<Long, SseEmitter> entry : entries) {
            log.info("[2] 현재 접속자: {}, {}", entry.getKey(), entry.getValue());
        }
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
