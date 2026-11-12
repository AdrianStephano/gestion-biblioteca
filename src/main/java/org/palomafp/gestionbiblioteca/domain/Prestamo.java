package org.palomafp.gestionbiblioteca.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad del dominio que registra el préstamo de un libro.
 * Una fecha de devolución null significa que el préstamo sigue activo. La entidad
 * protege la coherencia temporal de las fechas y expresa la operación de cierre,
 * mientras que la infraestructura se ocupa de guardar esos datos como texto ISO en SQLite.
 */
public class Prestamo {
    /** Clave primaria generada al insertar el préstamo; null indica que es nuevo. */
    private Long id;
    /** Fecha de inicio, obligatoria para todo préstamo. */
    private LocalDate fechaPrestamo;
    /** Fecha de devolución; permanece null mientras el préstamo está activo. */
    private LocalDate fechaDevolucion;
    /** Identificador del libro asociado, equivalente a la clave foránea libro_id. */
    private Long libroId;

    /**
     * Crea un préstamo.
     *
     * @param id identificador (puede ser nulo si aún no está persistido)
     * @param fechaPrestamo fecha del préstamo, no nula
     * @param fechaDevolucion fecha de devolución, puede ser nula si sigue activo
     * @param libroId identificador del libro, no nulo
     * @throws IllegalArgumentException si algún dato es inválido o la devolución es anterior al préstamo
     */
    public Prestamo(Long id, LocalDate fechaPrestamo, LocalDate fechaDevolucion, Long libroId) {
        if (fechaPrestamo == null) {
            throw new IllegalArgumentException("La fecha de préstamo no puede ser nula.");
        }
        if (libroId == null) {
            throw new IllegalArgumentException("El préstamo debe estar asociado a un libro (libroId no puede ser nulo).");
        }
        validarFechas(fechaPrestamo, fechaDevolucion);
        this.id = id;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.libroId = libroId;
    }

    /**
     * Aplica la regla temporal compartida por el constructor, los setters y cerrar:
     * si existe una devolución, no puede ser anterior a la fecha del préstamo.
     *
     * @param fechaPrestamo fecha de inicio que debe respetarse
     * @param fechaDevolucion fecha final, o null si todavía no se ha devuelto
     * @throws IllegalArgumentException si la devolución precede al préstamo
     */
    private static void validarFechas(LocalDate fechaPrestamo, LocalDate fechaDevolucion) {
        if (fechaDevolucion != null && fechaDevolucion.isBefore(fechaPrestamo)) {
            throw new IllegalArgumentException("La fecha de devolución no puede ser anterior a la fecha de préstamo.");
        }
    }

    /**
     * Cierra el préstamo estableciendo su fecha de devolución. La fecha se valida antes
     * de modificar el estado para que una operación inválida no deje la entidad incoherente.
     *
     * @param fechaDevolucion fecha en la que se devuelve el libro. No puede ser nula ni anterior a la fecha de préstamo.
     * @throws IllegalArgumentException si la fecha es nula o anterior a la fecha del préstamo
     */
    public void cerrar(LocalDate fechaDevolucion) {
        if (fechaDevolucion == null) {
            throw new IllegalArgumentException("La fecha de devolución no puede ser nula.");
        }
        validarFechas(this.fechaPrestamo, fechaDevolucion);
        this.fechaDevolucion = fechaDevolucion;
    }

    /** @return identificador persistido del préstamo, o null si todavía es nuevo */
    public Long getId() {
        return id;
    }

    /**
     * Asigna el identificador generado al guardar el préstamo.
     *
     * @param id clave primaria, o null si aún no se ha persistido
     */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return fecha en la que comenzó el préstamo */
    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    /**
     * Cambia la fecha inicial si no invalida una fecha de devolución ya establecida.
     *
     * @param fechaPrestamo nueva fecha inicial
     * @throws IllegalArgumentException si es null o deja la devolución antes del inicio
     */
    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        if (fechaPrestamo == null) {
            throw new IllegalArgumentException("La fecha de préstamo no puede ser nula.");
        }
        validarFechas(fechaPrestamo, this.fechaDevolucion);
        this.fechaPrestamo = fechaPrestamo;
    }

    /** @return fecha de devolución o null mientras el préstamo permanezca activo */
    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    /**
     * Establece o elimina la fecha de devolución, manteniendo la coherencia temporal.
     *
     * @param fechaDevolucion fecha de devolución, o null para dejarlo activo
     * @throws IllegalArgumentException si la fecha no nula es anterior a la fecha del préstamo
     */
    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        validarFechas(this.fechaPrestamo, fechaDevolucion);
        this.fechaDevolucion = fechaDevolucion;
    }

    /** @return identificador del libro prestado */
    public Long getLibroId() {
        return libroId;
    }

    /**
     * Cambia el libro asociado usando su identificador.
     *
     * @param libroId clave del libro, no nula
     * @throws IllegalArgumentException si libroId es null
     */
    public void setLibroId(Long libroId) {
        if (libroId == null) {
            throw new IllegalArgumentException("El préstamo debe estar asociado a un libro (libroId no puede ser nulo).");
        }
        this.libroId = libroId;
    }

    /**
     * Compara préstamos por su identificador persistente.
     *
     * @param o objeto que se quiere comparar
     * @return true si o es un préstamo con el mismo identificador
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prestamo)) return false;
        Prestamo prestamo = (Prestamo) o;
        return Objects.equals(id, prestamo.id);
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
     * Devuelve una representación compacta que incluye el estado activo o devuelto.
     *
     * @return texto con los datos principales del préstamo
     */
    @Override
    public String toString() {
        String estado = fechaDevolucion == null
                ? "Activo"
                : "Devuelto: " + fechaDevolucion;
        return "[Prestamo ID: " + id + "] Libro ID: " + libroId
                + " | Fecha prestamo: " + fechaPrestamo + " | " + estado;
    }
}
