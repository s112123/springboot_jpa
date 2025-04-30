package org.demo.server.module.member.util.send.impl.password.impl;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.demo.server.module.member.service.base.MemberService;
import org.demo.server.module.member.util.send.impl.password.base.SendTempPassword;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Slf4j
// @Profile(value = "prod")
@Profile(value = "pass - 이메일을 발송하려면 실제 이메일이 필요하므로 원래는 prod 로 해야 한다")
@Component
@RequiredArgsConstructor
public class SendTempPasswordEmail extends SendTempPassword {

    private final MemberService memberService;
    private final JavaMailSender javaMailSender;
    
    /**
     * 임시 비밀번호를 이메일로 발송하고 비밀번호 변경
     *
     * @param to 인증 코드를 보낼 대상
     */
    @Override
    public String send(String to) {
        // 임시 비밀번호 (20자리)
        String tempPassword = UUID.randomUUID().toString().substring(0, 20);
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            // 받는 사람
            message.addRecipients(Message.RecipientType.TO, to);
            // 보내는 사람
            message.setFrom(new InternetAddress("v011235@naver.com", "Today's Review"));
            // 메일 제목
            message.setSubject("[Today's Review] 임시 비밀번호입니다");
            // 메일 내용
            String html = "";
            html += "<div style=\"width:350px; margin:50px 0px; padding:20px; border:1px solid rgb(210, 210, 210); " +
                    "             border-radius:5px;\">";
            html += "  <p style=\"margin-bottom:25px; font-size:16px; font-weight:bold;\">Today's Review</p>";
            html += "  <p style=\"padding:15px 0px; font-size:16px; font-weight:bold; border-bottom:1px solid rgb(210, 210, 210);\">";
            html += "    [임시 비밀번호]</p>";
            html += "  <p style=\"padding:40px 0px; font-size:24px; font-weight:bold; text-align:center;\">" +
                        tempPassword + "</p>";
            html += "  <p style=\"margin-bottom:25px; font-size:14px; text-align:center;\">";
            html += "    로그인 후, 비밀번호를 변경하시길 바랍니다</p>";
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

        // 회원 비밀번호를 임시 비밀번호로 변경
        memberService.updateTempPasswordByEmail(to, tempPassword);
        return "이메일로 임시 비밀번호가 발송되었습니다";
    }
}
