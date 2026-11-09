package org.palomafp.gestionbiblioteca.infrastructure;

import org.palomafp.gestionbiblioteca.domain.Isbn;
import org.palomafp.gestionbiblioteca.domain.IRepositorioLibro;
import org.palomafp.gestionbiblioteca.domain.Libro;
import org.palomafp.gestionbiblioteca.domain.RepositorioException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositorioLibroJdbcImpl implements IRepositorioLibro {

    @Override
    public long count() {
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

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        String sql = "DELETE FROM libros WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositorioException("Error en deleteById() de Libros: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM libros";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RepositorioException("Error en deleteAll() de Libros: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
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

    @Override
    public Libro findById(Long id) {
        return findByIdOptional(id).orElse(null);
    }

    @Override
    public Iterable<Libro> findAll() {
        return findAllToList();
    }

    @Override
    public <S extends Libro> S save(S entity) {
        if (entity == null) throw new IllegalArgumentException("La entidad no puede ser nula");

        if (entity.getId() != null) {
            if (!existsById(entity.getId())) {
                throw new RepositorioException("No existe ningún Libro con id " + entity.getId() + " para actualizar.");
            }
            // Actualización (UPDATE)
            String sql = "UPDATE libros SET isbn = ?, titulo = ?, sede_id = ? WHERE id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, entity.getIsbn().getCodigo());
                pstmt.setString(2, entity.getTitulo());
                pstmt.setLong(3, entity.getSedeId());
                pstmt.setLong(4, entity.getId());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                throw new RepositorioException("Error al actualizar Libro: " + e.getMessage(), e);
            }
        } else {
            // Inserción (INSERT)
            String sql = "INSERT INTO libros (isbn, titulo, sede_id) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, entity.getIsbn().getCodigo());
                pstmt.setString(2, entity.getTitulo());
                pstmt.setLong(3, entity.getSedeId());
                pstmt.executeUpdate();

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

    @Override
    public Optional<Libro> findByIdOptional(Long id) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        String sql = "SELECT id, isbn, titulo, sede_id FROM libros WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToLibro(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findByIdOptional() de Libros: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Libro> findAllToList() {
        List<Libro> lista = new ArrayList<>();
        String sql = "SELECT id, isbn, titulo, sede_id FROM libros";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapRowToLibro(rs));
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findAllToList() de Libros: " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public List<Libro> findBySedeId(Long sedeId) {
        if (sedeId == null) throw new IllegalArgumentException("El id de la sede no puede ser nulo");
        List<Libro> lista = new ArrayList<>();
        String sql = "SELECT id, isbn, titulo, sede_id FROM libros WHERE sede_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, sedeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRowToLibro(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findBySedeId(): " + e.getMessage(), e);
        }
        return lista;
    }

    @Override
    public List<Libro> findByTituloContaining(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto de búsqueda no puede ser nulo ni vacío");
        }
        List<Libro> lista = new ArrayList<>();
        String sql = "SELECT id, isbn, titulo, sede_id FROM libros WHERE titulo LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + texto + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRowToLibro(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepositorioException("Error en findByTituloContaining(): " + e.getMessage(), e);
        }
        return lista;
    }

    private Libro mapRowToLibro(ResultSet rs) throws SQLException {
        return new Libro(
                rs.getLong("id"),
                new Isbn(rs.getString("isbn")),
                rs.getString("titulo"),
                rs.getLong("sede_id")
        );
    }
}
