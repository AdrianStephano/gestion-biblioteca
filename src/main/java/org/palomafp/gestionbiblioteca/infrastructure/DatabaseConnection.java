package org.palomafp.gestionbiblioteca.infrastructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Adaptador de infraestructura responsable de cargar la configuración de SQLite,
 * abrir conexiones JDBC y crear el esquema inicial.
 *
 * La URL JDBC, el usuario y la contraseña se cargan desde database.properties del
 * classpath una sola vez, durante la inicialización de la clase. Los repositorios
 * solicitan una conexión a este componente en vez de repetir detalles de configuración.
 * Los métodos que crean recursos JDBC no los conservan: quien recibe una conexión
 * debe cerrarla, normalmente mediante try-with-resources.
 */
public class DatabaseConnection {
    /** URL JDBC de SQLite; el fichero se resuelve respecto al directorio de ejecución. */
    private static String url;
    /** Usuario de conexión leído de la configuración; SQLite normalmente no lo requiere. */
    private static String user;
    /** Contraseña de conexión leída de la configuración; SQLite normalmente no la requiere. */
    private static String password;

    /*
     * Inicialización única de configuración: Properties interpreta las parejas clave-valor
     * del recurso y el class loader permite empaquetarlo dentro de la aplicación.
     * El bloque try-with-resources cierra el InputStream incluso si la lectura falla.
     */
    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("database.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                // Sin configuración no existe una URL fiable para abrir la base de datos.
                throw new RuntimeException("No se ha encontrado el fichero database.properties en resources.");
            }
            // Carga las propiedades db.url, db.user y db.password desde el archivo.
            prop.load(input);
            url = prop.getProperty("db.url");
            user = prop.getProperty("db.user");
            password = prop.getProperty("db.password");
        } catch (Exception e) {
            // El inicializador estático no puede declarar checked exceptions; conserva la causa.
            throw new RuntimeException("Error al cargar la configuración de la base de datos: " + e.getMessage(), e);
        }
    }

    /**
     * Abre una conexión JDBC nueva con los parámetros cargados del recurso de configuración.
     * DriverManager localiza el controlador SQLite disponible en el classpath y delega
     * en él la apertura de la URL. Cada llamada produce una conexión independiente.
     *
     * @return conexión abierta; el código llamante es responsable de cerrarla
     * @throws SQLException si DriverManager no puede abrir la URL o el controlador rechaza la conexión
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Lee schema.sql del classpath, separa sus instrucciones por punto y coma y las
     * ejecuta en orden para crear las tablas si todavía no existen. Connection y Statement
     * se cierran automáticamente incluso si una instrucción falla.
     *
     * Este cargador sencillo está pensado para el esquema de este proyecto, cuyas
     * instrucciones no contienen punto y coma dentro de literales ni bloques procedurales.
     *
     * @throws RuntimeException si no se puede leer el recurso, abrir la conexión o ejecutar DDL
     */
    public static void initSchema() {
        // Se lee primero el recurso y después se abre la conexión de escritura.
        String script = readSchemaScript();
        String[] statements = script.split(";");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sentencia : statements) {
                // El archivo puede tener líneas en blanco; no se envían consultas vacías al motor.
                String sql = sentencia.trim();
                if (!sql.isEmpty()) {
                    // El esquema es DDL fijo incluido con la aplicación, no una entrada de usuario.
                    stmt.executeUpdate(sql);
                }
            }
        } catch (SQLException e) {
            // La inicialización no expone SQLException porque se invoca desde el arranque.
            throw new RuntimeException("Error al inicializar el esquema de la base de datos: " + e.getMessage(), e);
        }
    }

    /**
     * Lee íntegramente el recurso schema.sql usando UTF-8 y lo devuelve como texto.
     * El método es privado porque solo forma parte del proceso de inicialización.
     *
     * @return contenido completo del esquema
     * @throws RuntimeException si el recurso no existe o no se puede leer
     */
    private static String readSchemaScript() {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (input == null) {
                // El error temprano permite distinguir recurso ausente de error de SQL.
                throw new RuntimeException("No se ha encontrado el fichero schema.sql en resources.");
            }
            StringBuilder sb = new StringBuilder();
            // Se especifica UTF-8 para no depender de la codificación predeterminada del sistema operativo.
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Se conserva un salto entre líneas para mantener legible el script leído.
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        } catch (IOException e) {
            // La causa se conserva para poder diagnosticar problemas de lectura del recurso.
            throw new RuntimeException("Error al leer schema.sql: " + e.getMessage(), e);
        }
    }
}
