package com.erp;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Password reset by emailed code. Public endpoints (no login), so they must resist
 * guessing the 6-digit code and being used to flood someone's inbox.
 * The mail server is replaced by a mock; the code is read from Redis.
 */
class PasswordResetTest extends IntegrationTestBase {

    private static final String WRONG_OR_EXPIRED = "Verification code error or expired";

    @MockitoBean
    JavaMailSender mailSender;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void fakeMailServer() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
    }

    @Test
    void codeIsSixRandomDigitsAndResetWorks() throws Exception {
        String email = "admin@example.com";
        String originalHash = passwordHash("admin");
        try {
            sendCode(email).andExpect(jsonPath("$.code").value(1));
            String code = codeInRedis(email);
            assertThat(code).matches("\\d{6}");

            reset(email, code, "NewPassw0rd").andExpect(jsonPath("$.code").value(1));
            // the code is single-use
            reset(email, code, "NewPassw0rd").andExpect(jsonPath("$.msg").value(WRONG_OR_EXPIRED));
        } finally {
            // other tests log in with the seed password
            jdbcTemplate.update("update employee set password = ? where username = 'admin'", originalHash);
        }
    }

    @Test
    void fiveWrongGuessesInvalidateTheCode() throws Exception {
        String email = "adam@example.com";
        String hashBefore = passwordHash("adam");
        sendCode(email).andExpect(jsonPath("$.code").value(1));
        String code = codeInRedis(email);
        String wrong = code.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < 5; i++) {
            reset(email, wrong, "Guessed123").andExpect(jsonPath("$.msg").value(WRONG_OR_EXPIRED));
        }

        // without a limit, 6 digits / 5 minutes can be brute-forced; now the real code is dead too
        reset(email, code, "Guessed123").andExpect(jsonPath("$.msg").value(WRONG_OR_EXPIRED));
        assertThat(passwordHash("adam")).isEqualTo(hashBefore);
    }

    @Test
    void cannotRequestCodesBackToBack() throws Exception {
        String email = "manager@example.com";
        sendCode(email).andExpect(jsonPath("$.code").value(1));

        sendCode(email)
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Please wait a minute before requesting another code"));
    }

    private ResultActions sendCode(String email) throws Exception {
        return mockMvc.perform(post("/employee/info/send-email-code")
                .contentType(APPLICATION_JSON).content(json(Map.of("email", email))));
    }

    private ResultActions reset(String email, String code, String newPassword) throws Exception {
        return mockMvc.perform(post("/employee/info/reset-password")
                .contentType(APPLICATION_JSON).content(json(Map.of(
                        "email", email, "verifyCode", code,
                        "newPassword", newPassword, "confirmPassword", newPassword))));
    }

    private String codeInRedis(String email) {
        return redisTemplate.opsForValue().get("email:reset:" + email);
    }

    private String passwordHash(String username) {
        return jdbcTemplate.queryForObject("select password from employee where username = ?", String.class, username);
    }
}
