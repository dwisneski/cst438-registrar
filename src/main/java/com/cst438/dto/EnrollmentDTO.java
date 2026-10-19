package com.cst438.dto;
/*
 * Data Transfer Object for student enrollment data for a section of a course
 */
public record EnrollmentDTO(
        Integer enrollmentId,
        String grade,  // final grade. May be null until instructor enters final grades.
        Integer studentId,
        String name,
        String email,
        String courseId,
        String title,
        Integer sectionId,
        Integer sectionNo,
        String building,
        String room,
        String times,
        Integer credits,
        Integer year,
        String semester

) {
}
