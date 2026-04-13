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
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.EnrollmentDTO;

@RestController
public class StudentController {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public StudentController(
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    // retrieve schedule for student for a term
    @GetMapping("/enrollments")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getSchedule(
            @RequestParam("year") int year,
            @RequestParam("semester") String semester,
            Principal principal) {

        User student = userRepository.findByEmail(principal.getName());
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "student not found");
        }

        List<Enrollment> enrollments =
                enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(student.getId());

        List<EnrollmentDTO> result = new ArrayList<>();

        for (Enrollment e : enrollments) {
            if (e.getSection().getTerm().getYear() == year &&
                e.getSection().getTerm().getSemester().equals(semester)) {

                result.add(convertToDTO(e));
            }
        }

        return result;
    }

    // return transcript for student
    @GetMapping("/transcripts")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getTranscript(Principal principal) {

        User student = userRepository.findByEmail(principal.getName());
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "student not found");
        }

        List<Enrollment> enrollments =
                enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(student.getId());

        List<EnrollmentDTO> result = new ArrayList<>();

        for (Enrollment e : enrollments) {
            result.add(convertToDTO(e));
        }

        return result;
    }

    private EnrollmentDTO convertToDTO(Enrollment e) {
        return new EnrollmentDTO(
                e.getEnrollmentId(),
                e.getGrade(),
                e.getStudent().getId(),
                e.getStudent().getName(),
                e.getStudent().getEmail(),
                e.getSection().getCourse().getCourseId(),
                e.getSection().getCourse().getTitle(),
                e.getSection().getSectionId(),
                e.getSection().getSectionNo(),
                e.getSection().getBuilding(),
                e.getSection().getRoom(),
                e.getSection().getTimes(),
                e.getSection().getCourse().getCredits(),
                e.getSection().getTerm().getYear(),
                e.getSection().getTerm().getSemester()
        );
    }
}