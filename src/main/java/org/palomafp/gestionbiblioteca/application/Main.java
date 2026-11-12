package org.palomafp.gestionbiblioteca.application;

import org.palomafp.gestionbiblioteca.domain.*;
import org.palomafp.gestionbiblioteca.infrastructure.*;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Punto de entrada de la capa de aplicación y programa demostrativo del proyecto.
 * Coordina el caso de uso de prueba: inicializa el esquema, solicita operaciones a
 * los interfaces de repositorio, presenta sus resultados y muestra excepciones esperadas.
 * No contiene SQL ni abre conexiones; delega esas responsabilidades en infraestructura
 * y utiliza las entidades y reglas definidas en el dominio.
 */
public class Main {
    /**
     * Inicializa SQLite y ejecuta, en orden, ejemplos de inserción, actualización,
     * consultas, borrado y validación de errores. La limpieza inicial elimina los
     * registros de prueba existentes respetando el orden inverso de las dependencias.
     *
     * @param args argumentos de línea de comandos; actualmente no se utilizan
     * @throws RuntimeException si no se puede cargar o ejecutar el esquema SQLite
     * @throws RepositorioException si una operación de repositorio fuera de los casos
     *                               esperados no puede completar la persistencia
     */
    public static void main(String[] args) {
        // Encabezado de consola: esta clase es una demostración ejecutable, no una interfaz gráfica.
        System.out.println("===================================");
        System.out.println("       GESTION DE BIBLIOTECA");
        System.out.println("===================================");

        /*
         * 0. Inicialización: DatabaseConnection lee schema.sql y crea las tablas que falten.
         * La aplicación no conoce el detalle DDL porque pertenece a infraestructura y resources.
         */
        DatabaseConnection.initSchema();
        System.out.println("[OK] Esquema de la base de datos inicializado.\n");

        // La aplicación conserva los contratos de dominio; las implementaciones concretas son adaptadores JDBC.
        IRepositorioSede repoSede = new RepositorioSedeJdbcImpl();
        IRepositorioLibro repoLibro = new RepositorioLibroJdbcImpl();
        IRepositorioPrestamo repoPrestamo = new RepositorioPrestamoJdbcImpl();

        /*
         * Limpieza propia de esta demostración para que las cantidades y resultados sean reproducibles.
         * Se eliminan primero los registros dependientes: prestamos -> libros -> sedes.
         */
        repoPrestamo.deleteAll();
        repoLibro.deleteAll();
        repoSede.deleteAll();

        System.out.println("============================================================");
        System.out.println("1. INSERCION");
        System.out.println("============================================================");
        // El id null indica que aún no hay clave de base de datos; save obtiene y asigna la generada.
        SedeBiblioteca sedeCentral = new SedeBiblioteca(null, "Biblioteca Central", "Madrid");
        repoSede.save(sedeCentral);
        System.out.println("  * Sede guardada: " + sedeCentral);

        SedeBiblioteca sedeNorte = new SedeBiblioteca(null, "Biblioteca Norte", "Bilbao");
        repoSede.save(sedeNorte);
        System.out.println("  * Sede guardada: " + sedeNorte);

        // El objeto valor Isbn valida el código y sedeCentral.getId() representa la relación del libro.
        Libro libro = new Libro(null, new Isbn("9788441540001"), "Clean Architecture en Java", sedeCentral.getId());
        repoLibro.save(libro);
        System.out.println("  * Libro guardado: " + libro);

        Libro libro2 = new Libro(null, new Isbn("9788441540018"), "Domain-Driven Design Aplicado", sedeCentral.getId());
        repoLibro.save(libro2);
        System.out.println("  * Libro guardado: " + libro2);

        // La fecha de devolución null representa estado activo; libro.getId() asocia el préstamo.
        Prestamo prestamo = new Prestamo(null, LocalDate.now().minusDays(5), null, libro.getId());
        repoPrestamo.save(prestamo);
        System.out.println("  * Prestamo guardado: " + prestamo);

        Prestamo prestamo2 = new Prestamo(null, LocalDate.now().minusDays(2), null, libro2.getId());
        repoPrestamo.save(prestamo2);
        System.out.println("  * Prestamo guardado: " + prestamo2);

        System.out.println("\n============================================================");
        System.out.println("2. ACTUALIZACIÓN");
        System.out.println("============================================================");
        // findById recupera la entidad; modificarla no persiste hasta invocar save.
        SedeBiblioteca sedeEncontrada = repoSede.findById(sedeCentral.getId());
        sedeEncontrada.setNombre("Biblioteca Central Renovada");
        repoSede.save(sedeEncontrada);
        System.out.println("  * Sede actualizada: " + repoSede.findById(sedeCentral.getId()));

        System.out.println("\n============================================================");
        System.out.println("3. CONSULTAS Y METODOS DE REPOSITORIO");
        System.out.println("============================================================");
        System.out.println("  Total sedes      : " + repoSede.count());
        System.out.println("  Total libros     : " + repoLibro.count());
        System.out.println("  Total prestamos : " + repoPrestamo.count());

        // existsById permite probar existencia sin recuperar ni mapear toda la fila.
        System.out.println("  Existe sede ID " + sedeCentral.getId() + "?: "
                + (repoSede.existsById(sedeCentral.getId()) ? "SI" : "NO"));
        System.out.println("  Existe libro ID " + libro.getId() + "?: "
                + (repoLibro.existsById(libro.getId()) ? "SI" : "NO"));
        System.out.println("  Existe prestamo ID " + prestamo.getId() + "?: "
                + (repoPrestamo.existsById(prestamo.getId()) ? "SI" : "NO"));

        // Las consultas singulares muestran los toString legibles de cada entidad.
        System.out.println("\n  Resultado findById (sede): " + repoSede.findById(sedeCentral.getId()));
        System.out.println("  Resultado findById (libro): " + repoLibro.findById(libro.getId()));
        System.out.println("  Resultado findById (prestamo): " + repoPrestamo.findById(prestamo.getId()));

        // findAll devuelve un Iterable; se imprime cada entidad por separado para facilitar su lectura.
        System.out.println("\n  Sedes:");
        repoSede.findAll().forEach(s -> System.out.println("    * " + s));
        System.out.println("  Libros:");
        repoLibro.findAll().forEach(l -> System.out.println("    * " + l));
        System.out.println("  Prestamos:");
        repoPrestamo.findAll().forEach(p -> System.out.println("    * " + p));

        System.out.println("\n  Consultas extendidas:");
        // Optional comunica explícitamente la posible ausencia, sin recurrir a null.
        Optional<SedeBiblioteca> sedeOpt = repoSede.findByIdOptional(sedeCentral.getId());
        System.out.println("    findByIdOptional (sede): "
                + sedeOpt.map(s -> s.toString()).orElse("[Vacio]"));

        // Se prueba la ruta sin resultados usando un id deliberadamente inexistente.
        Optional<SedeBiblioteca> sedeVacia = repoSede.findByIdOptional(999999L);
        System.out.println("    findByIdOptional (ID inexistente): "
                + sedeVacia.map(s -> s.toString()).orElse("[Vacio]"));

        /*
         * Se demuestra el mismo método extendido en los otros dos repositorios.
         * El Optional contiene la entidad si existe; map aplica su formato toString
         * y orElse presenta un texto claro cuando el identificador no tiene resultado.
         */
        System.out.println("    findByIdOptional (libro): "
                + repoLibro.findByIdOptional(libro.getId()).map(Libro::toString).orElse("[Vacio]"));
        System.out.println("    findByIdOptional (prestamo): "
                + repoPrestamo.findByIdOptional(prestamo.getId()).map(Prestamo::toString).orElse("[Vacio]"));

        System.out.println("    Sedes:");
        repoSede.findAllToList().forEach(s -> System.out.println("      * " + s));
        System.out.println("    Libros:");
        repoLibro.findAllToList().forEach(l -> System.out.println("      * " + l));
        System.out.println("    Prestamos:");
        repoPrestamo.findAllToList().forEach(p -> System.out.println("      * " + p));

        // Métodos específicos del dominio aplican filtros y agregaciones encapsulados en los repositorios.
        System.out.println("\n  Consultas propias:");
        System.out.println("    Sedes en Madrid:");
        repoSede.findByCiudad("Madrid").forEach(s -> System.out.println("      * " + s));
        System.out.println("    Libros de la sede central:");
        repoLibro.findBySedeId(sedeCentral.getId()).forEach(l -> System.out.println("      * " + l));
        System.out.println("    Libros cuyo titulo contiene 'Design':");
        repoLibro.findByTituloContaining("Design").forEach(l -> System.out.println("      * " + l));
        System.out.println("    Prestamos del libro ID " + libro.getId() + ":");
        repoPrestamo.findPrestamosByLibroId(libro.getId())
                .forEach(p -> System.out.println("      * " + p));
        System.out.println("    Prestamos activos:");
        repoPrestamo.findPrestamosActivos().forEach(p -> System.out.println("      * " + p));
        System.out.println("    Libros en sede central: "
                + repoSede.contarLibrosEnSede(sedeCentral.getId()));

        // El repositorio recupera el préstamo, ejecuta la validación del dominio y guarda la fecha actual.
        boolean cerrado = repoPrestamo.cerrarPrestamoHoy(prestamo.getId());
        System.out.println("    Cierre del prestamo ID " + prestamo.getId() + ": "
                + (cerrado ? "REALIZADO" : "NO REALIZADO"));
        System.out.println("    Prestamo actualizado: " + repoPrestamo.findById(prestamo.getId()));
        System.out.println("    Prestamos activos tras el cierre:");
        repoPrestamo.findPrestamosActivos().forEach(p -> System.out.println("      * " + p));

        System.out.println("\n============================================================");
        System.out.println("4. BORRADOS Y MANEJO DE EXCEPCIONES");
        System.out.println("============================================================");
        // Se borran préstamo y libro antes de su sede para respetar las claves foráneas.
        repoPrestamo.deleteById(prestamo2.getId());
        System.out.println("  Prestamo ID " + prestamo2.getId() + " eliminado: "
                + (repoPrestamo.existsById(prestamo2.getId()) ? "NO" : "SI"));

        repoLibro.deleteById(libro2.getId());
        System.out.println("  Libro ID " + libro2.getId() + " eliminado: "
                + (repoLibro.existsById(libro2.getId()) ? "NO" : "SI"));

        repoSede.deleteById(sedeNorte.getId());
        System.out.println("  Sede ID " + sedeNorte.getId() + " eliminada: "
                + (repoSede.existsById(sedeNorte.getId()) ? "NO" : "SI"));

        /*
         * Las pruebas siguientes provocan errores de entrada conocidos y los capturan
         * para demostrar la diferencia entre validación del dominio y fallo de persistencia.
         */
        try {
            repoSede.findById(null);
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR CONTROLADO] X IllegalArgumentException en findById: "
                    + e.getMessage());
        }

        // El contrato del repositorio rechaza identificadores nulos antes de crear la consulta JDBC.
        try {
            repoLibro.existsById(null);
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR CONTROLADO] X IllegalArgumentException en existsById: "
                    + e.getMessage());
        }

        // Un id no nulo pero inexistente se interpreta como actualización inválida del repositorio.
        try {
            SedeBiblioteca sedeInvalida = new SedeBiblioteca(12345L, "Sede Inexistente", "Nowhere");
            repoSede.save(sedeInvalida);
        } catch (RepositorioException e) {
            System.out.println("  [ERROR CONTROLADO] X RepositorioException al actualizar una sede inexistente: "
                    + e.getMessage());
        }

        // La entidad impide construir un préstamo cuya devolución antecede a su inicio.
        try {
            new Prestamo(null, LocalDate.now(), LocalDate.now().minusDays(1), 1L);
        } catch (IllegalArgumentException e) {
            System.out.println("  [ERROR CONTROLADO] X IllegalArgumentException por fechas invalidas: "
                    + e.getMessage());
        }

        System.out.println("\n============================================================");
        System.out.println("              PRUEBAS COMPLETADAS CON ÉXITO");
        System.out.println("============================================================");
    }
}
