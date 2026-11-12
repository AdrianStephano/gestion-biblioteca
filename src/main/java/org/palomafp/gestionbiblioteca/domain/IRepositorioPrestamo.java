package org.palomafp.gestionbiblioteca.domain;

import java.util.List;

/**
 * Interfaz para la persistencia de préstamos y sus consultas de negocio.
 * Define el comportamiento disponible sin acoplar el dominio a JDBC ni a SQLite.
 */
public interface IRepositorioPrestamo extends IRepositorioExtend<Prestamo, Long> {
    /**
     * Recupera el historial de préstamos asociado a un libro.
     *
     * @param libroId identificador del libro cuyo historial se consulta
     * @return lista de préstamos; vacía si el libro no tiene historial
     * @throws IllegalArgumentException si libroId es nulo
     * @throws RepositorioException si falla la consulta de persistencia
     */
    List<Prestamo> findPrestamosByLibroId(Long libroId);

    /**
     * Marca como devuelto el préstamo indicado usando la fecha actual del sistema.
     * La implementación recupera la entidad, aplica la validación del dominio y
     * persiste la nueva fecha de devolución.
     *
     * @param prestamoId identificador del préstamo que se quiere cerrar
     * @return true si se actualizó al menos una fila; false si la persistencia no informó cambios
     * @throws IllegalArgumentException si prestamoId es nulo
     * @throws IllegalArgumentException si la fecha actual es anterior a la fecha del préstamo
     * @throws RepositorioException si el préstamo no existe o falla la operación JDBC
     */
    boolean cerrarPrestamoHoy(Long prestamoId);

    /**
     * Recupera los préstamos cuya fecha de devolución no está informada; ese valor nulo
     * representa en el modelo un préstamo que continúa activo.
     *
     * @return lista de préstamos activos; vacía si todos han sido devueltos
     * @throws RepositorioException si falla la consulta de persistencia
     */
    List<Prestamo> findPrestamosActivos();
}
