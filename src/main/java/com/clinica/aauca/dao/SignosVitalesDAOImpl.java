package com.clinica.aauca.dao;

import com.clinica.aauca.model.SignosVitales;
import com.clinica.aauca.util.DatabaseConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SignosVitalesDAOImpl implements SignosVitalesDAO {

    @Override
    public void registrarSignos(SignosVitales sv) throws SQLException {
        String sql = "INSERT INTO signos_vitales(paciente_id, fecha, peso, temperatura, frecuencia_cardiaca, frecuencia_respiratoria, presion_arterial, talla) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sv.getPacienteId());
            pstmt.setString(2, sv.getFecha());
            pstmt.setString(3, sv.getPeso());
            pstmt.setString(4, sv.getTemperatura());
            pstmt.setString(5, sv.getFrecuenciaCardiaca());
            pstmt.setString(6, sv.getFrecuenciaRespiratoria());
            pstmt.setString(7, sv.getPresionArterial());
            pstmt.setString(8, sv.getTalla());
            pstmt.executeUpdate();
        }
    }

    @Override
    public Optional<SignosVitales> obtenerUltimosSignos(int pacienteId) throws SQLException {
        String sql = "SELECT * FROM signos_vitales WHERE paciente_id = ? ORDER BY fecha DESC, id DESC LIMIT 1";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pacienteId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new SignosVitales(
                    rs.getInt("id"),
                    rs.getInt("paciente_id"),
                    rs.getString("fecha"),
                    rs.getString("peso"),
                    rs.getString("temperatura"),
                    rs.getString("frecuencia_cardiaca"),
                    rs.getString("frecuencia_respiratoria"),
                    rs.getString("presion_arterial"),
                    rs.getString("talla")
                ));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<SignosVitales> obtenerTodosSignos(int pacienteId) throws SQLException {
        List<SignosVitales> lista = new ArrayList<>();
        String sql = "SELECT * FROM signos_vitales WHERE paciente_id = ? ORDER BY fecha DESC, id DESC";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pacienteId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lista.add(new SignosVitales(
                    rs.getInt("id"),
                    rs.getInt("paciente_id"),
                    rs.getString("fecha"),
                    rs.getString("peso"),
                    rs.getString("temperatura"),
                    rs.getString("frecuencia_cardiaca"),
                    rs.getString("frecuencia_respiratoria"),
                    rs.getString("presion_arterial"),
                    rs.getString("talla")
                ));
            }
        }
        return lista;
    }
}

