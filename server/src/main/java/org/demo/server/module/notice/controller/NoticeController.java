package org.demo.server.module.notice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.common.dto.response.PagedListResponse;
import org.demo.server.module.notice.dto.request.NoticeCreateRequest;
import org.demo.server.module.notice.dto.response.NoticeResponse;
import org.demo.server.module.notice.service.NoticeService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지 등록
     *
     * @param request 공지 정보
     * @return Void
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<NoticeResponse> addNotice(
            @RequestBody NoticeCreateRequest request
    ) {
        NoticeResponse response = noticeService.save(request).toResponse();
        return ResponseEntity.ok().body(response);
    }

    /**
     * 공지 목록
     *
     * @param page 목록 조회 페이지
     * @return 공지 목록
     */
    @GetMapping("/pages/{page}")
    public ResponseEntity<PagedListResponse<NoticeResponse>> getNoticeList(
            @PathVariable("page") int page
    ) {
        Page<NoticeResponse> findNotices = noticeService.findAll(page)
                .map(noticeDetails -> noticeDetails.toResponse());
        return ResponseEntity.ok().body(new PagedListResponse<>(findNotices));
    }
}
