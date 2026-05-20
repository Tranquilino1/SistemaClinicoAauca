package com.clinica.aauca.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;

public class PopulateData {
    public static void main(String[] args) {
        System.out.println("Iniciando población de datos de prueba...");
        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();

            // Limpiar datos previos si se desea (opcional)
            // stmt.execute("DELETE FROM consultas;");
            // stmt.execute("DELETE FROM hospitalizaciones;");
            // stmt.execute("DELETE FROM pacientes;");

            // 1. Asegurar Pacientes (Ya existen en schema.sql, pero re-insertamos o actualizamos)
            String[] pacientes = {
                "Pelayo Nvulu Nvulu (Informática)",
                "Carlota Raquel (Medicina)",
                "Mauricio Edu (Economía)",
                "Ángel Asimi (Ing. Eléctrica)",
                "Tranquilino Maba (Agroindustrial)",
                "Profe. Diosdado Esono (Docente)",
                "Evaristo Olo (Ing. Civil)",
                "Natividad Eseng (Mecánica)",
                "Patricio Mba (Administración)",
                "Beatriz Nguema (Interpretación)"
            };

            for (int i = 0; i < pacientes.length; i++) {
                String sql = "INSERT OR IGNORE INTO pacientes (id, nombre_completo, es_estudiante, fecha_nacimiento) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, i + 1);
                    ps.setString(2, pacientes[i]);
                    ps.setInt(3, i == 5 ? 0 : 1); // El 6º es docente
                    ps.setString(4, "200" + (i % 5) + "-0" + (i + 1) + "-15");
                    ps.executeUpdate();
                }
            }

            // 2. Insertar Consultas para cada paciente
            String[] motivos = {"Dolor de cabeza severo", "Fiebre y tos", "Chequeo rutinario", "Dolor abdominal", "Alergia cutánea"};
            String[] diag = {"Migraña", "Gripe común", "Saludable", "Gastritis", "Dermatitis"};
            String[] trat = {"Paracetamol 500mg cada 8h", "Descanso y líquidos", "Ninguno", "Dieta blanda y antiácidos", "Crema hidratante"};

            for (int pId = 1; pId <= 10; pId++) {
                for (int c = 0; c < 2; c++) {
                    String sql = "INSERT INTO consultas (paciente_id, fecha, motivo_consulta, diagnostico, tratamiento, antecedentes, receta, factura, estado) " +
                                 "VALUES (?, date('now', '-" + (c * 10) + " days'), ?, ?, ?, 'Ninguno relevante', 'Receta " + (c+1) + "', '0', 'impreso')";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, pId);
                        ps.setString(2, motivos[(pId + c) % 5]);
                        ps.setString(3, diag[(pId + c) % 5]);
                        ps.setString(4, trat[(pId + c) % 5]);
                        ps.executeUpdate();
                    }
                }
            }

            // 3. Insertar algunas Hospitalizaciones
            for (int pId = 1; pId <= 5; pId++) {
                String sql = "INSERT INTO hospitalizaciones (paciente_id, estado, seguimiento, fecha_ingreso, motivo) " +
                             "VALUES (?, 'de alta', 'Evolución favorable después de 3 días.', date('now', '-20 days'), 'Observación post-operatoria')";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, pId);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            System.out.println("✅ Población de datos completada exitosamente.");

        } catch (SQLException e) {
            System.err.println("❌ Error durante la población de datos: " + e.getMessage());
        }
    }
}
