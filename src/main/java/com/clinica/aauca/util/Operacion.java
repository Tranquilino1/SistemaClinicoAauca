package com.clinica.aauca.util;

/**
 * Clase para gestionar el estado de las operaciones en curso en el sistema.
 * Permite controlar si el usuario está editando un formulario para prevenir
 * la pérdida de datos accidental al navegar.
 */
public class Operacion {
    /**
     * Atributo estático que indica si hay una operación de edición/creación en curso.
     */
    public static boolean encurso = false;
    
    /**
     * Reinicia el estado de la operación.
     */
    public static void finalizar() {
        encurso = false;
    }
    
    /**
     * Activa el estado de operación en curso.
     */
    public static void iniciar() {
        encurso = true;
    }
}
