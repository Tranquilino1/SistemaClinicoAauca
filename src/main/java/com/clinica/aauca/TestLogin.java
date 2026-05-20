package com.clinica.aauca;

import com.clinica.aauca.dao.UserDAO;
import com.clinica.aauca.dao.UserDAOImpl;
import com.clinica.aauca.model.User;
import com.clinica.aauca.util.DatabaseConnector;
import java.util.Optional;

/**
 * Pruebas de integración automatizadas para el Sistema de Autenticación.
 */
public class TestLogin {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBAS DE SISTEMA DE LOGIN ===");

        UserDAO dao = new UserDAOImpl();

        // Prueba 1: Login de Admin
        testLogin(dao, "admin", "admin123", "Admin");

        // Prueba 2: Login de Médico
        testLogin(dao, "medico", "medico123", "Médico");

        // Prueba 3: Login de Recepción
        testLogin(dao, "recepcion", "recep123", "Recepción");

        // Prueba 4: Login fallido (Password incorrecto)
        testLoginFailure(dao, "admin", "password_erroneo");

        // Prueba 5: Usuario inexistente
        testLoginFailure(dao, "usuario_fantasma", "123");

        System.out.println("\n=== TODAS LAS PRUEBAS COMPLETADAS CON ÉXITO ===");
    }

    private static void testLogin(UserDAO dao, String user, String pass, String expectedRole) {
        System.out.print("Probando login [" + user + "]... ");
        Optional<User> result = dao.login(user, pass);

        if (result.isPresent() && result.get().getRole().equals(expectedRole)) {
            System.out.println("✅ OK (Rol detectado: " + result.get().getRole() + ")");
        } else {
            System.out.println("❌ FALLO (Esperado: " + expectedRole + ")");
            System.exit(1);
        }
    }

    private static void testLoginFailure(String user, String pass) {
        // Overloaded
    }

    private static void testLoginFailure(UserDAO dao, String user, String pass) {
        System.out.print("Probando login incorrecto [" + user + "]... ");
        Optional<User> result = dao.login(user, pass);

        if (result.isEmpty()) {
            System.out.println("✅ OK (Acceso denegado correctamente)");
        } else {
            System.out.println("❌ FALLO (Se permitió el acceso de forma errónea)");
            System.exit(1);
        }
    }
}
