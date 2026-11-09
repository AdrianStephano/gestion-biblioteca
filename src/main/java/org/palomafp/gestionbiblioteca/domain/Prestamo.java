package org.palomafp.gestionbiblioteca.domain;

import java.time.LocalDate;
import java.util.Objects;

public class Prestamo {
    private Long id;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private Long libroId; // Relación 1:N (Un Libro puede tener muchos Préstamos)

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

    private static void validarFechas(LocalDate fechaPrestamo, LocalDate fechaDevolucion) {
        if (fechaDevolucion != null && fechaDevolucion.isBefore(fechaPrestamo)) {
            throw new IllegalArgumentException("La fecha de devolución no puede ser anterior a la fecha de préstamo.");
        }
    }

    /**
     * Cierra el préstamo estableciendo su fecha de devolución.
     *
     * @param fechaDevolucion fecha en la que se devuelve el libro. No puede ser nula ni anterior a la fecha de préstamo.
     */
    public void cerrar(LocalDate fechaDevolucion) {
        if (fechaDevolucion == null) {
            throw new IllegalArgumentException("La fecha de devolución no puede ser nula.");
        }
        validarFechas(this.fechaPrestamo, fechaDevolucion);
        this.fechaDevolucion = fechaDevolucion;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }
    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        if (fechaPrestamo == null) {
            throw new IllegalArgumentException("La fecha de préstamo no puede ser nula.");
        }
        validarFechas(fechaPrestamo, this.fechaDevolucion);
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }
    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        validarFechas(this.fechaPrestamo, fechaDevolucion);
        this.fechaDevolucion = fechaDevolucion;
    }

    public Long getLibroId() {
        return libroId;
    }
    public void setLibroId(Long libroId) {
        if (libroId == null) {
            throw new IllegalArgumentException("El préstamo debe estar asociado a un libro (libroId no puede ser nulo).");
        }
        this.libroId = libroId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prestamo)) return false;
        Prestamo prestamo = (Prestamo) o;
        return Objects.equals(id, prestamo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Prestamo{" +
                "id=" + id +
                ", fechaPrestamo=" + fechaPrestamo +
                ", fechaDevolucion=" + fechaDevolucion +
                ", libroId=" + libroId +
                '}';
    }
}
