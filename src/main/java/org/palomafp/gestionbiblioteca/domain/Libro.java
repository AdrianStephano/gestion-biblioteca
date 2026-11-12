package org.palomafp.gestionbiblioteca.domain;

import java.util.Objects;

/**
 * Entidad del dominio que representa un libro catalogado en una sede.
 * Mantiene un objeto valor Isbn y el identificador de la sede asociada. Las reglas
 * básicas de validez se comprueban aquí, en vez de dispersarse por las consultas SQL,
 * mientras que la traducción entre esta entidad y la tabla libros corresponde al repositorio.
 */
public class Libro {
    /** Clave primaria generada por SQLite; null indica que el libro aún no está persistido. */
    private Long id;
    /** ISBN validado como objeto valor en lugar de guardar una cadena sin restricciones. */
    private Isbn isbn;
    /** Título obligatorio que se muestra en el catálogo. */
    private String titulo;
    /** Clave foránea lógica de la sede a la que pertenece este libro. */
    private Long sedeId;

    /**
     * Crea un libro.
     *
     * @param id identificador (puede ser nulo si aún no está persistido)
     * @param isbn ISBN del libro, no nulo
     * @param titulo título, no nulo ni vacío
     * @param sedeId identificador de la sede, no nulo
     * @throws IllegalArgumentException si algún parámetro obligatorio es inválido
     */
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

    /** @return identificador persistido, o null si es un libro nuevo */
    public Long getId() {
        return id;
    }

    /**
     * Asigna la clave generada por la base de datos tras una inserción.
     *
     * @param id identificador del libro, o null si todavía no está persistido
     */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return objeto valor que contiene el ISBN */
    public Isbn getIsbn() {
        return isbn;
    }

    /**
     * Sustituye el ISBN tras comprobar que se ha proporcionado un objeto valor.
     *
     * @param isbn ISBN validado que se asignará al libro
     * @throws IllegalArgumentException si isbn es null
     */
    public void setIsbn(Isbn isbn) {
        if (isbn == null) {
            throw new IllegalArgumentException("El ISBN del libro no puede ser nulo.");
        }
        this.isbn = isbn;
    }
    /** @return título actual del libro */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Cambia el título, rechazando cadenas nulas o vacías.
     *
     * @param titulo nuevo título
     * @throws IllegalArgumentException si titulo es null o está en blanco
     */
    public void setTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título del libro no puede ser nulo ni vacío.");
        }
        this.titulo = titulo;
    }
    /** @return identificador de la sede a la que pertenece el libro */
    public Long getSedeId() {
        return sedeId;
    }

    /**
     * Asocia el libro a una sede mediante su clave. La existencia de esa sede se
     * comprueba en persistencia mediante la clave foránea de la base de datos.
     *
     * @param sedeId identificador no nulo de la sede
     * @throws IllegalArgumentException si sedeId es null
     */
    public void setSedeId(Long sedeId) {
        if (sedeId == null) {
            throw new IllegalArgumentException("El libro debe estar asociado a una sede (sedeId no puede ser nulo).");
        }
        this.sedeId = sedeId;
    }

    /**
     * Compara libros por su clave persistente; los cambios en título o sede no crean
     * una identidad diferente.
     *
     * @param o objeto que se quiere comparar
     * @return true si o es un libro con el mismo identificador
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Libro)) return false;
        Libro libro = (Libro) o;
        return Objects.equals(id, libro.id);
    }

    /**
     * Calcula un hash coherente con equals.
     *
     * @return hash calculado a partir del identificador
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Devuelve una descripción compacta del libro para consola y trazas.
     *
     * @return texto con id, ISBN, título y sede
     */
    @Override
    public String toString() {
        return "[Libro ID: " + id + "] ISBN: " + isbn
                + " | " + titulo + " | Sede ID: " + sedeId;
    }
}
