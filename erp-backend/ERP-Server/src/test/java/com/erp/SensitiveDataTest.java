package com.erp;

import com.erp.context.BaseContext;
import com.erp.properties.AliOssProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/** Secrets and personal data must not leak through API responses, logs or request state. */
@ExtendWith(OutputCaptureExtension.class)
class SensitiveDataTest extends IntegrationTestBase {

    @Test
    void employeeApisDoNotReturnPasswordHashes() throws Exception {
        String token = loginAs("admin");

        mockMvc.perform(get("/employee/info/page").header("token", token)
                        .param("page", "1").param("pageSize", "10"))
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records[0].username").exists())
                .andExpect(jsonPath("$.data.records[*].password").isEmpty());

        mockMvc.perform(get("/employee/info/" + YRWANG_ID).header("token", token))
                .andExpect(jsonPath("$.data.username").value("yrwang"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void jwtIsNotWrittenToTheLog(CapturedOutput output) throws Exception {
        String token = loginAs("yrwang");

        mockMvc.perform(get("/employee/client/page").header("token", token)
                        .param("page", "1").param("pageSize", "1"))
                .andExpect(jsonPath("$.code").value(1));

        // anyone who can read the logs could otherwise reuse the token
        assertThat(output.getAll()).doesNotContain(token);
    }

    @Test
    void currentUserIsClearedAfterTheRequest() throws Exception {
        // BaseContext uses ThreadLocals; server threads are reused, so a leftover identity
        // would be seen by the next request on the same thread. MockMvc runs on this thread.
        mockMvc.perform(get("/employee/client/page").header("token", loginAs("yrwang"))
                        .param("page", "1").param("pageSize", "1"))
                .andExpect(jsonPath("$.code").value(1));

        assertThat(BaseContext.getCurrentId()).isNull();
        assertThat(BaseContext.getCurrentRole()).isNull();
        assertThat(BaseContext.getCurrentDepartment()).isNull();
    }

    @Test
    void ossSecretIsNotInToString() {
        // OssConfiguration logged the whole properties object at startup
        AliOssProperties properties = new AliOssProperties();
        properties.setAccessKeyId("key-id");
        properties.setAccessKeySecret("very-secret-value");

        assertThat(properties.toString()).doesNotContain("very-secret-value");
    }
}
