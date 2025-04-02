package org.demo.server.module.chat.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Message {

    private String to;
    private String from;
    private String message;
}
