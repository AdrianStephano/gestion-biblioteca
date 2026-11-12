package org.palomafp.gestionbiblioteca.infrastructure;

import org.palomafp.gestionbiblioteca.domain.Isbn;
import org.palomafp.gestionbiblioteca.domain.IRepositorioLibro;
import org.palomafp.gestionbiblioteca.domain.Libro;
import org.palomafp.gestionbiblioteca.domain.RepositorioException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC que implementa la interfaz IRepositorioLibro sobre la tabla libros
 * de SQLite. Traduce objetos Libro a instrucciones SQL y filas ResultSet a entidades
 * del dominio. Así, las interfaces y reglas de negocio no necesitan conocer JDBC.
 * Los parámetros se enlazan mediante PreparedStatement y los recursos se cierran
 * automáticamente con try-with-resources.
 */
public class RepositorioLibroJdbcImpl implements IRepositorioLibro {

    /**
     * Cuenta las filas de libros sin recuperar sus columnas.
     *
     * @return número de libros
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public long count() {
        // COUNT(*) delega el cálculo en SQLite y devuelve una sola fila agregada.
        String sql = "SELECT COUNT(*) FROM libros";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en count() de Libros: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Borra por clave primaria el libro indicado. La consulta parametrizada evita
     * concatenar el identificador en el texto SQL; una clave inexistente simplemente
     * hace que SQLite afecte a cero filas.
     *
     * @param id identificador del libro
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public void deleteById(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        // El marcador ? se completa con setLong antes de ejecutar la sentencia.
        String sql = "DELETE FROM libros WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositorioException("Error en deleteById() de Libros: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina todas las filas de libros mediante una sentencia sin filtro WHERE.
     * Se utiliza durante la demostración, después de borrar préstamos dependientes.
     *
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public void deleteAll() {
        // Statement es suficiente porque esta sentencia no recibe valores externos.
        String sql = "DELETE FROM libros";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RepositorioException("Error en deleteAll() de Libros: " + e.getMessage(), e);
        }
    }

    /**
     * Comprueba la existencia por clave primaria sin cargar el objeto completo.
     *
     * @param id identificador del libro
     * @return true si existe
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public boolean existsById(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        // SELECT 1 solo necesita producir una fila para que rs.next() confirme existencia.
        String sql = "SELECT 1 FROM libros WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en existsById() de Libros: " + e.getMessage(), e);
        }
    }

    /**
     * Busca un libro por id y conserva la convención del contrato base de devolver null
     * cuando no hay coincidencia. La consulta real se centraliza en findByIdOptional.
     *
     * @param id identificador del libro
     * @return el libro, o null si no existe
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla la consulta JDBC
     */
    @Override
    public Libro findById(Long id) {
        return findByIdOptional(id).orElse(null);
    }

    /**
     * Expone los resultados como Iterable para cumplir el contrato genérico del repositorio.
     *
     * @return libros almacenados
     * @throws RepositorioException si falla la consulta JDBC
     */
    @Override
    public Iterable<Libro> findAll() {
        return findAllToList();
    }

