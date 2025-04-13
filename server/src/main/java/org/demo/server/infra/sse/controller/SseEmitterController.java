package org.demo.server.infra.sse.controller;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.sse.service.SseEmitterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
public class SseEmitterController {

    private final SseEmitterService sseEmitterService;

    // 구독하기
    @GetMapping("/subscribe")
    public ResponseEntity<SseEmitter> subscribe(
            @RequestParam("memberId") Long memberId
    ) {
        return ResponseEntity.ok().body(sseEmitterService.subscribe(memberId));
    }
}
