package org.palomafp.gestionbiblioteca.domain;

import java.util.List;
import java.util.Optional;

/**
 * Amplía el contrato CRUD genérico con resultados de consulta más cómodos para
 * la aplicación. Sigue perteneciendo al dominio: describe capacidades, pero no
 * incorpora detalles sobre tablas, SQL ni conexiones.
 *
 * @param <T> tipo de entidad gestionado
 * @param <ID> tipo del identificador de la entidad
 */
public interface IRepositorioExtend<T, ID> extends IRepositorio<T,ID> {
    /**
     * Busca una entidad por identificador y representa su posible ausencia mediante
     * Optional, evitando devolver null en este método.
     *
     * @param id identificador de la entidad
     * @return Optional con la entidad si existe, o vacío si no se encontró
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla la consulta al almacenamiento
     */
    Optional<T> findByIdOptional(ID id);

    /**
     * Recupera todas las entidades en una lista, útil cuando quien consume el
     * repositorio necesita una colección concreta para iterar o procesar.
     *
     * @return lista de entidades; vacía si no hay registros
     * @throws RepositorioException si falla la consulta al almacenamiento
     */
    List<T> findAllToList();
}
