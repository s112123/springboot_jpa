package org.demo.server.module.logout.controller;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.sse.service.SseEmitterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logout")
@RequiredArgsConstructor
public class LogoutController {

    private final SseEmitterService sseEmitterService;

    @PostMapping
    public ResponseEntity<Void> logout(
            @RequestBody Long memberId
    ) {
        // SseEmitter 삭제
        sseEmitterService.removeEmitter(memberId);
        // TODO: Refresh Token 삭제
        return ResponseEntity.ok().build();
    }
}
