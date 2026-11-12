package org.palomafp.gestionbiblioteca.domain;

import java.util.List;

/**
 * Interfaz de persistencia y consulta de libros. Sus operaciones específicas expresan
 * búsquedas útiles para la gestión bibliotecaria; la capa de infraestructura proporciona
 * la implementación JDBC y la aplicación puede depender de esta interfaz.
 */
public interface IRepositorioLibro extends IRepositorioExtend<Libro, Long> {
    /**
     * Recupera los libros asociados a una sede concreta.
     *
     * @param sedeId identificador de la sede que se quiere consultar
     * @return lista de libros de esa sede; vacía si no hay coincidencias
     * @throws IllegalArgumentException si sedeId es nulo
     * @throws RepositorioException si falla la consulta de persistencia
     */
    List<Libro> findBySedeId(Long sedeId);

    /**
     * Busca libros cuyo título contiene el fragmento indicado. La interfaz expresa
     * el criterio funcional; cada adaptador decide cómo traducirlo a su almacenamiento.
     *
     * @param texto fragmento no vacío que se buscará en el título
     * @return lista de libros coincidentes; vacía si no hay resultados
     * @throws IllegalArgumentException si texto es nulo o está en blanco
     * @throws RepositorioException si falla la consulta de persistencia
     */
    List<Libro> findByTituloContaining(String texto);
}
