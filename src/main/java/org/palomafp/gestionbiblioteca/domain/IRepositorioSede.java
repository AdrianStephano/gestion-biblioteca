package org.palomafp.gestionbiblioteca.domain;

import java.util.List;

/**
 * Interfaz de persistencia de sedes y de las consultas propias de este agregado.
 * Su ubicación en el dominio mantiene independiente la lógica de aplicación del
 * mecanismo JDBC utilizado para guardar y recuperar los datos.
 */
public interface IRepositorioSede extends IRepositorioExtend<SedeBiblioteca, Long> {
    /**
     * Busca las sedes cuyo campo de ciudad coincide con el nombre recibido.
     *
     * @param ciudad nombre no vacío de la ciudad por la que se filtra
     * @return lista de sedes coincidentes; vacía si no hay resultados
     * @throws IllegalArgumentException si ciudad es nula o está en blanco
     * @throws RepositorioException si falla la consulta de persistencia
     */
    List<SedeBiblioteca> findByCiudad(String ciudad);

    /**
     * Cuenta los libros relacionados con la sede indicada. Es una consulta agregada:
     * devuelve el total sin materializar los objetos Libro.
     *
     * @param sedeId identificador de la sede cuyos libros se cuentan
     * @return número de libros asociados
     * @throws IllegalArgumentException si sedeId es nulo
     * @throws RepositorioException si falla la consulta de persistencia
     */
    long contarLibrosEnSede(Long sedeId);
}
