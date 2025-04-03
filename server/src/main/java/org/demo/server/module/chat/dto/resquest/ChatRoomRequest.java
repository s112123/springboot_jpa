package org.demo.server.module.chat.dto.resquest;

import lombok.Getter;

@Getter
public class ChatRoomRequest {

    // 메시지를 받는 회원의 식별자 (memberId)
    private Long to;
    // 메시지를 보내는 회원의 식별자 (memberId)
    private Long from;
}