    /**
     * Persiste un libro aplicando una estrategia de inserción o actualización: id null
     * representa una entidad nueva; id no null exige que ya exista una fila y provoca UPDATE.
     * Los datos se enlazan como parámetros, y en una inserción se copia al objeto la clave
     * generada por SQLite cuando el controlador la devuelve.
     *
     * @param entity libro a guardar
     * @return el libro guardado, con el id asignado si era nuevo
     * @throws IllegalArgumentException si entity es nula
     * @throws RepositorioException si tiene id pero no existe, o si falla la base de datos
     */
    @Override
    public <S extends Libro> S save(S entity) {
        if (entity == null) throw new IllegalArgumentException("La entidad no puede ser nula");

        if (entity.getId() != null) {
            // Se comprueba antes que la intención de UPDATE no se convierta en una inserción accidental.
            if (!existsById(entity.getId())) {
                throw new RepositorioException("No existe ningún Libro con id " + entity.getId() + " para actualizar.");
            }
            // UPDATE mantiene la misma identidad y modifica solo los campos editables del libro.
            String sql = "UPDATE libros SET isbn = ?, titulo = ?, sede_id = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Cada setter enlaza un valor al marcador correspondiente, no modifica el SQL.
                pstmt.setString(1, entity.getIsbn().getCodigo());
                pstmt.setString(2, entity.getTitulo());
                pstmt.setLong(3, entity.getSedeId());
                pstmt.setLong(4, entity.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RepositorioException("Error al actualizar Libro: " + e.getMessage(), e);
            }
        } else {
            // INSERT omite id porque SQLite lo genera mediante INTEGER PRIMARY KEY AUTOINCREMENT.
            String sql = "INSERT INTO libros (isbn, titulo, sede_id) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, entity.getIsbn().getCodigo());
                pstmt.setString(2, entity.getTitulo());
                pstmt.setLong(3, entity.getSedeId());
                pstmt.executeUpdate();

                // El controlador expone la clave generada para sincronizar la entidad Java con la fila.
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getLong(1));
                    }
                }
            } catch (SQLException e) {
                throw new RepositorioException("Error al insertar Libro: " + e.getMessage(), e);
            }
        }
        return entity;
    }

    /**
     * Busca un libro por clave y representa ausencia con Optional.empty(), no con null.
     *
     * @param id identificador del libro
     * @return Optional con el libro, o vacío si no existe
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public Optional<Libro> findByIdOptional(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        // Solo se seleccionan las columnas necesarias para reconstruir Libro.
        String sql = "SELECT id, isbn, titulo, sede_id FROM libros WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // El ResultSet está posicionado en la fila coincidente; se convierte al modelo.
                    return Optional.of(mapRowToLibro(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findByIdOptional() de Libros: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Ejecuta una consulta sin filtro y transforma cada fila devuelta a una entidad Libro.
     *
     * @return lista de libros
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public List<Libro> findAllToList() {
        List<Libro> lista = new ArrayList<>();
        // Statement se usa porque la consulta no tiene parámetros.
        String sql = "SELECT id, isbn, titulo, sede_id FROM libros";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // rs.next() avanza por el cursor JDBC; cada fila se mapea antes de avanzar de nuevo.
                lista.add(mapRowToLibro(rs));
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findAllToList() de Libros: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Filtra libros por sede usando una clave enlazada y devuelve todas las coincidencias.
     *
     * @param sedeId identificador de la sede
     * @return lista de libros de la sede
     * @throws IllegalArgumentException si sedeId es nulo
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public List<Libro> findBySedeId(Long sedeId) {
        if (sedeId == null) throw new IllegalArgumentException("El id de la sede no puede ser nulo");
        List<Libro> lista = new ArrayList<>();
        // La clave foránea sede_id permite realizar la búsqueda relacional.
        String sql = "SELECT id, isbn, titulo, sede_id FROM libros WHERE sede_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, sedeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Puede haber varios libros asociados a la misma sede.
                    lista.add(mapRowToLibro(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findBySedeId(): " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Busca títulos que contengan el fragmento indicado. Los comodines % se añaden
     * al valor de búsqueda y se envían como parámetro, manteniendo el SQL parametrizado.
     *
     * @param texto texto a buscar
     * @return lista de libros coincidentes
     * @throws IllegalArgumentException si texto es nulo o vacío
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public List<Libro> findByTituloContaining(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto de búsqueda no puede ser nulo ni vacío");
        }
        List<Libro> lista = new ArrayList<>();
        // LIKE compara el título con el patrón; el comportamiento de mayúsculas depende de SQLite.
        String sql = "SELECT id, isbn, titulo, sede_id FROM libros WHERE titulo LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + texto + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // La búsqueda parcial puede producir cero, una o muchas filas.
                    lista.add(mapRowToLibro(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findByTituloContaining(): " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Convierte la fila actual del ResultSet en una entidad de dominio.
     * Se invoca únicamente mientras el cursor JDBC está posicionado en una fila.
     *
     * @param rs cursor ubicado en la fila que se quiere mapear
     * @return entidad Libro reconstruida con columnas de la tabla
     * @throws SQLException si no se puede leer alguna columna del ResultSet
     * @throws IllegalArgumentException si el ISBN almacenado incumple la validación del dominio
     */
    private Libro mapRowToLibro(ResultSet rs) throws SQLException {
        // El constructor Isbn vuelve a aplicar la regla de formato al cargar datos persistidos.
        return new Libro(
                rs.getLong("id"),
                new Isbn(rs.getString("isbn")),
                rs.getString("titulo"),
                rs.getLong("sede_id")
        );
    }
}
