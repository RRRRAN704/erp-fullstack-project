package com.erp.config;

import com.erp.interceptor.JwtTokenAdminInterceptor;
import com.erp.json.JacksonDateTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Configuration class, register web layer related components.
 * Implements WebMvcConfigurer (instead of extending WebMvcConfigurationSupport) so that
 * Spring Boot's MVC auto-configuration stays on: it serves the springdoc Swagger UI resources.
 */
@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * Register custom interceptors
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Register custom interceptor...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/employee/**")
                .excludePathPatterns("/employee/info/login")
                .excludePathPatterns("/employee/info/send-email-code")
                .excludePathPatterns("/employee/info/reset-password")
                .excludePathPatterns("/employee/order");

    }

    /**
     * API documentation (springdoc). Served only when springdoc is enabled (dev profile):
     * Swagger UI at /swagger-ui.html, the OpenAPI JSON at /v3/api-docs.
     */
    @Bean
    public OpenAPI erpOpenApi() {
        return new OpenAPI().info(new Info()
                .title("ERP Project API Documentation")
                .version("1.0")
                .description("ERP Project API Documentation"));
    }


    /**
     * Date/time formats for JSON request and response bodies ("yyyy-MM-dd HH:mm" etc.).
     * Spring Boot adds every JacksonModule bean to the JsonMapper behind its message converter.
     */
    @Bean
    public JacksonDateTimeModule jacksonDateTimeModule() {
        return new JacksonDateTimeModule();
    }

}
