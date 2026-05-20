package com.clinica.aauca.dao;

import com.clinica.aauca.model.Hospitalizacion;
import com.clinica.aauca.util.DatabaseConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la gestión de Hospitalizaciones.
 * Maneja el ciclo de vida del internamiento: Ingreso, Evolución y Alta.
 */
public class HospitalizacionDAO {

    /**
     * Obtiene el nombre del paciente asociado al internamiento.
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

    public boolean esEstudiante(int pacienteId) throws SQLException {
        String sql = "SELECT es_estudiante FROM pacientes WHERE id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pacienteId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt("es_estudiante") == 1;
        }
    }

    /**
     * Recupera todos los internamientos (pasados y actuales) de un paciente.
     */
    public List<Hospitalizacion> obtenerPorPaciente(int pacienteId) throws SQLException {
        List<Hospitalizacion> lista = new ArrayList<>();
        String sql = "SELECT h.*, p.nombre_completo FROM hospitalizaciones h " +
                     "JOIN pacientes p ON h.paciente_id = p.id " +
                     "WHERE h.paciente_id = ? ORDER BY h.id DESC";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pacienteId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Hospitalizacion h = new Hospitalizacion(
                    rs.getInt("id"),
                    rs.getInt("paciente_id"),
                    rs.getString("estado"),
                    rs.getString("seguimiento"),
                    rs.getString("fecha_ingreso"),
                    rs.getString("motivo")
                );
                h.setNombrePaciente(rs.getString("nombre_completo"));
                lista.add(h);
            }
        }
        return lista;
    }

    /**
     * Filtra y devuelve solo los pacientes que están actualmente en la clínica (estado 'ingresado').
     */
    public List<Hospitalizacion> obtenerHospitalizacionesActivas() throws SQLException {
        List<Hospitalizacion> lista = new ArrayList<>();
        String sql = "SELECT h.*, p.nombre_completo FROM hospitalizaciones h " +
                     "JOIN pacientes p ON h.paciente_id = p.id " +
                     "WHERE h.estado = 'ingresado' ORDER BY h.id DESC";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Hospitalizacion h = new Hospitalizacion(
                    rs.getInt("id"),
                    rs.getInt("paciente_id"),
                    rs.getString("estado"),
                    rs.getString("seguimiento"),
                    rs.getString("fecha_ingreso"),
                    rs.getString("motivo")
                );
                h.setNombrePaciente(rs.getString("nombre_completo"));
                lista.add(h);
            }
        }
        return lista;
    }

    /**
     * Registra un nuevo ingreso hospitalario con la fecha actual del sistema.
     */
    public void crearNuevaHospitalizacion(int pacienteId, String estado, String seguimiento, String motivo) throws SQLException {
        String sql = "INSERT INTO hospitalizaciones(paciente_id, estado, seguimiento, fecha_ingreso, motivo) VALUES(?,?,?,date('now'),?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pacienteId);
            pstmt.setString(2, estado);
            pstmt.setString(3, seguimiento);
            pstmt.setString(4, motivo);
            pstmt.executeUpdate();
        }
    }

    /**
     * Actualiza el reporte de evolución del paciente.
     */
    public void actualizarEvolucion(int id, String estado, String seguimiento) throws SQLException {
        String sql = "UPDATE hospitalizaciones SET estado=?, seguimiento=? WHERE id=?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, estado);
            pstmt.setString(2, seguimiento);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Elimina un registro de hospitalización.
     */
    public void eliminarHospitalizacion(int id) throws SQLException {
        String sql = "DELETE FROM hospitalizaciones WHERE id=?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Busca un internamiento específico por su ID único.
     */
    public Hospitalizacion obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM hospitalizaciones WHERE id=?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Hospitalizacion(
                        rs.getInt("id"),
                        rs.getInt("paciente_id"),
                        rs.getString("estado"),
                        rs.getString("seguimiento"),
                        rs.getString("fecha_ingreso"),
                        rs.getString("motivo")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Finaliza un internamiento registrando la fecha de salida y el monto facturado.
     */
    public void registrarAltaYFactura(int id, String fechaAlta, double monto) throws SQLException {
        String sql = "UPDATE hospitalizaciones SET fecha_alta=?, monto_total=?, estado='de alta' WHERE id=?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fechaAlta);
            pstmt.setDouble(2, monto);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * Clase interna para transferir datos rápidos de seguimiento.
     */
    public static class InfoHospitalizacion {
        public int id;
        public String estado;
        public String seguimiento;
    }

    /**
     * Recupera el estado más reciente de un paciente para alertas en el Dashboard.
     */
    public InfoHospitalizacion obtenerUltimoSeguimiento(int pacienteId) throws SQLException {
        String sql = "SELECT * FROM hospitalizaciones WHERE paciente_id=? ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pacienteId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                InfoHospitalizacion info = new InfoHospitalizacion();
                info.id = rs.getInt("id");
                info.estado = rs.getString("estado");
                info.seguimiento = rs.getString("seguimiento");
                return info;
            }
        }
        return null;
    }
}
