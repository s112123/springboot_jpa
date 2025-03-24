package org.demo.server.module.member.util.send.base;

public interface SendUtils {

    /**
     * 인증 코드 발송
     * 
     * @param to 인증 코드를 보낼 대상
     * @return 인증 코드
     */
    String send(String to);
}
