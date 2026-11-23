package com.erp.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Component
@ConfigurationProperties(prefix = "erp.jwt")
@Data
@Validated
public class JwtProperties {


    /**
     * admin-end jwt properties
     */
    // HS256 needs a key of at least 256 bits; the app refuses to start without one
    @NotBlank
    @Size(min = 32, message = "must be at least 32 characters (256 bits) for HS256")
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

//    /**
//     * client-end jwt properties
//     */
//    private String userSecretKey;
//    private long userTtl;
//    private String userTokenName;


}
