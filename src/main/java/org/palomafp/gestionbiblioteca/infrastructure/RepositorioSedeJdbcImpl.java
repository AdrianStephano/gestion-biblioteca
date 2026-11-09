package org.palomafp.gestionbiblioteca.infrastructure;

import org.palomafp.gestionbiblioteca.domain.IRepositorioSede;
import org.palomafp.gestionbiblioteca.domain.SedeBiblioteca;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositorioSedeJdbcImpl implements IRepositorioSede {

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM sedes";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en count(): " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        String sql = "DELETE FROM sedes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error en deleteById(): " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM sedes";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error en deleteAll(): " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        String sql = "SELECT 1 FROM sedes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en existsById(): " + e.getMessage(), e);
        }
    }

    @Override
    public SedeBiblioteca findById(Long id) {
        return findByIdOptional(id).orElse(null);
    }

    @Override
    public Iterable<SedeBiblioteca> findAll() {
        return findAllToList();
    }

    @Override
    public <S extends SedeBiblioteca> S save(S entity) {
        if (entity == null) throw new IllegalArgumentException("La entidad no puede ser nula");

        if (entity.getId() != null && existsById(entity.getId())) {
            // Lógica de actualización (UPDATE) exigida por rúbrica
            String sql = "UPDATE sedes SET nombre = ?, ciudad = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, entity.getNombre());
                pstmt.setString(2, entity.getCiudad());
                pstmt.setLong(3, entity.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error al actualizar Sede: " + e.getMessage(), e);
            }
        } else {
            // Lógica de inserción (INSERT)
            String sql = "INSERT INTO sedes (nombre, ciudad) VALUES (?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, entity.getNombre());
                pstmt.setString(2, entity.getCiudad());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getLong(1));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al insertar Sede: " + e.getMessage(), e);
            }
        }
        return entity;
    }

    @Override
    public Optional<SedeBiblioteca> findByIdOptional(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        String sql = "SELECT id, nombre, ciudad FROM sedes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    SedeBiblioteca sede = new SedeBiblioteca(
                            rs.getLong("id"),
                            rs.getString("nombre"),
                            rs.getString("ciudad")
                    );
                    return Optional.of(sede);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en findByIdOptional(): " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<SedeBiblioteca> findAllToList() {
        List<SedeBiblioteca> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, ciudad FROM sedes";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new SedeBiblioteca(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("ciudad")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en findAllToList(): " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public List<SedeBiblioteca> findByCiudad(String ciudad) {
        List<SedeBiblioteca> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, ciudad FROM sedes WHERE ciudad = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ciudad);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new SedeBiblioteca(
                            rs.getLong("id"),
                            rs.getString("nombre"),
                            rs.getString("ciudad")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en findByCiudad(): " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public long contarLibrosEnSede(Long sedeId) {
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
            throw new RuntimeException("Error en contarLibrosEnSede(): " + e.getMessage(), e);
        }
        return 0;
    }
}