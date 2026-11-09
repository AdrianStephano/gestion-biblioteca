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

public class DatabaseConnection {
    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("database.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                throw new RuntimeException("No se ha encontrado el fichero database.properties en resources.");
            }
            prop.load(input);
            url = prop.getProperty("db.url");
            user = prop.getProperty("db.user");
            password = prop.getProperty("db.password");
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar la configuración de la base de datos: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Lee el fichero schema.sql del classpath y ejecuta cada instrucción DDL
     * por separado, creando el esquema de la base de datos si no existe aún.
     */
    public static void initSchema() {
        String script = readSchemaScript();
        String[] statements = script.split(";");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sentencia : statements) {
                String sql = sentencia.trim();
                if (!sql.isEmpty()) {
                    stmt.executeUpdate(sql);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al inicializar el esquema de la base de datos: " + e.getMessage(), e);
        }
    }

    private static String readSchemaScript() {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (input == null) {
                throw new RuntimeException("No se ha encontrado el fichero schema.sql en resources.");
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer schema.sql: " + e.getMessage(), e);
        }
    }
}
