package com.erp;

import com.aliyun.oss.common.comm.ResponseMessage;
import com.aliyun.oss.common.parser.JAXBResponseParser;
import com.aliyun.oss.internal.model.OSSErrorResult;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Aliyun OSS SDK parses OSS error responses with JAXB (javax.xml.bind). Without a working
 * javax JAXB on the classpath, a failed upload throws NoClassDefFoundError instead of OSSException.
 * This broke once already (removing jaxb-api), and Spring Boot 3 manages the jakarta-namespace
 * JAXB, so guard it with a test that runs the SDK's own parser.
 */
class OssJaxbTest {

    @Test
    void ossSdkCanParseAnErrorResponse() throws Exception {
        String xml = "<Error>"
                + "<Code>NoSuchBucket</Code>"
                + "<Message>The specified bucket does not exist.</Message>"
                + "<RequestId>test-request-id</RequestId>"
                + "</Error>";
        ResponseMessage response = new ResponseMessage(null);
        response.setStatusCode(404);
        response.setContent(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        Object parsed = new JAXBResponseParser(OSSErrorResult.class).parse(response);

        assertThat(parsed).isInstanceOf(OSSErrorResult.class);
        assertThat(((OSSErrorResult) parsed).Code).isEqualTo("NoSuchBucket");
    }
}
