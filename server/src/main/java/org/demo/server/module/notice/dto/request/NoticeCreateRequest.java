package org.demo.server.module.notice.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NoticeCreateRequest {

    // 작성자의 ID
    private Long writerId;
    private String content;
}
