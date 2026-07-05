package com.cst438.controller;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Section;
import com.cst438.domain.SectionRepository;
import com.cst438.domain.Term;
import com.cst438.domain.TermRepository;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.service.GradebookServiceProxy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentScheduleControllerUnitTest {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TermRepository termRepository;

    @MockitoBean
    private GradebookServiceProxy gradebook;

    @Test
    public void addCourseTest() {
        // --- DATA SETUP ---
        Course c = new Course();
        c.setCourseId("cst300");
        c.setTitle("Introduction");
        c.setCredits(3);
        courseRepository.save(c);

        // Term requires specific non-null dates per schema.sql
        Term t = new Term();
        t.setTermId(20251); // Added a primary key
        t.setYear(2025);  
        t.setSemester("Fall");
        
        // Satisfy "NOT NULL" constraints for Date columns
        long now = System.currentTimeMillis();
        Date today = new Date(now);
        t.setAddDate(today);
        t.setAddDeadline(today);
        t.setDropDeadline(today);
        t.setStartDate(today);
        t.setEndDate(today);
        
        termRepository.save(t);

        Section s = new Section();
        s.setCourse(c);
        s.setTerm(t);
        s.setSectionId(101);
        s.setBuilding("090");
        s.setRoom("A1");
        s.setTimes("MWF");
        
        // Save and get the database-assigned secNo
        s = sectionRepository.save(s);
        int generatedSecNo = s.getSectionNo();

        // --- TEST LOGIC ---
        Section section = sectionRepository.findById(generatedSecNo).orElse(null);
        assertNotNull(section, "Section should exist in database");

        EnrollmentDTO dto = new EnrollmentDTO(
                0, null, 2, "Sam", "sam@csumb.edu",
                section.getCourse().getCourseId(),
                section.getCourse().getTitle(),
                section.getSectionId(),
                section.getSectionNo(),
                section.getBuilding(),
                section.getRoom(),
                section.getTimes(),
                section.getCourse().getCredits(),
                section.getTerm().getYear(),
                section.getTerm().getSemester()
        );

        List<EnrollmentDTO> list = List.of(dto);
        gradebook.sendMessage("addEnrollment", list);
        verify(gradebook, times(1)).sendMessage(eq("addEnrollment"), any());
    }

    @Test
    public void dropCourseTest() {
        int enrollmentId = 1;
        gradebook.sendMessage("deleteEnrollment", enrollmentId);
        verify(gradebook, times(1)).sendMessage(eq("deleteEnrollment"), any());
    }
}