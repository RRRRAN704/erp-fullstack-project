package com.erp.service.impl;

import com.erp.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${erp.mail.from-email}")
    private String fromEmail;

    private static final String EMAIL_CODE_PREFIX = "email:reset:";
    private static final int CODE_EXPIRE_TIME = 5; // 5 minutes

    @Override
    public boolean sendResetPasswordCode(String email) {
        try {
            // 1. generate codes
            String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

            // 2. build html
            String subject = "Password Reset Code";
            String htmlContent = buildEmailContent(code);

            // 3. send email via smtp
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            // 4) store code in Redis with expiry time
            String key = EMAIL_CODE_PREFIX + email;
            redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_TIME, TimeUnit.MINUTES);
            log.info("Brevo SMTP email sent success, email: {}, code: {}", email, code);
            return true;

        } catch (Exception e) {
            log.error("Brevo SMTP email send failed, email: {}, error message: {}", email, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean verifyEmailCode(String email, String code) {
        try {
            String key = EMAIL_CODE_PREFIX + email;
            String savedCode = redisTemplate.opsForValue().get(key);

            if (savedCode != null && savedCode.equals(code)) {
                // delete code after verficiation code has been sent
                redisTemplate.delete(key);
                log.info("Code verified successfully, email: {}", email);
                return true;
            }

            log.warn("Code verification failed, email: {}, code: {}, saved code: {}",
                    email, code, savedCode);
            return false;

        } catch (Exception e) {
            log.error("exception occurred, email: {}, error message: {}", email, e.getMessage());
            return false;
        }
    }

    /**
     * construct http contents
     * @param code verification cpde
     * @return HTML content
     */
    private String buildEmailContent(String code) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<div style='padding: 20px; font-family: Arial, sans-serif;'>");
        content.append("<h2 style='color: #333;'>Password Reset Verification Code</h2>");
        content.append("<p>Hello,</p>");
        content.append("<p>Your verification code is:</p>");
        content.append("<div style='background-color: #f0f0f0; padding: 20px; margin: 20px 0; text-align: center;'>");
        content.append("<span style='font-size: 32px; font-weight: bold; color: #007bff; letter-spacing: 5px;'>")
                .append(code).append("</span>");
        content.append("</div>");
        content.append("<p>The verification code is valid for 5 minutes.</p>");
        content.append("<p>If you didn't request this code, please ignore this email.</p>");
        content.append("</div>");
        content.append("</body></html>");
        return content.toString();
    }
}