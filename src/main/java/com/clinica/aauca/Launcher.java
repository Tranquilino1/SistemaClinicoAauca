package com.clinica.aauca;

/**
 * Clase lanzadora para evitar problemas de JavaFX Runtime en JARs sombreados (Shaded JARs).
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
