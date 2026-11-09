package org.palomafp.gestionbiblioteca.domain;

import java.util.Objects;

public class Libro {
    private Long id;
    private Isbn isbn; // Usamos nuestro Objeto Valor
    private String titulo;
    private Long sedeId; // Relación 1:N (Una Sede tiene muchos Libros)

    public Libro(Long id, Isbn isbn, String titulo, Long sedeId) {
        if (isbn == null) {
            throw new IllegalArgumentException("El ISBN del libro no puede ser nulo.");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título del libro no puede ser nulo ni vacío.");
        }
        if (sedeId == null) {
            throw new IllegalArgumentException("El libro debe estar asociado a una sede (sedeId no puede ser nulo).");
        }
        this.id = id;
        this.isbn = isbn;
        this.titulo = titulo;
        this.sedeId = sedeId;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Isbn getIsbn() {
        return isbn;
    }
    public void setIsbn(Isbn isbn) {
        if (isbn == null) {
            throw new IllegalArgumentException("El ISBN del libro no puede ser nulo.");
        }
        this.isbn = isbn;
    }
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título del libro no puede ser nulo ni vacío.");
        }
        this.titulo = titulo;
    }
    public Long getSedeId() {
        return sedeId;
    }
    public void setSedeId(Long sedeId) {
        if (sedeId == null) {
            throw new IllegalArgumentException("El libro debe estar asociado a una sede (sedeId no puede ser nulo).");
        }
        this.sedeId = sedeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Libro)) return false;
        Libro libro = (Libro) o;
        return Objects.equals(id, libro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Libro{" +
                "id=" + id +
                ", isbn=" + isbn +
                ", titulo='" + titulo + '\'' +
                ", sedeId=" + sedeId +
                '}';
    }
}
