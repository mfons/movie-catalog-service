package io.javabrains.movie_catalog_service.models;

public record Student(int id, String name, int marks) {
    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", marks=" + marks +
                '}';
    }
}
