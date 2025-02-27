package com.infobip.spring.data.jdbc.mapped.collection;

import static com.infobip.spring.data.jdbc.mapped.collection.QStudent.student;
import static com.infobip.spring.data.jdbc.mapped.collection.QStudentCourse.studentCourse;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.Collections;
import java.util.HashSet;

import com.infobip.spring.data.jdbc.TestBase;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@AllArgsConstructor
public class StudentRepositoryTest extends TestBase {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final StudentCourseRepository studentCourseRepository;

    @AfterEach
    void cleanUp() {
        studentCourseRepository.deleteAll();
    }

    @Test
    void shouldFindAll() {

        // given
        var givenCourse = givenCourse();
        var givenStudent = givenStudentAttendingCourse(givenCourse);

        // when
        var actual = studentRepository.findAll();

        // then
        var givenStudentCourse = givenStudent.getCourses()
                                             .stream()
                                             .findFirst()
                                             .orElseThrow(NullPointerException::new);
        then(actual).containsExactly(new Student(givenStudent.getId(),
                                                 givenStudent.getName(),
                                                 Collections.singleton(new StudentCourse(givenStudentCourse.getId(),
                                                                                         AggregateReference.to(givenCourse.getId()),
                                                                                         givenStudent.getId()))));
    }

    @Test
    void shouldQueryManyToMany() {

        // given
        var givenCourse = givenCourse();
        var givenStudent = givenStudentAttendingCourse(givenCourse);

        // when
        var actual = studentRepository.query(query -> query.select(studentRepository.entityProjection())
                                                           .from(student)
                                                           .innerJoin(studentCourse)
                                                           .on(student.id.eq(studentCourse.studentId))
                                                           .fetch());

        // then
        var givenStudentCourse = givenStudent.getCourses()
                                             .stream()
                                             .findFirst()
                                             .orElseThrow(NullPointerException::new);
        then(actual).containsExactly(new Student(givenStudent.getId(),
                                                 givenStudent.getName(),
                                                 Collections.singleton(new StudentCourse(givenStudentCourse.getId(),
                                                                                         AggregateReference.to(givenCourse.getId()),
                                                                                         givenStudent.getId()))));
    }

    private Course givenCourse() {
        return courseRepository.save(new Course(null, "givenCourseName"));
    }

    private Student givenStudentAttendingCourse(Course givenCourse) {
        var givenStudent = new Student(null, "givenStudent", new HashSet<>());
        givenStudent.addItem(givenCourse);
        return studentRepository.save(givenStudent);
    }

}
