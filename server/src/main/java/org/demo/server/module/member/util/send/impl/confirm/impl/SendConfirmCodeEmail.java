package org.demo.server.module.member.util.send.impl.confirm.impl;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.module.member.util.send.impl.confirm.base.SendConfirmCode;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Slf4j
// @Profile(value = "prod")
@Profile(value = "pass - 이메일을 발송하려면 실제 이메일이 필요하므로 원래는 prod 로 해야 한다")
@Component
public class SendConfirmCodeEmail extends SendConfirmCode {

    private final JavaMailSender javaMailSender;

    public SendConfirmCodeEmail(
            RedisTemplate<String, String> redisTemplate,
            JavaMailSender javaMailSender
    ) {
        super(redisTemplate);
        this.javaMailSender = javaMailSender;
    }

    /**
     * 인증 코드를 이메일로 발송
     *
     * @param to 인증 코드를 보낼 이메일
     */
    @Override
    public String send(String to) {
        // 인증 코드
        String code = UUID.randomUUID().toString().substring(0, 6);
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            // 받는 사람
            message.addRecipients(Message.RecipientType.TO, to);
            // 보내는 사람
            message.setFrom(new InternetAddress("v011235@naver.com", "Today's Review"));
            // 메일 제목
            message.setSubject("[Today's Review] 인증 메일 발송");
            // 메일 내용
            String html = "";
            html += "<div style=\"width:350px; margin:50px 0px; padding:20px; border:1px solid rgb(210, 210, 210); " +
                    "             border-radius:5px;\">";
            html += "  <p style=\"margin-bottom:25px; font-size:16px; font-weight:bold;\">Today's Review</p>";
            html += "  <p style=\"padding:15px 0px; font-size:16px; font-weight:bold; border-bottom:1px solid rgb(210, 210, 210);\">";
            html += "    [인증 코드]</p>";
            html += "  <p style=\"padding:40px 0px; font-size:24px; font-weight:bold; text-align:center;\">" +
                         code + "</p>";
            html += "</div>";
            message.setText(html, "UTF-8", "html");

            // 메일 발송
            javaMailSender.send(message);
        } catch (MessagingException e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }

        return "이메일로 인증코드가 발송되었습니다";
    }
}
