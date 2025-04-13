package org.demo.server.infra.mq.controller;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.mq.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 읽지 않은 메세지 목록 조회
     *
     * @param memberId 회원 ID
     * @return 읽지 않은 메세지 목록
     */
    @GetMapping
    public ResponseEntity<Boolean> existsNotReadMessage(
            @RequestParam("memberId") Long memberId
    ) {
        return ResponseEntity.ok().body(messageService.existsNotReadMessage(memberId));
    }
}
