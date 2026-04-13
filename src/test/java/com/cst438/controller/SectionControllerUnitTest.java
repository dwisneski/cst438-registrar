package com.cst438.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.TermRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.LoginDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.service.GradebookServiceProxy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SectionControllerUnitTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TermRepository termRepository;

    @MockitoBean
    GradebookServiceProxy gradebookService;

    @Test
    public void sectionCreateUpdateDelete() throws Exception {
        // --- DATA SETUP ---
        // Create the Course the controller expects
        Course c = new Course();
        c.setCourseId("cst363");
        c.setTitle("Database");
        courseRepository.save(c);

        // Create the Instructor the controller expects
        User instructor = new User();
        instructor.setEmail("ted@csumb.edu");
        instructor.setName("Ted");
        instructor.setType("ROLE_INSTRUCTOR");
        userRepository.save(instructor);

        // Create the Term the controller expects
        Term t = new Term();
        t.setYear(2025);
        t.setSemester("Fall");
        termRepository.save(t);

        // --- LOGIN ---
        String adminEmail = "admin@csumb.edu";
        String password = "admin";

        EntityExchangeResult<LoginDTO> login_dto = client.get().uri("/login")
                .headers(headers -> headers.setBasicAuth(adminEmail, password))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginDTO.class).returnResult();

        String jwt = login_dto.getResponseBody().jwt();

        // --- POST NEW SECTION ---
        SectionDTO sectionDTO = new SectionDTO(0, 2025, "Fall", "cst363", null, 1, "090", "E410", "T Th 9-10", null, "ted@csumb.edu");

        EntityExchangeResult<SectionDTO> sectionResponse = client.post().uri("/sections")
                .headers(headers -> headers.setBearerAuth(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sectionDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SectionDTO.class).returnResult();

        SectionDTO actualSection = sectionResponse.getResponseBody();
        assertTrue(actualSection.secNo() > 0);
        verify(gradebookService, times(1)).sendMessage(eq("addSection"), any());

        // --- PUT UPDATE ---
        sectionDTO = new SectionDTO(actualSection.secNo(), 2025, "Fall", "cst363", null, 1, "090", "F110", "T Th 2-3pm", null, "ted@csumb.edu");
        client.put().uri("/sections")
                .headers(headers -> headers.setBearerAuth(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sectionDTO)
                .exchange()
                .expectStatus().isOk();

        // --- DELETE ---
        client.delete().uri("/sections/" + actualSection.secNo())
                .headers(headers -> headers.setBearerAuth(jwt))
                .exchange()
                .expectStatus().isOk();
    }
}