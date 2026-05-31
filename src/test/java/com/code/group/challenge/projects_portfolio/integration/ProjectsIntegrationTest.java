package com.code.group.challenge.projects_portfolio.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.config.location=classpath:application-test.properties")
class ProjectsIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void basicAuthProtected() {
        var url = "http://localhost:" + port + "/api/projects";
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void basicAuthWithCredentials() {
        var url = "http://localhost:" + port + "/api/projects";
        var rt = restTemplate.withBasicAuth("admin", "admin123");
        ResponseEntity<String> resp = rt.getForEntity(url, String.class);
        // should be OK (may be empty list)
        assertTrue(resp.getStatusCode().is2xxSuccessful());
    }
}

