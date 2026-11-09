package org.palomafp.gestionbiblioteca.application;

import org.palomafp.gestionbiblioteca.domain.*;
import org.palomafp.gestionbiblioteca.infrastructure.*;

import java.time.LocalDate;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== PROBANDO GESTIÓN DE BIBLIOTECA JDBC (DDD) ===");

        // 0. Inicialización del esquema de la base de datos
        DatabaseConnection.initSchema();
        System.out.println("Esquema de la base de datos inicializado correctamente.\n");

        IRepositorioSede repoSede = new RepositorioSedeJdbcImpl();
        IRepositorioLibro repoLibro = new RepositorioLibroJdbcImpl();
        IRepositorioPrestamo repoPrestamo = new RepositorioPrestamoJdbcImpl();

        // Limpiar datos previos de prueba
        repoPrestamo.deleteAll();
        repoLibro.deleteAll();
        repoSede.deleteAll();

        // ============================================================
        // 1. CICLO DE VIDA DE save(): INSERCIÓN
        // ============================================================
        System.out.println("--- 1. INSERCIÓN (save) ---");
        SedeBiblioteca sedeCentral = new SedeBiblioteca(null, "Biblioteca Central", "Madrid");
        repoSede.save(sedeCentral);
        System.out.println("Sede guardada: " + sedeCentral);

        SedeBiblioteca sedeNorte = new SedeBiblioteca(null, "Biblioteca Norte", "Bilbao");
        repoSede.save(sedeNorte);
        System.out.println("Sede guardada: " + sedeNorte);

        Libro libro = new Libro(null, new Isbn("9788441540001"), "Clean Architecture en Java", sedeCentral.getId());
        repoLibro.save(libro);
        System.out.println("Libro guardado: " + libro);

        Libro libro2 = new Libro(null, new Isbn("9788441540018"), "Domain-Driven Design Aplicado", sedeCentral.getId());
        repoLibro.save(libro2);
        System.out.println("Libro guardado: " + libro2);

        Prestamo prestamo = new Prestamo(null, LocalDate.now().minusDays(5), null, libro.getId());
        repoPrestamo.save(prestamo);
        System.out.println("Préstamo guardado: " + prestamo);

        Prestamo prestamo2 = new Prestamo(null, LocalDate.now().minusDays(2), null, libro2.getId());
        repoPrestamo.save(prestamo2);
        System.out.println("Préstamo guardado: " + prestamo2);

        // ============================================================
        // 2. CICLO DE VIDA DE save(): ACTUALIZACIÓN (buscar y modificar)
        // ============================================================
        System.out.println("\n--- 2. ACTUALIZACIÓN (save tras búsqueda) ---");
        SedeBiblioteca sedeEncontrada = repoSede.findById(sedeCentral.getId());
        sedeEncontrada.setNombre("Biblioteca Central Renovada");
        repoSede.save(sedeEncontrada);
        System.out.println("Sede actualizada: " + repoSede.findById(sedeCentral.getId()));

        // ============================================================
        // 3. MÉTODOS DE IRepositorio: count(), existsById(), findById(), findAll()
        // ============================================================
        System.out.println("\n--- 3. MÉTODOS DE IRepositorio ---");
        System.out.println("Total sedes: " + repoSede.count());
        System.out.println("Total libros: " + repoLibro.count());
        System.out.println("Total préstamos: " + repoPrestamo.count());

        System.out.println("¿Existe sede con id " + sedeCentral.getId() + "?: " + repoSede.existsById(sedeCentral.getId()));
        System.out.println("¿Existe libro con id " + libro.getId() + "?: " + repoLibro.existsById(libro.getId()));
        System.out.println("¿Existe préstamo con id " + prestamo.getId() + "?: " + repoPrestamo.existsById(prestamo.getId()));

        System.out.println("findById sede: " + repoSede.findById(sedeCentral.getId()));
        System.out.println("findById libro: " + repoLibro.findById(libro.getId()));
        System.out.println("findById préstamo: " + repoPrestamo.findById(prestamo.getId()));

        System.out.println("findAll sedes:");
        repoSede.findAll().forEach(s -> System.out.println("  " + s));
        System.out.println("findAll libros:");
        repoLibro.findAll().forEach(l -> System.out.println("  " + l));
        System.out.println("findAll préstamos:");
        repoPrestamo.findAll().forEach(p -> System.out.println("  " + p));

        // ============================================================
        // 4. MÉTODOS DE IRepositorioExtend: findByIdOptional(), findAllToList()
        // ============================================================
        System.out.println("\n--- 4. MÉTODOS DE IRepositorioExtend ---");
        Optional<SedeBiblioteca> sedeOpt = repoSede.findByIdOptional(sedeCentral.getId());
        System.out.println("findByIdOptional (con valor): " + sedeOpt.isPresent() + " -> " + sedeOpt.orElse(null));

        Optional<SedeBiblioteca> sedeVacia = repoSede.findByIdOptional(999999L);
        System.out.println("findByIdOptional (vacío): " + sedeVacia.isPresent());

        System.out.println("findAllToList sedes: " + repoSede.findAllToList());
        System.out.println("findAllToList libros: " + repoLibro.findAllToList());
        System.out.println("findAllToList préstamos: " + repoPrestamo.findAllToList());

        // ============================================================
        // 5. MÉTODOS PROPIOS DE CADA REPOSITORIO
        // ============================================================
        System.out.println("\n--- 5. MÉTODOS PROPIOS ---");

        // Sede
        System.out.println("findByCiudad('Madrid'): " + repoSede.findByCiudad("Madrid"));
        System.out.println("contarLibrosEnSede(sedeCentral): " + repoSede.contarLibrosEnSede(sedeCentral.getId()));

        // Libro
        System.out.println("findBySedeId(sedeCentral): " + repoLibro.findBySedeId(sedeCentral.getId()));
        System.out.println("findByTituloContaining('Design'): " + repoLibro.findByTituloContaining("Design"));

        // Préstamo
        System.out.println("findPrestamosByLibroId(libro): " + repoPrestamo.findPrestamosByLibroId(libro.getId()));
        System.out.println("findPrestamosActivos(): " + repoPrestamo.findPrestamosActivos());
        boolean cerrado = repoPrestamo.cerrarPrestamoHoy(prestamo.getId());
        System.out.println("cerrarPrestamoHoy(prestamo): " + cerrado + " -> " + repoPrestamo.findById(prestamo.getId()));
        System.out.println("findPrestamosActivos() tras cierre: " + repoPrestamo.findPrestamosActivos());

        // ============================================================
        // 6. deleteById() PERSISTIDO EN BASE DE DATOS RELACIONAL
        // ============================================================
        System.out.println("\n--- 6. deleteById() ---");
        repoPrestamo.deleteById(prestamo2.getId());
        System.out.println("¿Existe préstamo2 tras deleteById?: " + repoPrestamo.existsById(prestamo2.getId()));

        repoLibro.deleteById(libro2.getId());
        System.out.println("¿Existe libro2 tras deleteById?: " + repoLibro.existsById(libro2.getId()));

        repoSede.deleteById(sedeNorte.getId());
        System.out.println("¿Existe sedeNorte tras deleteById?: " + repoSede.existsById(sedeNorte.getId()));

        // ============================================================
        // 7. MANEJO DEFENSIVO DE EXCEPCIONES
        // ============================================================
        System.out.println("\n--- 7. MANEJO DE EXCEPCIONES ---");
        try {
            repoSede.findById(null);
        } catch (IllegalArgumentException e) {
            System.out.println("Capturada IllegalArgumentException al pasar id nulo a findById(): " + e.getMessage());
        }

        try {
            repoLibro.existsById(null);
        } catch (IllegalArgumentException e) {
            System.out.println("Capturada IllegalArgumentException al pasar id nulo a existsById(): " + e.getMessage());
        }

        try {
            SedeBiblioteca sedeInvalida = new SedeBiblioteca(12345L, "Sede Inexistente", "Nowhere");
            repoSede.save(sedeInvalida);
        } catch (RepositorioException e) {
            System.out.println("Capturada RepositorioException al intentar actualizar un id inexistente: " + e.getMessage());
        }

        try {
            new Prestamo(null, LocalDate.now(), LocalDate.now().minusDays(1), 1L);
        } catch (IllegalArgumentException e) {
            System.out.println("Capturada IllegalArgumentException por fechaDevolucion anterior a fechaPrestamo: " + e.getMessage());
        }

        System.out.println("\n=== ¡PRUEBAS COMPLETADAS CON ÉXITO! ===");
    }
}
