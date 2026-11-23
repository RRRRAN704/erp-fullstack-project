package com.erp.properties;


import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "erp.alioss")
@Data
public class AliOssProperties {

    private String endpoint;
    private String accessKeyId;
    @ToString.Exclude // keep the secret out of logs
    private String accessKeySecret;
    private String bucketName;

}
