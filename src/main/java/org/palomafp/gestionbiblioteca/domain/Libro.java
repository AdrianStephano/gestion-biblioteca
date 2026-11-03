package org.palomafp.gestionbiblioteca.domain;

public class Libro {
    private Long id;
    private Isbn isbn; // Usamos nuestro Objeto Valor
    private String titulo;
    private Long sedeId; // Relación 1:N (Una Sede tiene muchos Libros)

    public Libro(Long id, Isbn isbn, String titulo, Long sedeId) {
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
        this.isbn = isbn;
    }
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public Long getSedeId() {
        return sedeId;
    }
    public void setSedeId(Long sedeId) {
        this.sedeId = sedeId;
    }
}