package org.demo.server.infra.mq.controller;

import lombok.RequiredArgsConstructor;
import org.demo.server.infra.common.dto.response.PagedListResponse;
import org.demo.server.infra.mq.dto.details.MessageDetails;
import org.demo.server.infra.mq.dto.response.MessageResponse;
import org.demo.server.infra.mq.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 메세지 목록 조회
     *
     * @param memberId 메세지 목록을 조회 할 회원 ID
     * @param page 현재 페이지
     * @return 메세지 목록
     */
    @GetMapping
    public ResponseEntity<PagedListResponse> findAll(
            @RequestParam("memberId") Long memberId,
            @RequestParam("page") int page
    ) {
        Page<MessageResponse> messages = messageService.findAll(memberId, page)
                .map(message -> message.toResponse());
        return ResponseEntity.ok().body(new PagedListResponse<>(messages));
    }

    /**
     * 읽지 않은 메세지 존재 여부
     *
     * @param memberId 회원 ID
     * @return 읽지 않은 메세지가 있으면 true, 없으면 false
     */
    @GetMapping("/no-read/exists")
    public ResponseEntity<Boolean> existsNotReadMessage(
            @RequestParam("memberId") Long memberId
    ) {
        return ResponseEntity.ok().body(messageService.existsNotReadMessage(memberId));
    }

    /**
     * 읽지 않은 메세지 개수
     *
     * @param memberId 메세지 개수를 조회할 회원 ID
     * @return 메세지 개수
     */
    @GetMapping("/no-read/count")
    public ResponseEntity<Long> countNotReadMessage(
            @RequestParam("memberId") Long memberId
    ) {
        return ResponseEntity.ok().body(messageService.countNotReadMessage(memberId));
    }

    /**
     * 읽음 처리
     *
     * @param notificationId 읽음으로 처리할 알림의 ID
     * @param memberId 회원 ID
     * @return Void
     */
    @PatchMapping("/{notificationId}/mark_read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable("notificationId") Long notificationId,
            @RequestParam("memberId") Long memberId
    ) {
        messageService.updateRead(memberId, notificationId);
        return ResponseEntity.ok().build();
    }

    /**
     * 모두 읽음 처리
     *
     * @param memberId 회원 ID
     * @return Void
     */
    @PatchMapping("/mark_all_read")
    public ResponseEntity<Void> markAsAllRead(
            @RequestParam("memberId") Long memberId
    ) {
        messageService.updateAllRead(memberId);
        return ResponseEntity.ok().build();
    }
}
