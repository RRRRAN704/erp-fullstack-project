package com.erp;

import com.erp.constant.JwtClaimsConstant;
import com.erp.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Login and the JWT interceptor.
 * Note: business errors (wrong password etc.) come back as HTTP 200 with code 0 and a message,
 * via GlobalExceptionHandler; only a missing or invalid token gives HTTP 401.
 */
class AuthTest extends IntegrationTestBase {

    // Any admin-only endpoint that just reads data, so these tests change nothing.
    private static final String ADMIN_ONLY_PAGE = "/employee/department/page?page=1&pageSize=10";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void loginWithCorrectPasswordReturnsTokenAndRole() throws Exception {
        mockMvc.perform(post("/employee/info/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.position").value("System Admin"))
                .andExpect(jsonPath("$.data.token", not(emptyOrNullString())));
    }

    @Test
    void loginWithWrongPasswordFails() throws Exception {
        mockMvc.perform(post("/employee/info/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Wrong password"));
    }

    @Test
    void loginWithUnknownUserFails() throws Exception {
        mockMvc.perform(post("/employee/info/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"123456\"}"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Account not found"));
    }

    @Test
    void loginToDisabledAccountFails() throws Exception {
        jdbcTemplate.update("update employee set status = 0 where username = 'adam'");
        try {
            mockMvc.perform(post("/employee/info/login")
                            .contentType(APPLICATION_JSON)
                            .content("{\"username\":\"adam\",\"password\":\"123456\"}"))
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.msg").value("Account locked"));
        } finally {
            // The database is shared by all tests, so put it back.
            jdbcTemplate.update("update employee set status = 1 where username = 'adam'");
        }
    }

    @Test
    void protectedEndpointWithoutTokenIs401() throws Exception {
        mockMvc.perform(get(ADMIN_ONLY_PAGE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithGarbageTokenIs401() throws Exception {
        mockMvc.perform(get(ADMIN_ONLY_PAGE).header("token", "not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithAnotherSecretIs401() throws Exception {
        // A forged token claiming to be the admin, signed with a key the server doesn't know.
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, 1L);
        claims.put(JwtClaimsConstant.POSITION, "System Admin");
        claims.put(JwtClaimsConstant.DEPARTMENT_ID, 1L);
        String forged = JwtUtil.createJWT("an-attacker-secret-that-is-also-32-chars-plus", 60_000, claims);

        mockMvc.perform(get(ADMIN_ONLY_PAGE).header("token", forged))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminWithValidTokenCanUseAdminEndpoint() throws Exception {
        mockMvc.perform(get(ADMIN_ONLY_PAGE).header("token", loginAs("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));
    }

    @Test
    void consultantIsRejectedFromAdminEndpoint() throws Exception {
        mockMvc.perform(get(ADMIN_ONLY_PAGE).header("token", loginAs("yrwang")))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Current user is not admin"));
    }
}
