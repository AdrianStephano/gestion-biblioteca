package org.palomafp.gestionbiblioteca.domain;

import java.util.Objects;

/**
 * Entidad del dominio que representa una sede física de la biblioteca.
 * Su identificador distingue la entidad a lo largo de su ciclo de vida; nombre y ciudad
 * son datos descriptivos validados antes de asignarse. La clase no conoce la tabla sedes
 * ni depende de JDBC, por lo que puede usarse con cualquier adaptador de persistencia.
 */
public class SedeBiblioteca {
    /** Clave primaria asignada al persistir; null representa una sede todavía nueva. */
    private Long id;
    /** Nombre visible de la sede, obligatorio y no vacío. */
    private String nombre;
    /** Ciudad donde se encuentra la sede, obligatoria y no vacía. */
    private String ciudad;

    /**
     * Crea una sede.
     *
     * @param id identificador (puede ser nulo si aún no está persistida)
     * @param nombre nombre de la sede, no nulo ni vacío
     * @param ciudad ciudad de la sede, no nula ni vacía
     * @throws IllegalArgumentException si nombre o ciudad son nulos o vacíos
     */
    public SedeBiblioteca(Long id, String nombre, String ciudad) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la sede no puede ser nulo ni vacío.");
        }
        if (ciudad == null || ciudad.isBlank()) {
            throw new IllegalArgumentException("La ciudad de la sede no puede ser nula ni vacía.");
        }
        this.id = id;
        this.nombre = nombre;
        this.ciudad = ciudad;
    }

    /** @return identificador persistido de la sede, o null si aún no se ha guardado */
    public Long getId() {
        return id;
    }

    /**
     * Asigna el identificador de persistencia devuelto por el repositorio.
     *
     * @param id clave asignada a la sede; puede ser null para indicar que es nueva
     */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return nombre actual de la sede */
    public String getNombre() {
        return nombre;
    }

    /**
     * Cambia el nombre después de validar la regla de dominio correspondiente.
     *
     * @param nombre nuevo nombre de la sede
     * @throws IllegalArgumentException si nombre es null o está en blanco
     */
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la sede no puede ser nulo ni vacío.");
        }
        this.nombre = nombre;
    }

    /** @return ciudad actual de la sede */
    public String getCiudad() {
        return ciudad;
    }

    /**
     * Cambia la ciudad después de validar la regla de dominio correspondiente.
     *
     * @param ciudad nueva ciudad de la sede
     * @throws IllegalArgumentException si ciudad es null o está en blanco
     */
    public void setCiudad(String ciudad) {
        if (ciudad == null || ciudad.isBlank()) {
            throw new IllegalArgumentException("La ciudad de la sede no puede ser nula ni vacía.");
        }
        this.ciudad = ciudad;
    }

    /**
     * Compara sedes por su identidad persistente. Los atributos descriptivos no alteran
     * la identidad cuando una sede cambia de nombre o ciudad.
     *
     * @param o objeto que se quiere comparar
     * @return true si o es una sede con el mismo identificador
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SedeBiblioteca)) return false;
        SedeBiblioteca that = (SedeBiblioteca) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Calcula un hash coherente con la identidad utilizada por equals.
     *
     * @return hash calculado a partir del identificador
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Crea una representación breve y legible para trazas y salida de consola.
     *
     * @return texto con el identificador, nombre y ciudad
     */
    @Override
    public String toString() {
        return "[Sede ID: " + id + "] " + nombre + " | Ciudad: " + ciudad;
    }
}
