package org.palomafp.gestionbiblioteca.infrastructure;

import org.palomafp.gestionbiblioteca.domain.IRepositorioSede;
import org.palomafp.gestionbiblioteca.domain.RepositorioException;
import org.palomafp.gestionbiblioteca.domain.SedeBiblioteca;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC que implementa la interfaz IRepositorioSede sobre la tabla sedes
 * de SQLite. Traduce entre filas relacionales y entidades del dominio, y convierte
 * los errores SQL a RepositorioException. La capa de aplicación puede utilizar la
 * interfaz sin conocer las sentencias ni los recursos JDBC.
 */
public class RepositorioSedeJdbcImpl implements IRepositorioSede {

    /**
     * Cuenta las sedes mediante una consulta agregada, sin cargar los objetos completos.
     *
     * @return número de sedes
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public long count() {
        // COUNT(*) devuelve el total en la primera columna de una fila.
        String sql = "SELECT COUNT(*) FROM sedes";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en count() de Sedes: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Elimina la sede que coincide con id. Si no existe, el DELETE afecta a cero filas.
     * SQLite aplica las relaciones declaradas en schema.sql según la configuración
     * de claves foráneas de la URL JDBC.
     *
     * @param id identificador de la sede
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public void deleteById(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        // El identificador se enlaza con PreparedStatement, nunca concatenado al SQL.
        String sql = "DELETE FROM sedes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositorioException("Error en deleteById() de Sedes: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina todas las sedes. Las claves foráneas pueden impedirlo o propagar el borrado
     * según el esquema; en la demostración se borran antes libros y préstamos.
     *
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public void deleteAll() {
        // La sentencia es fija y no requiere parámetros, por lo que Statement resulta suficiente.
        String sql = "DELETE FROM sedes";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RepositorioException("Error en deleteAll() de Sedes: " + e.getMessage(), e);
        }
    }

    /**
     * Comprueba si hay una fila con la clave indicada sin recuperar nombre ni ciudad.
     *
     * @param id identificador de la sede
     * @return true si existe
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public boolean existsById(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        // SELECT 1 y rs.next() bastan para responder existencia.
        String sql = "SELECT 1 FROM sedes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en existsById() de Sedes: " + e.getMessage(), e);
        }
    }

    /**
     * Busca una sede por id y devuelve null si no existe, manteniendo el contrato base.
     *
     * @param id identificador de la sede
     * @return la sede, o null si no existe
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla la consulta JDBC
     */
    @Override
    public SedeBiblioteca findById(Long id) {
        return findByIdOptional(id).orElse(null);
    }

    /**
     * Expone todas las sedes como Iterable, según el contrato genérico del repositorio.
     *
     * @return sedes almacenadas
     * @throws RepositorioException si falla la consulta JDBC
     */
    @Override
    public Iterable<SedeBiblioteca> findAll() {
        return findAllToList();
    }

