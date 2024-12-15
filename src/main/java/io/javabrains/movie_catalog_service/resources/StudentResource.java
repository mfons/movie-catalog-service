package io.javabrains.movie_catalog_service.resources;

import io.javabrains.movie_catalog_service.models.Student;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class StudentResource {

    private List<Student> students = new ArrayList<>(List.of(
            new Student(1, "Michael", 60),
            new Student(2, "Navin", 60)
    ));

    @GetMapping("/students")
    public List<Student> getStudents() {
        return students;
    }

    @GetMapping("/csrf-token")
    public CsrfToken getCsrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }

    @PostMapping("/student")
    public Student addStudent(@RequestBody Student student) {
        students.add(student);
        return student;
    }
}
