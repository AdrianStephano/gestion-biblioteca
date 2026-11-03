package org.palomafp.gestionbiblioteca.domain;

import java.util.List;

public interface IRepositorioSede extends IRepositorioExtend<SedeBiblioteca, Long> {
    // Métodos propios de Sede
    List<SedeBiblioteca> findByCiudad(String ciudad);
    long contarLibrosEnSede(Long sedeId);
}
