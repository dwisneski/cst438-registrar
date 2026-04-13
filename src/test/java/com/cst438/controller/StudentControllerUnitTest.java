package com.cst438.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.LoginDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerUnitTest {

    @Autowired
    private WebTestClient client;

    @Test
    public void testScheduleAndTranscript() {

        String studentEmail = "sam@csumb.edu";
        String password = "sam2025";

        // 1. LOGIN
        EntityExchangeResult<LoginDTO> loginResult = client.get()
                .uri("/login")
                .headers(headers -> headers.setBasicAuth(studentEmail, password))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginDTO.class)
                .returnResult();

        assertNotNull(loginResult.getResponseBody(), "Login response should not be null");

        String jwt = loginResult.getResponseBody().jwt();
        assertNotNull(jwt, "JWT token should not be null");

        // 2. TEST ENROLLMENTS (Schedule)
        EntityExchangeResult<List<EnrollmentDTO>> scheduleResult = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/enrollments")
                        .queryParam("year", 2026)
                        .queryParam("semester", "Spring")
                        .build())
                .headers(headers -> headers.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EnrollmentDTO.class)
                .returnResult();

        List<EnrollmentDTO> schedule = scheduleResult.getResponseBody();
        assertNotNull(schedule, "Schedule should not be null");

        // Optional (safe assertion — won’t fail if empty)
        System.out.println("Schedule size: " + schedule.size());

        // 3. TEST TRANSCRIPT
        EntityExchangeResult<List<EnrollmentDTO>> transcriptResult = client.get()
                .uri("/transcripts")
                .headers(headers -> headers.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EnrollmentDTO.class)
                .returnResult();

        List<EnrollmentDTO> transcript = transcriptResult.getResponseBody();
        assertNotNull(transcript, "Transcript should not be null");

        System.out.println("Transcript size: " + transcript.size());
    }
}