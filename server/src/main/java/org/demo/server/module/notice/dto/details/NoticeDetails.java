package org.demo.server.module.notice.dto.details;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.demo.server.module.member.entity.Member;
import org.demo.server.module.notice.dto.response.NoticeResponse;
import org.demo.server.module.notice.entity.Notice;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class NoticeDetails {

    private Long id;
    private Member writer;
    private String content;
    private LocalDateTime createdAt;

    public NoticeDetails(Notice notice) {
        this.id = notice.getId();
        this.writer = notice.getWriter();
        this.content = notice.getContent();
        this.createdAt = notice.getCreatedAt();
    }

    /**
     * NoticeController 에서 응답할 NoticeResponse 객체 생성 (DTO → DTO)
     *
     * @return NoticeResponse
     */
    public NoticeResponse toResponse() {
        return NoticeResponse.builder()
                .id(this.id)
                .writer(this.writer.getUsername())
                .content(this.content)
                .createdAt(this.createdAt)
                .build();
    }
}
