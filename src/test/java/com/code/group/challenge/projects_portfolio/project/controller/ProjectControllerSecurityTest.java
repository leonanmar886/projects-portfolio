package com.code.group.challenge.projects_portfolio.project.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.config.location=classpath:application-test.properties")
public class ProjectControllerSecurityTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void unauthenticatedShouldBeUnauthorized() {
        var url = "http://localhost:" + port + "/api/projects";
        var resp = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void authenticatedShouldReturnOk() {
        var url = "http://localhost:" + port + "/api/projects";
        var rt = restTemplate.withBasicAuth("admin", "admin123");
        var resp = rt.getForEntity(url, String.class);
        assertTrue(resp.getStatusCode().is2xxSuccessful());
    }
}
