package org.palomafp.gestionbiblioteca.domain;

import java.util.Objects;

public class Isbn {
    private final String codigo;

    public Isbn(String codigo) {
        if (codigo == null || !codigo.matches("^(97[89])?\\d{9}[\\dX]$")) {
            throw new IllegalArgumentException("El formato del ISBN no es válido.");
        }
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Isbn isbn = (Isbn) o;
        return Objects.equals(codigo, isbn.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return codigo;
    }
}