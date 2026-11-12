package org.palomafp.gestionbiblioteca.domain;

/**
 * Contrato genérico de acceso a una colección de entidades identificadas por una clave.
 * Pertenece al dominio y define las operaciones que necesita la aplicación sin indicar
 * si los datos proceden de SQLite, memoria u otro mecanismo. Esta abstracción permite
 * que la capa de aplicación dependa de operaciones de negocio y no de JDBC.
 *
 * @param <T> tipo de entidad gestionado por el repositorio
 * @param <ID> tipo de dato de su identificador
 */
public interface IRepositorio<T, ID> {
    /**
     * Cuenta las entidades actualmente almacenadas en el repositorio.
     * En una implementación relacional suele ejecutarse una consulta agregada
     * y se devuelve el resultado sin cargar todas las filas en memoria.
     *
     * @return número de entidades persistidas
     * @throws RepositorioException si la implementación no puede consultar el almacenamiento
     */
    long count();

    /**
     * Elimina la entidad cuyo identificador coincide con el recibido. Si no existe,
     * la operación no modifica el almacenamiento. La interfaz no prescribe SQL:
     * cada adaptador debe resolver la eliminación según su tecnología.
     *
     * @param id identificador de la entidad que se quiere borrar
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla la operación de persistencia
     */
    void deleteById(ID id);

    /**
     * Elimina todas las entidades administradas por el repositorio.
     * Debe invocarse con cuidado porque afecta al conjunto completo, no a una fila.
     *
     * @throws RepositorioException si falla la operación de persistencia
     */
    void deleteAll();

    /**
     * Comprueba la existencia de una entidad mediante su clave, evitando recuperar
     * todos sus datos cuando solo se necesita una respuesta afirmativa o negativa.
     *
     * @param id identificador de la entidad que se quiere comprobar
     * @return true si existe una entidad con ese identificador; false en caso contrario
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla la consulta al almacenamiento
     */
    boolean existsById(ID id);

    /**
     * Recupera una entidad por su identificador. El contrato de esta implementación
     * devuelve null cuando la clave no está almacenada; las alternativas que expresan
     * explícitamente la ausencia se ofrecen en IRepositorioExtend.
     *
     * @param id identificador de la entidad que se quiere recuperar
     * @return entidad encontrada o null si no existe
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla la consulta al almacenamiento
     */
    T findById(ID id);

    /**
     * Recupera todas las entidades disponibles. Iterable permite recorrer resultados
     * sin obligar a los consumidores del contrato a conocer una implementación concreta
     * de colección.
     *
     * @return secuencia iterable de entidades; puede estar vacía
     * @throws RepositorioException si falla la consulta al almacenamiento
     */
    Iterable<T> findAll();

    /**
     * Persiste una entidad nueva o actualiza una existente, según las reglas de identidad
     * de la implementación. El tipo genérico S conserva el tipo concreto del argumento
     * en el valor devuelto, y una inserción puede asignar al objeto su identificador generado.
     *
     * @param entity entidad que se va a persistir
     * @param <S> tipo concreto de entidad compatible con T
     * @return la misma entidad ya guardada
     * @throws IllegalArgumentException si entity es nula
     * @throws RepositorioException si no se puede insertar o actualizar la entidad
     */
    <S extends T> S save(S entity);
}
