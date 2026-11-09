package org.palomafp.gestionbiblioteca.domain;

import java.util.List;

public interface IRepositorioLibro extends IRepositorioExtend<Libro, Long> {
    // Métodos propios de Libro
    List<Libro> findBySedeId(Long sedeId);
    List<Libro> findByTituloContaining(String texto);
}
