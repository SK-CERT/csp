package com.intrasoft.csp.integration.sandbox.client.anon;

import com.intrasoft.csp.anon.AnonApp;
import com.intrasoft.csp.client.AnonClient;
import com.intrasoft.csp.client.config.AnonClientConfig;
import com.intrasoft.csp.client.config.CspRestTemplateConfiguration;
import com.intrasoft.csp.client.impl.AnonClientImpl;
import com.intrasoft.csp.commons.client.ApiVersionClient;
import com.intrasoft.csp.commons.client.RetryRestTemplate;
import com.intrasoft.csp.commons.model.*;
import com.intrasoft.csp.commons.routes.ContextUrl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.intrasoft.csp.commons.routes.ContextUrl.DATA_ANONYMIZATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AnonApp.class, AnonClientConfig.class, CspRestTemplateConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port: 8585",
                "anon.server.protocol: http",
                "anon.server.host: localhost",
                "anon.server.port: 8585",
                "api.version: 1",
                "csp.retry.backOffPeriod:10",
                "csp.retry.maxAttempts:1",
                "key.update=10000"
        })
@ActiveProfiles("mysql") //TODO: to be changed to use H2 DB profile
public class AnonClientTest {
    private static final Logger LOG = LoggerFactory.getLogger(AnonClientTest.class);

    @Autowired
    RetryRestTemplate retryRestTemplate;

    @Autowired
    @Qualifier("anonClient")
    AnonClient anonClient;

    @Test
    public void sendPostIntegrationDataTest() throws IOException {
        IntegrationAnonData data = new IntegrationAnonData();
        data.setDataType(IntegrationDataType.TRUSTCIRCLE);
        data.setCspId("9");
        ResponseEntity<String> response = anonClient.postAnonData(data, DATA_ANONYMIZATION);
        //TODO: on an empty DB AnonException is expected
        assertThat(response.getStatusCode(),is(HttpStatus.OK));
    }
}


