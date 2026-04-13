package com.cst438.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;

@RestController
public class StudentController {

    private final EnrollmentRepository enrollmentRepository;

    public StudentController(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping("/enrollments")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getSchedule(
            @RequestParam("year") int year,
            @RequestParam("semester") String semester,
            Principal principal) {

        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        List<Enrollment> enrollments = enrollmentRepository.findByEmail(principal.getName());
        List<EnrollmentDTO> result = new ArrayList<>();

        if (enrollments != null) {
            for (Enrollment e : enrollments) {
                try {
                    if (e.getSection() != null && e.getSection().getTerm() != null) {
                        if (e.getSection().getTerm().getYear() == year &&
                            semester.equals(e.getSection().getTerm().getSemester())) {
                            result.add(convertToDTO(e));
                        }
                    }
                } catch (Exception ex) {
                    // Skip any malformed enrollment records
                }
            }
        }
        return result;
    }

    @GetMapping("/transcripts")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getTranscript(Principal principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        List<Enrollment> enrollments = enrollmentRepository.findByEmail(principal.getName());
        List<EnrollmentDTO> result = new ArrayList<>();

        if (enrollments != null) {
            for (Enrollment e : enrollments) {
                result.add(convertToDTO(e));
            }
        }
        return result;
    }

    private EnrollmentDTO convertToDTO(Enrollment e) {
        // We use "null-safe" checks for every single nested object
        return new EnrollmentDTO(
                e.getEnrollmentId(),
                e.getGrade(),
                (e.getStudent() != null) ? e.getStudent().getId() : 0,
                (e.getStudent() != null) ? e.getStudent().getName() : "",
                (e.getStudent() != null) ? e.getStudent().getEmail() : "",
                (e.getSection() != null && e.getSection().getCourse() != null) ? e.getSection().getCourse().getCourseId() : "",
                (e.getSection() != null && e.getSection().getCourse() != null) ? e.getSection().getCourse().getTitle() : "",
                (e.getSection() != null) ? e.getSection().getSectionId() : 0,
                (e.getSection() != null) ? e.getSection().getSectionNo() : 0,
                (e.getSection() != null) ? e.getSection().getBuilding() : "",
                (e.getSection() != null) ? e.getSection().getRoom() : "",
                (e.getSection() != null) ? e.getSection().getTimes() : "",
                (e.getSection() != null && e.getSection().getCourse() != null) ? e.getSection().getCourse().getCredits() : 0,
                (e.getSection() != null && e.getSection().getTerm() != null) ? e.getSection().getTerm().getYear() : 0,
                (e.getSection() != null && e.getSection().getTerm() != null) ? e.getSection().getTerm().getSemester() : ""
        );
    }
}