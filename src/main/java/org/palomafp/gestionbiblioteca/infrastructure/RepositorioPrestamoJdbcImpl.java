package org.palomafp.gestionbiblioteca.infrastructure;

import org.palomafp.gestionbiblioteca.domain.IRepositorioPrestamo;
import org.palomafp.gestionbiblioteca.domain.Prestamo;
import org.palomafp.gestionbiblioteca.domain.RepositorioException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositorioPrestamoJdbcImpl implements IRepositorioPrestamo {

    @Override
    public long count() {
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

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        String sql = "DELETE FROM prestamos WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositorioException("Error en deleteById() de Préstamos: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM prestamos";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RepositorioException("Error en deleteAll() de Préstamos: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
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

    @Override
    public Prestamo findById(Long id) {
        return findByIdOptional(id).orElse(null);
    }

    @Override
    public Iterable<Prestamo> findAll() {
        return findAllToList();
    }

    @Override
    public <S extends Prestamo> S save(S entity) {
        if (entity == null) throw new IllegalArgumentException("La entidad no puede ser nula");

        if (entity.getId() != null) {
            if (!existsById(entity.getId())) {
                throw new RepositorioException("No existe ningún Préstamo con id " + entity.getId() + " para actualizar.");
            }
            // Actualización (UPDATE)
            String sql = "UPDATE prestamos SET fecha_prestamo = ?, fecha_devolucion = ?, libro_id = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, entity.getFechaPrestamo().toString());
                pstmt.setString(2, entity.getFechaDevolucion() != null ? entity.getFechaDevolucion().toString() : null);
                pstmt.setLong(3, entity.getLibroId());
                pstmt.setLong(4, entity.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RepositorioException("Error al actualizar Préstamo: " + e.getMessage(), e);
            }
        } else {
            // Inserción (INSERT)
            String sql = "INSERT INTO prestamos (fecha_prestamo, fecha_devolucion, libro_id) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, entity.getFechaPrestamo().toString());
                pstmt.setString(2, entity.getFechaDevolucion() != null ? entity.getFechaDevolucion().toString() : null);
                pstmt.setLong(3, entity.getLibroId());
                pstmt.executeUpdate();

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

    @Override
    public Optional<Prestamo> findByIdOptional(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        String sql = "SELECT id, fecha_prestamo, fecha_devolucion, libro_id FROM prestamos WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToPrestamo(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findByIdOptional() de Préstamos: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Prestamo> findAllToList() {
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT id, fecha_prestamo, fecha_devolucion, libro_id FROM prestamos";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapRowToPrestamo(rs));
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findAllToList() de Préstamos: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public List<Prestamo> findPrestamosByLibroId(Long libroId) {
        if (libroId == null) throw new IllegalArgumentException("El id del libro no puede ser nulo");
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT id, fecha_prestamo, fecha_devolucion, libro_id FROM prestamos WHERE libro_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, libroId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRowToPrestamo(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findPrestamosByLibroId(): " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public boolean cerrarPrestamoHoy(Long prestamoId) {
        if (prestamoId == null) throw new IllegalArgumentException("El id del préstamo no puede ser nulo");
        Prestamo prestamo = findById(prestamoId);
        if (prestamo == null) {
            throw new RepositorioException("No existe ningún Préstamo con id " + prestamoId + " para cerrar.");
        }
        prestamo.cerrar(LocalDate.now());

        String sql = "UPDATE prestamos SET fecha_devolucion = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prestamo.getFechaDevolucion().toString());
            pstmt.setLong(2, prestamoId);
            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (SQLException e) {
            throw new RepositorioException("Error en cerrarPrestamoHoy(): " + e.getMessage(), e);
        }
    }

    @Override
    public List<Prestamo> findPrestamosActivos() {
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT id, fecha_prestamo, fecha_devolucion, libro_id FROM prestamos WHERE fecha_devolucion IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapRowToPrestamo(rs));
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findPrestamosActivos(): " + e.getMessage(), e);
        }
        return lista;
    }

    private Prestamo mapRowToPrestamo(ResultSet rs) throws SQLException {
        String fechaDevStr = rs.getString("fecha_devolucion");
        return new Prestamo(
                rs.getLong("id"),
                LocalDate.parse(rs.getString("fecha_prestamo")),
                fechaDevStr != null ? LocalDate.parse(fechaDevStr) : null,
                rs.getLong("libro_id")
        );
    }
}
