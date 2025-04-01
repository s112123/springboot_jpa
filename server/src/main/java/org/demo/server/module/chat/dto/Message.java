package org.demo.server.module.chat.dto;

import lombok.Getter;

@Getter
public class Message {

    private String to;
    private String from;
    private String message;
}
