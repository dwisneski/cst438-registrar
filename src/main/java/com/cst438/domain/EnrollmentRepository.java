package com.cst438.domain;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends CrudRepository<Enrollment, Integer> {

    @Query("select e from Enrollment e where e.student.email = :email order by e.section.term.termId")
    List<Enrollment> findByEmail(@Param("email") String email);

    @Query("select e from Enrollment e where e.student.id = :studentId order by e.section.term.termId")
    List<Enrollment> findEnrollmentsByStudentIdOrderByTermId(@Param("studentId") int studentId);
    
    // Keeping these for other parts of your app
    @Query("select e from Enrollment e where e.section.term.year=:year and e.section.term.semester=:semester and e.student.id=:studentId order by e.section.course.courseId")
    List<Enrollment> findByYearAndSemesterOrderByCourseId(@Param("year") int year, @Param("semester") String semester, @Param("studentId") int studentId);

    @Query("select e from Enrollment e where e.section.sectionNo=:sectionNo and e.student.id=:studentId")
    Enrollment findEnrollmentBySectionNoAndStudentId(@Param("sectionNo") int sectionNo, @Param("studentId") int studentId);
}