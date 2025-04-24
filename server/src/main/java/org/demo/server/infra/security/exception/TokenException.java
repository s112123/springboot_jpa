package org.demo.server.infra.security.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public class TokenException extends RuntimeException {

    private TokenStatus tokenStatus;

    public TokenException(TokenStatus tokenStatus) {
        super(tokenStatus.getMessage());
        this.tokenStatus = tokenStatus;
    }

    // 클라이언트에 에러 메세지 전달
    public void sendTokenErrorResponse(HttpServletResponse response) {
        response.setStatus(tokenStatus.getStatus());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // 응답 메세지
        Map<String, Object> responseBody = Map.of(
                "message", tokenStatus.getMessage(),
                "createdAt", LocalDateTime.now()
        );

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.registerModule(new JavaTimeModule())
                    .writeValueAsString(responseBody);
            response.getWriter().println(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
