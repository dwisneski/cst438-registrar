package com.cst438.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.cst438.dto.LoginDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerUnitTest {

    @Autowired
    private WebTestClient client;

    @Test
    public void testScheduleAndTranscript() {

        String email = "sam@csumb.edu";
        String password = "sam2025";

        // login
        EntityExchangeResult<LoginDTO> login = client.get()
                .uri("/login")
                .headers(headers -> headers.setBasicAuth(email, password))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginDTO.class)
                .returnResult();

        String jwt = login.getResponseBody().jwt();
        assertNotNull(jwt);

        // test schedule
        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/enrollments")
                        .queryParam("year", 2025)
                        .queryParam("semester", "Fall")
                        .build())
                .headers(headers -> headers.setBearerAuth(jwt))
                .exchange()
                .expectStatus().isOk();

        // test transcript
        client.get()
                .uri("/transcripts")
                .headers(headers -> headers.setBearerAuth(jwt))
                .exchange()
                .expectStatus().isOk();
    }
}