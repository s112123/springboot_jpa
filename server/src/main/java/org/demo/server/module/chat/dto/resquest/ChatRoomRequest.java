package org.demo.server.module.chat.dto.resquest;

import lombok.Getter;

@Getter
public class ChatRoomRequest {

    private Long to;
    private Long from;
}
