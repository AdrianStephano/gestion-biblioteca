package org.palomafp.gestionbiblioteca.domain;

import java.util.List;

public interface IRepositorioPrestamo extends IRepositorioExtend<Prestamo, Long> {
    // Métodos propios de Préstamo
    List<Prestamo> findPrestamosByLibroId(Long libroId);
    void cerrarPrestamoHoy(Long prestamoId);
}
