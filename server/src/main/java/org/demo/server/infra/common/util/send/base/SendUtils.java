package org.demo.server.infra.common.util.send.base;

public abstract class SendUtils {

    /**
     * 인증 코드 (6자리) 발송
     * 
     * @param to 인증 코드를 보낼 대상
     * @return 인증 코드
     */
    public abstract String sendConfirmationCode(String to);
}
