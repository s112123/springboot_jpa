package org.demo.server.infra.socket.interceptor;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.infra.security.util.JwtUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCheckInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 메세지의 헤더 접근
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        // Access Token 이 있는지 여부
        List<String> authorization = accessor.getNativeHeader("Authorization");

        if (authorization != null && !authorization.isEmpty()) {
            // 토큰 값
            String accessToken = authorization.get(0);

            // 웹 소켓에 구독할 때 → StompCommand.CONNECT.equals(accessor.getCommand())
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                if (!checkAccessToken(accessToken)) {
                    log.error("WebSocket - Invalid Token");
                    throw new RuntimeException("Invalid Token");
                }
            }

            // 메시지를 전송할 때 → StompCommand.SEND.equals(accessor.getCommand())
            if (StompCommand.SEND.equals(accessor.getCommand())) {
                if (!checkAccessToken(accessToken)) {
                    log.error("WebSocket - Invalid Token");
                    throw new RuntimeException("Invalid Token");
                }
            }

        }

        // null 을 반환하면 클라이언트에 메세지를 전송하지 않는다
        return message;
    }

    // 토큰 검증
    private boolean checkAccessToken(String token) {
        try {
            Claims claims = jwtUtils.validate(token);
            return claims != null;
        } catch (Exception e) {
            return false;
        }
    }
}
