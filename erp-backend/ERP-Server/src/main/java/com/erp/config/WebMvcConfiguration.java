package com.erp.config;

import com.erp.interceptor.JwtTokenAdminInterceptor;
import com.erp.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

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


    /*Extending the Spring MVC framework's message converter*/
    //this ensures that all JSON data in the system
    // follows the custom rules I defined in JacksonObjectMapper, instead of using the default settings.
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("Extending the message converter...");
        // Give Spring Boot's own Jackson converter our ObjectMapper, keeping its place in the list.
        // (Adding a new one at index 0 would put it before the byte[] and String converters,
        // so e.g. springdoc's byte[] /v3/api-docs response would be written as a base64 string.)
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                jacksonConverter.setObjectMapper(new JacksonObjectMapper());
            }
        }
    }

}