    /**
     * Inserta la sede si todavía no tiene identificador, o actualiza una fila existente.
     * Antes de actualizar se comprueba la existencia para distinguir una entidad persistida
     * de una clave arbitraria. Los campos se envían mediante marcadores parametrizados.
     *
     * @param entity sede a guardar
     * @return la sede guardada, con el id asignado si era nueva
     * @throws IllegalArgumentException si entity es nula
     * @throws RepositorioException si tiene id pero no existe, o si falla la base de datos
     */
    @Override
    public <S extends SedeBiblioteca> S save(S entity) {
        if (entity == null) throw new IllegalArgumentException("La entidad no puede ser nula");

        if (entity.getId() != null) {
            // Se valida que haya una fila destino antes de ejecutar UPDATE.
            if (!existsById(entity.getId())) {
                throw new RepositorioException("No existe ninguna Sede con id " + entity.getId() + " para actualizar.");
            }
            // UPDATE conserva la clave y modifica los valores descriptivos de la sede.
            String sql = "UPDATE sedes SET nombre = ?, ciudad = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Los valores se enlazan por posición, en el mismo orden de los marcadores ?.
                pstmt.setString(1, entity.getNombre());
                pstmt.setString(2, entity.getCiudad());
                pstmt.setLong(3, entity.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RepositorioException("Error al actualizar Sede: " + e.getMessage(), e);
            }
        } else {
            // Al omitir id, SQLite crea la clave AUTOINCREMENT declarada en schema.sql.
            String sql = "INSERT INTO sedes (nombre, ciudad) VALUES (?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, entity.getNombre());
                pstmt.setString(2, entity.getCiudad());
                pstmt.executeUpdate();

                // Recupera la clave generada y actualiza la entidad en memoria.
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getLong(1));
                    }
                }
            } catch (SQLException e) {
                throw new RepositorioException("Error al insertar Sede: " + e.getMessage(), e);
            }
        }
        return entity;
    }

    /**
     * Busca una sede por clave primaria y representa la ausencia mediante Optional vacío.
     *
     * @param id identificador de la sede
     * @return Optional con la sede, o vacío si no existe
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public Optional<SedeBiblioteca> findByIdOptional(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        // Se enumeran las columnas necesarias para reconstruir la entidad de dominio.
        String sql = "SELECT id, nombre, ciudad FROM sedes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // La clave primaria solo permite como máximo una fila coincidente.
                    return Optional.of(mapRowToSede(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findByIdOptional() de Sedes: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Ejecuta una consulta completa y construye una lista con todas las sedes.
     *
     * @return lista de sedes
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public List<SedeBiblioteca> findAllToList() {
        List<SedeBiblioteca> lista = new ArrayList<>();
        // No hay filtros, por lo que se utiliza Statement en vez de enlazar parámetros.
        String sql = "SELECT id, nombre, ciudad FROM sedes";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // Cada llamada a next() posiciona el cursor en la siguiente fila obtenida.
                lista.add(mapRowToSede(rs));
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findAllToList() de Sedes: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Filtra sedes por ciudad mediante una comparación parametrizada.
     *
     * @param ciudad nombre de la ciudad
     * @return lista de sedes de la ciudad
     * @throws IllegalArgumentException si ciudad es nula o vacía
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public List<SedeBiblioteca> findByCiudad(String ciudad) {
        if (ciudad == null || ciudad.isBlank()) {
            throw new IllegalArgumentException("La ciudad no puede ser nula ni vacía");
        }
        List<SedeBiblioteca> lista = new ArrayList<>();
        // La ciudad se enlaza separada del SQL, evitando concatenación de datos.
        String sql = "SELECT id, nombre, ciudad FROM sedes WHERE ciudad = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ciudad);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Se agregan todas las sedes que coincidan con el valor exacto.
                    lista.add(mapRowToSede(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findByCiudad(): " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Cuenta en la tabla libros las filas cuya clave foránea referencia esta sede.
     *
     * @param sedeId identificador de la sede
     * @return número de libros
     * @throws IllegalArgumentException si sedeId es nulo
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public long contarLibrosEnSede(Long sedeId) {
        if (sedeId == null) throw new IllegalArgumentException("El id de la sede no puede ser nulo");
        // COUNT evita cargar los libros y devuelve únicamente el total solicitado.
        String sql = "SELECT COUNT(*) FROM libros WHERE sede_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, sedeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en contarLibrosEnSede(): " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Convierte la fila actual del ResultSet en una sede del dominio.
     *
     * @param rs cursor posicionado en la fila que se debe mapear
     * @return entidad con identificador, nombre y ciudad leídos de SQLite
     * @throws SQLException si no se puede acceder a alguna columna
     * @throws IllegalArgumentException si los valores leídos incumplen las validaciones del dominio
     */
    private SedeBiblioteca mapRowToSede(ResultSet rs) throws SQLException {
        // El constructor vuelve a validar los datos, incluso si se recuperan desde persistencia.
        return new SedeBiblioteca(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("ciudad")
        );
    }
}
