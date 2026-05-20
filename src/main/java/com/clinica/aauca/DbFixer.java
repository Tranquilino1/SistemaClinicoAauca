package com.clinica.aauca;

import com.clinica.aauca.util.DatabaseConnector;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

/**
 * Corrección de contraseñas y actualización de usuarios en la BD.
 */
public class DbFixer {
    public static void main(String[] args) throws Exception {
        Connection conn = DatabaseConnector.getConnection();

        // Generar hash correcto para enfermero123
        String hashEnfermero = BCrypt.hashpw("enfermero123", BCrypt.gensalt(12));
        String hashMedico    = BCrypt.hashpw("medico123", BCrypt.gensalt(12));
        String hashAdmin     = BCrypt.hashpw("admin123", BCrypt.gensalt(12));

        // Actualizar contraseñas con hashes correctos para cada usuario
        String[] usernames  = {"admin", "medico", "enfermero"};
        String[] passwords  = {hashAdmin, hashMedico, hashEnfermero};
        String[] nombres    = {"Administrador Principal", "Dr. Marcos Nguema Edu", "Enf. Roberto Díaz Mba"};
        String[] roles      = {"Admin", "Médico", "Enfermero"};

        for (int i = 0; i < usernames.length; i++) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE usuarios SET password=?, nombre_completo=?, rol=? WHERE username=?");
            ps.setString(1, passwords[i]);
            ps.setString(2, nombres[i]);
            ps.setString(3, roles[i]);
            ps.setString(4, usernames[i]);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                // Si no existe, insertarlo
                PreparedStatement ins = conn.prepareStatement(
                    "INSERT OR IGNORE INTO usuarios(username, password, nombre_completo, rol) VALUES(?,?,?,?)");
                ins.setString(1, usernames[i]);
                ins.setString(2, passwords[i]);
                ins.setString(3, nombres[i]);
                ins.setString(4, roles[i]);
                ins.executeUpdate();
            }
        }

        // Eliminar usuario recepcion si existe
        conn.prepareStatement("DELETE FROM usuarios WHERE username='recepcion'").executeUpdate();

        // Verificar
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT id, username, nombre_completo, rol FROM usuarios ORDER BY id");
        System.out.println("\n=== USUARIOS ACTUALIZADOS ===");
        while (rs.next()) {
            System.out.printf("ID=%-2d | user=%-12s | nombre=%-35s | rol=%s%n",
                rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
        }
        
        // Verificar que los hashes son correctos
        System.out.println("\n=== VERIFICACIÓN DE CONTRASEÑAS ===");
        String[][] checks = {{"admin123", hashAdmin}, {"medico123", hashMedico}, {"enfermero123", hashEnfermero}};
        for (String[] c : checks) {
            System.out.printf("%-15s -> BCrypt válido: %s%n", c[0], BCrypt.checkpw(c[0], c[1]));
        }

        conn.close();
        System.out.println("\nBase de datos corregida exitosamente.");
    }
}
