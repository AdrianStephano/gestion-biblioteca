package org.palomafp.gestionbiblioteca.infrastructure;

import org.palomafp.gestionbiblioteca.domain.IRepositorioPrestamo;
import org.palomafp.gestionbiblioteca.domain.Prestamo;
import org.palomafp.gestionbiblioteca.domain.RepositorioException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC de la interfaz de préstamos para la tabla prestamos de SQLite.
 * Convierte fechas LocalDate al formato ISO-8601 textual de la tabla y reconstruye
 * entidades al leer filas. Los detalles de Connection, PreparedStatement y ResultSet
 * quedan confinados aquí; los errores SQL se traducen a RepositorioException.
 */
public class RepositorioPrestamoJdbcImpl implements IRepositorioPrestamo {

    /**
     * Cuenta las filas de préstamos sin recuperar los datos de cada préstamo.
     *
     * @return número de préstamos
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public long count() {
        // COUNT(*) devuelve un resultado agregado de una sola columna.
        String sql = "SELECT COUNT(*) FROM prestamos";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en count() de Préstamos: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Elimina el préstamo cuya clave primaria coincide con id. Un identificador
     * inexistente no produce error: el DELETE afecta a cero filas.
     *
     * @param id identificador del préstamo
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public void deleteById(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        // La clave se enlaza como parámetro para evitar construir SQL por concatenación.
        String sql = "DELETE FROM prestamos WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositorioException("Error en deleteById() de Préstamos: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina todas las filas de préstamos. Se ejecuta antes de borrar libros para
     * respetar la dependencia establecida por la clave foránea.
     *
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public void deleteAll() {
        // No hay parámetros externos, por eso se usa Statement en lugar de PreparedStatement.
        String sql = "DELETE FROM prestamos";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RepositorioException("Error en deleteAll() de Préstamos: " + e.getMessage(), e);
        }
    }

    /**
     * Comprueba la existencia por clave primaria sin cargar las columnas del préstamo.
     *
     * @param id identificador del préstamo
     * @return true si existe
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public boolean existsById(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        // SELECT 1 permite detener la comprobación al encontrar una coincidencia.
        String sql = "SELECT 1 FROM prestamos WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en existsById() de Préstamos: " + e.getMessage(), e);
        }
    }

    /**
     * Busca un préstamo y adapta el resultado opcional al contrato base, devolviendo
     * null cuando no existe.
     *
     * @param id identificador del préstamo
     * @return el préstamo, o null si no existe
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla la consulta JDBC
     */
    @Override
    public Prestamo findById(Long id) {
        return findByIdOptional(id).orElse(null);
    }

    /**
     * Devuelve todos los préstamos mediante la vista Iterable del contrato genérico.
     *
     * @return préstamos almacenados
     * @throws RepositorioException si falla la consulta JDBC
     */
    @Override
    public Iterable<Prestamo> findAll() {
        return findAllToList();
    }

