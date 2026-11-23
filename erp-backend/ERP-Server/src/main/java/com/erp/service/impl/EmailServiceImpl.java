package com.erp.service.impl;

import com.erp.constant.MessageConstant;
import com.erp.exception.TooManyRequestsException;
import com.erp.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;

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
    // wrong guesses allowed per code: without a limit, 6 digits in 5 minutes can be brute-forced
    private static final String ATTEMPTS_PREFIX = "email:reset:attempts:";
    private static final int MAX_ATTEMPTS = 5;
    // minimum gap between two codes for one address: the endpoint is public and sends email
    private static final String COOLDOWN_PREFIX = "email:reset:cooldown:";
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    // Math.random() is predictable; codes guard password resets
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public boolean sendResetPasswordCode(String email) {
        // setIfAbsent = Redis SET NX: only the first request in the cooldown window gets through
        Boolean firstRequest = redisTemplate.opsForValue()
                .setIfAbsent(COOLDOWN_PREFIX + email, "1", RESEND_COOLDOWN);
        if (!Boolean.TRUE.equals(firstRequest)) {
            throw new TooManyRequestsException(MessageConstant.CODE_REQUESTED_TOO_OFTEN);
        }
        try {
            // 1. generate codes
            String code = String.format("%06d", RANDOM.nextInt(1_000_000));

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
            redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(CODE_EXPIRE_TIME));
            redisTemplate.delete(ATTEMPTS_PREFIX + email); // a new code gets fresh attempts
            log.info("Password reset code sent, email: {}", email);
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

            if (savedCode == null) {
                return false;
            }
            if (savedCode.equals(code)) {
                // single use: delete the code once it has been used
                redisTemplate.delete(key);
                redisTemplate.delete(ATTEMPTS_PREFIX + email);
                log.info("Code verified successfully, email: {}", email);
                return true;
            }

            // count wrong guesses; after MAX_ATTEMPTS the code is deleted and a new one is needed
            String attemptsKey = ATTEMPTS_PREFIX + email;
            Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
            redisTemplate.expire(attemptsKey, Duration.ofMinutes(CODE_EXPIRE_TIME));
            if (attempts != null && attempts >= MAX_ATTEMPTS) {
                redisTemplate.delete(key);
                redisTemplate.delete(attemptsKey);
                log.warn("Too many wrong codes, code invalidated, email: {}", email);
            } else {
                log.warn("Code verification failed, email: {}", email);
            }
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