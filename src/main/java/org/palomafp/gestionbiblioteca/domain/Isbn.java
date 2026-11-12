package org.palomafp.gestionbiblioteca.domain;

import java.util.Objects;

/**
 * Objeto valor inmutable que representa el ISBN de una publicación.
 * Pertenece al dominio porque encapsula una regla propia del negocio: no se puede
 * construir un ISBN con formato inválido. Al no tener identidad propia, dos instancias
 * con el mismo código se consideran equivalentes por su valor.
 */
public class Isbn {
    /** Código ISBN validado al construir el objeto; final impide sustituirlo después. */
    private final String codigo;

    /**
     * Crea un ISBN.
     *
     * @param codigo código ISBN de 10 o 13 caracteres
     * @throws IllegalArgumentException si el código es nulo o su formato no es válido
     */
    public Isbn(String codigo) {
        if (codigo == null || !codigo.matches("^(97[89])?\\d{9}[\\dX]$")) {
            throw new IllegalArgumentException("El formato del ISBN no es válido.");
        }
        this.codigo = codigo;
    }

    /**
     * Devuelve el código del ISBN.
     *
     * @return código ISBN
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Compara este ISBN con otro objeto por su tipo y código.
     *
     * @param o objeto que se quiere comparar
     * @return true si o es un Isbn con el mismo código
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Isbn isbn = (Isbn) o;
        return Objects.equals(codigo, isbn.codigo);
    }

    /**
     * Genera un hash coherente con equals, necesario al utilizar ISBN en colecciones
     * basadas en hash.
     *
     * @return valor hash calculado a partir del código
     */
    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    /**
     * Devuelve el código en su forma textual para mostrarlo y enlazarlo con la columna
     * isbn de la tabla libros.
     *
     * @return código ISBN
     */
    @Override
    public String toString() {
        return codigo;
    }
}