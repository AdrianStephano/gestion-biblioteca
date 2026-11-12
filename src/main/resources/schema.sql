-- Tabla principal de sedes bibliotecarias.
CREATE TABLE IF NOT EXISTS sedes (
    -- Clave primaria numérica asignada automáticamente por SQLite.
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    -- Datos obligatorios: NOT NULL impide almacenar valores SQL nulos.
    nombre TEXT NOT NULL,
    ciudad TEXT NOT NULL
);

-- Catálogo de publicaciones, cada libro está asociado a una sede.
CREATE TABLE IF NOT EXISTS libros (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    isbn TEXT NOT NULL,
    titulo TEXT NOT NULL,
    -- Clave foránea que relaciona libros con sedes.
    sede_id INTEGER NOT NULL,
    -- Si se elimina la sede, SQLite elimina también sus libros relacionados.
    FOREIGN KEY (sede_id) REFERENCES sedes(id) ON DELETE CASCADE
);

-- Historial de préstamos. Un libro puede tener múltiples préstamos a lo largo del tiempo.
CREATE TABLE IF NOT EXISTS prestamos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    -- LocalDate se almacena como texto ISO-8601, por ejemplo 2026-11-10.
    fecha_prestamo TEXT NOT NULL,
    -- NULL representa un préstamo activo que todavía no se ha devuelto.
    fecha_devolucion TEXT,
    libro_id INTEGER NOT NULL,
    -- Al eliminar un libro se eliminan sus registros de préstamo dependientes.
    FOREIGN KEY (libro_id) REFERENCES libros(id) ON DELETE CASCADE
);
