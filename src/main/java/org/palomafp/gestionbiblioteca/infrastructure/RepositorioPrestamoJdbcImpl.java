package org.palomafp.gestionbiblioteca.infrastructure;

import org.palomafp.gestionbiblioteca.domain.IRepositorioPrestamo;
import org.palomafp.gestionbiblioteca.domain.Libro;
import org.palomafp.gestionbiblioteca.domain.Prestamo;

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
            throw new RuntimeException("Error en count() de Préstamos: " + e.getMessage(), e);
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
            throw new RuntimeException("Error en deleteById() de Préstamos: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM prestamos";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error en deleteAll() de Préstamos: " + e.getMessage(), e);
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
            throw new RuntimeException("Error en existsById() de Préstamos: " + e.getMessage(), e);
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

        if (entity.getId() != null && existsById(entity.getId())) {
            // Actualización (UPDATE)
            String sql = "UPDATE prestamos SET fecha_prestamo = ?, fecha_devolucion = ?, libro_id = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, Date.valueOf(entity.getFechaPrestamo()));
                pstmt.setDate(2, entity.getFechaDevolucion() != null ? Date.valueOf(entity.getFechaDevolucion()) : null);
                pstmt.setLong(3, entity.getLibroId());
                pstmt.setLong(4, entity.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error al actualizar Préstamo: " + e.getMessage(), e);
            }
        } else {
            // Inserción (INSERT)
            String sql = "INSERT INTO prestamos (fecha_prestamo, fecha_devolucion, libro_id) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setDate(1, Date.valueOf(entity.getFechaPrestamo()));
                pstmt.setDate(2, entity.getFechaDevolucion() != null ? Date.valueOf(entity.getFechaDevolucion()) : null);
                pstmt.setLong(3, entity.getLibroId());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getLong(1));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al insertar Préstamo: " + e.getMessage(), e);
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
                    Date fechaDev = rs.getDate("fecha_devolucion");
                    Prestamo prestamo = new Prestamo(
                            rs.getLong("id"),
                            rs.getDate("fecha_prestamo").toLocalDate(),
                            fechaDev != null ? fechaDev.toLocalDate() : null,
                            rs.getLong("libro_id")
                    );
                    return Optional.of(prestamo);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en findByIdOptional() de Préstamos: " + e.getMessage(), e);
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
                Date fechaDev = rs.getDate("fecha_devolucion");
                lista.add(new Prestamo(
                        rs.getLong("id"),
                        rs.getDate("fecha_prestamo").toLocalDate(),
                        fechaDev != null ? fechaDev.toLocalDate() : null,
                        rs.getLong("libro_id")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en findAllToList() de Préstamos: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public List<Prestamo> findPrestamosByLibroId(Long libroId) {
        List<Libro> listaLibros = new ArrayList<>(); // por si acaso
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT id, fecha_prestamo, fecha_devolucion, libro_id FROM prestamos WHERE libro_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, libroId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Date fechaDev = rs.getDate("fecha_devolucion");
                    lista.add(new Prestamo(
                            rs.getLong("id"),
                            rs.getDate("fecha_prestamo").toLocalDate(),
                            fechaDev != null ? fechaDev.toLocalDate() : null,
                            rs.getLong("libro_id")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en findPrestamosByLibroId(): " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public void cerrarPrestamoHoy(Long prestamoId) {
        String sql = "UPDATE prestamos SET fecha_devolucion = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setLong(2, prestamoId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en cerrarPrestamoHoy(): " + e.getMessage(), e);
        }
    }
}
