package org.palomafp.gestionbiblioteca.domain;

/**
 * Excepción no comprobada que representa un fallo al ejecutar una operación de repositorio.
 * La infraestructura convierte errores técnicos, como SQLException, a este tipo común
 * para comunicar el fallo a la aplicación sin obligar al dominio a depender de JDBC.
 */
public class RepositorioException extends RuntimeException {

    /**
     * Construye un error de repositorio cuando se dispone de una descripción, pero no
     * se necesita asociar una excepción técnica original.
     *
     * @param message mensaje que describe la operación de persistencia que falló
     */
    public RepositorioException(String message) {
        super(message);
    }

    /**
     * Construye un error de repositorio conservando la causa técnica original.
     * Mantener la causa permite diagnosticar el problema JDBC sin exponerla como
     * contrato obligatorio para las capas superiores.
     *
     * @param message mensaje que describe la operación de persistencia que falló
     * @param cause excepción original que provocó el fallo, normalmente una SQLException
     */
    public RepositorioException(String message, Throwable cause) {
        super(message, cause);
    }
}
