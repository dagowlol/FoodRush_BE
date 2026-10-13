package com.foodrush.be.mail;

import com.foodrush.be.config.AppProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    public void sendOtp(String to, String code) {
        String fromName = appProperties.getOtp().getSenderName();
        String fromEmail = appProperties.getOtp().getSenderEmail();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromName + " <" + fromEmail + ">");
            helper.setTo(to);
            helper.setSubject("Mã xác nhận đăng ký FoodRush");
            String content = String.format(
                    "<div style=\"font-family: Arial, sans-serif; font-size: 16px;\">"
                            + "<p>Chào bạn,</p>"
                            + "<p>Mã OTP xác nhận đăng ký tài khoản FoodRush của bạn là:</p>"
                            + "<h2 style=\"color: #e63946;\">%s</h2>"
                            + "<p>Mã có hiệu lực trong %d phút. Vui lòng không chia sẻ với bất kỳ ai.</p>"
                            + "<p>Trân trọng,</p>"
                            + "<p>FoodRush Team</p>"
                            + "</div>",
                    code, appProperties.getOtp().getTtlMinutes());
            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send OTP to {}: {}", to, e.getMessage());
        }
    }
}

