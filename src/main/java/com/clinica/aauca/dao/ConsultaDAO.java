package com.clinica.aauca.dao;

import com.clinica.aauca.model.Consulta;
import com.clinica.aauca.util.DatabaseConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Consulta.
 * Centraliza todas las operaciones SQL sobre la tabla 'consultas'.
 */
public class ConsultaDAO {

    /**
     * Obtiene el nombre completo de un paciente dado su ID (HC).
     */
    public String obtenerNombrePaciente(int id) throws SQLException {
        String sql = "SELECT nombre_completo FROM pacientes WHERE id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("nombre_completo") : null;
        }
    }

    /**
     * Determina si un paciente es estudiante de la AAUCA para aplicar exención de pagos.
     */
    public boolean esEstudiante(int id) throws SQLException {
        String sql = "SELECT es_estudiante FROM pacientes WHERE id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getBoolean("es_estudiante");
        }
    }

    /**
     * Recupera el historial clínico de un paciente específico, ordenado por fecha descendente.
     */
    public List<Consulta> obtenerConsultasPorPaciente(int pacienteId) throws SQLException {
        List<Consulta> lista = new ArrayList<>();
        String sql = "SELECT * FROM consultas WHERE paciente_id = ? ORDER BY fecha DESC";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pacienteId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lista.add(new Consulta(
                    rs.getInt("id"),
                    rs.getInt("paciente_id"),
                    rs.getString("fecha"),
                    rs.getString("motivo_consulta"),
                    rs.getString("historia_actual"),
                    rs.getString("antecedentes_familiares"),
                    rs.getString("antecedentes_personales"),
                    rs.getString("examen_fisico"),
                    rs.getString("diagnostico"),
                    rs.getString("laboratorio"),
                    rs.getString("tratamiento"),
                    rs.getString("receta"),
                    rs.getString("factura"),
                    rs.getString("estado")
                ));
            }
        }
        return lista;
    }

    /**
     * Inserta un nuevo registro de consulta médica en la base de datos.
     */
    public void registrarConsulta(Consulta c) throws SQLException {
        String sql = "INSERT INTO consultas(paciente_id, fecha, motivo_consulta, historia_actual, antecedentes_familiares, antecedentes_personales, examen_fisico, diagnostico, laboratorio, tratamiento, receta, factura, estado) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, c.getPacienteId());
            pstmt.setString(2, c.getFecha());
            pstmt.setString(3, c.getMotivo());
            pstmt.setString(4, c.getHistoriaActual());
            pstmt.setString(5, c.getAntecedentesFamiliares());
            pstmt.setString(6, c.getAntecedentesPersonales());
            pstmt.setString(7, c.getExamenFisico());
            pstmt.setString(8, c.getDiagnostico());
            pstmt.setString(9, c.getLaboratorio());
            pstmt.setString(10, c.getTratamiento());
            pstmt.setString(11, c.getReceta());
            pstmt.setString(12, c.getFactura());
            pstmt.setString(13, c.getEstado());
            pstmt.executeUpdate();
        }
    }

    /**
     * Actualiza los datos de una consulta existente (diagnóstico, receta, etc.).
     */
    public void actualizarConsulta(Consulta c) throws SQLException {
        String sql = "UPDATE consultas SET motivo_consulta=?, historia_actual=?, antecedentes_familiares=?, antecedentes_personales=?, examen_fisico=?, diagnostico=?, laboratorio=?, tratamiento=?, receta=?, factura=?, estado=? WHERE id=?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getMotivo());
            pstmt.setString(2, c.getHistoriaActual());
            pstmt.setString(3, c.getAntecedentesFamiliares());
            pstmt.setString(4, c.getAntecedentesPersonales());
            pstmt.setString(5, c.getExamenFisico());
            pstmt.setString(6, c.getDiagnostico());
            pstmt.setString(7, c.getLaboratorio());
            pstmt.setString(8, c.getTratamiento());
            pstmt.setString(9, c.getReceta());
            pstmt.setString(10, c.getFactura());
            pstmt.setString(11, c.getEstado());
            pstmt.setInt(12, c.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Elimina un registro de consulta (Uso administrativo restringido).
     */
    public void eliminarConsulta(int id) throws SQLException {
        String sql = "DELETE FROM consultas WHERE id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}
