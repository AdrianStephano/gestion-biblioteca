package org.palomafp.gestionbiblioteca.application;

import org.palomafp.gestionbiblioteca.domain.*;
import org.palomafp.gestionbiblioteca.infrastructure.*;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== PROBANDO GESTIÓN DE BIBLIOTECA JDBC (DDD) ===");

        IRepositorioSede repoSede = new RepositorioSedeJdbcImpl();
        IRepositorioLibro repoLibro = new RepositorioLibroJdbcImpl();
        IRepositorioPrestamo repoPrestamo = new RepositorioPrestamoJdbcImpl();

        // 1. Limpiar datos previos de prueba
        repoPrestamo.deleteAll();
        repoLibro.deleteAll();
        repoSede.deleteAll();

        // 2. Guardar una Sede (Agregado Principal)
        SedeBiblioteca sedeCentral = new SedeBiblioteca(null, "Biblioteca Central", "Madrid");
        repoSede.save(sedeCentral);
        System.out.println("Sede guardada con ID: " + sedeCentral.getId());

        // 3. Guardar un Libro asociado a la Sede (Relación 1:N)
        Libro libro = new Libro(null, new Isbn("9788441540001"), "Clean Architecture en Java", sedeCentral.getId());
        repoLibro.save(libro);
        System.out.println("Libro guardado con ID: " + libro.getId());

        // 4. Guardar un Préstamo asociado al Libro (Relación 1:N)
        Prestamo prestamo = new Prestamo(null, LocalDate.now().minusDays(5), null, libro.getId());
        repoPrestamo.save(prestamo);
        System.out.println("Préstamo registrado con ID: " + prestamo.getId());

        // 5. Comprobar consultas (count, findByIdOptional, findAllToList)
        System.out.println("Total sedes en BD: " + repoSede.count());
        System.out.println("Buscando sede por ID opcional: " + repoSede.findByIdOptional(sedeCentral.getId()).get().getNombre());

        // 6. Comprobar métodos propios de los repositorios
        System.out.println("Libros en la sede central: " + repoLibro.findBySedeId(sedeCentral.getId()).size());

        // 7. Comprobar cierre de préstamo mediante método propio
        repoPrestamo.cerrarPrestamoHoy(prestamo.getId());
        System.out.println("Préstamo cerrado. Fecha devolución actualizada.");

        System.out.println("=== ¡PRUEBAS COMPLETADAS CON ÉXITO! ===");
    }
}