    /**
     * Inserta un préstamo nuevo cuando id es null, o actualiza el ya existente cuando
     * contiene una clave. Las fechas se convierten a texto ISO-8601 para almacenarse;
     * una fecha de devolución null se enlaza como SQL NULL y representa un préstamo activo.
     *
     * @param entity préstamo a guardar
     * @return el préstamo guardado, con el id asignado si era nuevo
     * @throws IllegalArgumentException si entity es nula
     * @throws RepositorioException si tiene id pero no existe, o si falla la base de datos
     */
    @Override
    public <S extends Prestamo> S save(S entity) {
        if (entity == null) throw new IllegalArgumentException("La entidad no puede ser nula");

        if (entity.getId() != null) {
            // Evita ejecutar un UPDATE que no corresponde a una entidad existente.
            if (!existsById(entity.getId())) {
                throw new RepositorioException("No existe ningún Préstamo con id " + entity.getId() + " para actualizar.");
            }
            // UPDATE modifica los campos de negocio sin alterar la identidad del préstamo.
            String sql = "UPDATE prestamos SET fecha_prestamo = ?, fecha_devolucion = ?, libro_id = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // LocalDate.toString() produce ISO-8601, que puede recuperarse con LocalDate.parse().
                pstmt.setString(1, entity.getFechaPrestamo().toString());
                // El condicional mantiene SQL NULL para un préstamo que todavía no se ha devuelto.
                pstmt.setString(2, entity.getFechaDevolucion() != null ? entity.getFechaDevolucion().toString() : null);
                pstmt.setLong(3, entity.getLibroId());
                pstmt.setLong(4, entity.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RepositorioException("Error al actualizar Préstamo: " + e.getMessage(), e);
            }
        } else {
            // La clave primaria se omite para que SQLite asigne el valor AUTOINCREMENT.
            String sql = "INSERT INTO prestamos (fecha_prestamo, fecha_devolucion, libro_id) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, entity.getFechaPrestamo().toString());
                pstmt.setString(2, entity.getFechaDevolucion() != null ? entity.getFechaDevolucion().toString() : null);
                pstmt.setLong(3, entity.getLibroId());
                pstmt.executeUpdate();

                // Se lee la clave generada para que el objeto Java conozca su identidad persistente.
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getLong(1));
                    }
                }
            } catch (SQLException e) {
                throw new RepositorioException("Error al insertar Préstamo: " + e.getMessage(), e);
            }
        }
        return entity;
    }

    /**
     * Recupera una fila por clave y devuelve Optional.empty() cuando no existe.
     *
     * @param id identificador del préstamo
     * @return Optional con el préstamo, o vacío si no existe
     * @throws IllegalArgumentException si id es nulo
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public Optional<Prestamo> findByIdOptional(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        // Se enumeran columnas explícitamente para que el mapeo no dependa de su orden físico.
        String sql = "SELECT id, fecha_prestamo, fecha_devolucion, libro_id FROM prestamos WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // El cursor está en la única fila posible porque id es clave primaria.
                    return Optional.of(mapRowToPrestamo(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findByIdOptional() de Préstamos: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Recupera cada fila de la tabla y construye una lista de entidades de dominio.
     *
     * @return lista de préstamos
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public List<Prestamo> findAllToList() {
        List<Prestamo> lista = new ArrayList<>();
        // Esta consulta no tiene condiciones ni parámetros; Statement es suficiente.
        String sql = "SELECT id, fecha_prestamo, fecha_devolucion, libro_id FROM prestamos";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // El cursor avanza fila por fila y cada resultado se transforma antes de continuar.
                lista.add(mapRowToPrestamo(rs));
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findAllToList() de Préstamos: " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Filtra el historial usando la clave foránea libro_id y un parámetro enlazado.
     *
     * @param libroId identificador del libro
     * @return lista de préstamos del libro
     * @throws IllegalArgumentException si libroId es nulo
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public List<Prestamo> findPrestamosByLibroId(Long libroId) {
        if (libroId == null) throw new IllegalArgumentException("El id del libro no puede ser nulo");
        List<Prestamo> lista = new ArrayList<>();
        // La condición usa una columna relacional, sin concatenar el valor recibido.
        String sql = "SELECT id, fecha_prestamo, fecha_devolucion, libro_id FROM prestamos WHERE libro_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, libroId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Un libro puede tener múltiples filas de historial.
                    lista.add(mapRowToPrestamo(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findPrestamosByLibroId(): " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Recupera el préstamo, aplica su operación de dominio cerrar y persiste la fecha
     * actual. La validación temporal permanece en Prestamo para compartirla con otros usos.
     *
     * @param prestamoId identificador del préstamo
     * @return true si se actualizó el registro
     * @throws IllegalArgumentException si prestamoId es nulo o la fecha de hoy es anterior al préstamo
     * @throws RepositorioException si el préstamo no existe o falla la base de datos
     */
    @Override
    public boolean cerrarPrestamoHoy(Long prestamoId) {
        if (prestamoId == null) throw new IllegalArgumentException("El id del préstamo no puede ser nulo");
        // Se carga la entidad para comprobar existencia y reutilizar las reglas del dominio.
        Prestamo prestamo = findById(prestamoId);
        if (prestamo == null) {
            throw new RepositorioException("No existe ningún Préstamo con id " + prestamoId + " para cerrar.");
        }
        // cerrar valida que hoy no sea anterior a la fecha original del préstamo.
        prestamo.cerrar(LocalDate.now());

        // Solo se modifica el atributo de devolución; el id selecciona el registro objetivo.
        String sql = "UPDATE prestamos SET fecha_devolucion = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prestamo.getFechaDevolucion().toString());
            pstmt.setLong(2, prestamoId);
            int filasAfectadas = pstmt.executeUpdate();
            // El resultado JDBC indica si se modificó alguna fila.
            return filasAfectadas > 0;
        } catch (SQLException e) {
            throw new RepositorioException("Error en cerrarPrestamoHoy(): " + e.getMessage(), e);
        }
    }

    /**
     * Busca los préstamos con fecha_devolucion SQL NULL, convención que representa
     * los préstamos todavía no cerrados.
     *
     * @return lista de préstamos activos
     * @throws RepositorioException si falla el acceso a la base de datos
     */
    @Override
    public List<Prestamo> findPrestamosActivos() {
        List<Prestamo> lista = new ArrayList<>();
        // IS NULL es la comparación correcta para NULL en SQL; "= NULL" no devolvería filas.
        String sql = "SELECT id, fecha_prestamo, fecha_devolucion, libro_id FROM prestamos WHERE fecha_devolucion IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // Cada fila activa se convierte al mismo modelo usado en las demás consultas.
                lista.add(mapRowToPrestamo(rs));
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findPrestamosActivos(): " + e.getMessage(), e);
        }
        return lista;
    }

    /**
     * Mapea la fila actual del cursor JDBC a una entidad Prestamo.
     *
     * @param rs ResultSet posicionado en la fila que se debe convertir
     * @return préstamo reconstruido con sus fechas y clave asociada
     * @throws SQLException si no se puede leer una columna
     * @throws java.time.format.DateTimeParseException si una fecha persistida no está en formato ISO-8601
     * @throws IllegalArgumentException si los valores leídos incumplen las reglas del constructor de Prestamo
     */
    private Prestamo mapRowToPrestamo(ResultSet rs) throws SQLException {
        // Una fecha SQL NULL se representa como null; de lo contrario se analiza como LocalDate.
        String fechaDevStr = rs.getString("fecha_devolucion");
        return new Prestamo(
                rs.getLong("id"),
                LocalDate.parse(rs.getString("fecha_prestamo")),
                fechaDevStr != null ? LocalDate.parse(fechaDevStr) : null,
                rs.getLong("libro_id")
        );
    }
}
