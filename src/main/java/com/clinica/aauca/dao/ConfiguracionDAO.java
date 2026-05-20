package com.clinica.aauca.dao;

import com.clinica.aauca.model.Configuracion;
import com.clinica.aauca.util.DatabaseConnector;
import java.sql.*;

public class ConfiguracionDAO {

    public Configuracion obtenerConfiguracion() {
        String sql = "SELECT * FROM configuracion WHERE id = 1";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                Configuracion config = new Configuracion();
                config.setNombreClinica(rs.getString("nombre_clinica"));
                config.setDireccion(rs.getString("direccion"));
                config.setTelefono(rs.getString("telefono"));
                config.setEmail(rs.getString("email"));
                config.setMoneda(rs.getString("moneda"));
                config.setPrecioConsulta(rs.getDouble("precio_consulta"));
                config.setPrecioHospitalizacion(rs.getDouble("precio_hospitalizacion"));
                config.setLogoPath(rs.getString("logo_path"));
                return config;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void guardarConfiguracion(Configuracion config) {
        String sql = "UPDATE configuracion SET nombre_clinica=?, direccion=?, telefono=?, email=?, moneda=?, precio_consulta=?, precio_hospitalizacion=?, logo_path=? WHERE id=1";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, config.getNombreClinica());
            pstmt.setString(2, config.getDireccion());
            pstmt.setString(3, config.getTelefono());
            pstmt.setString(4, config.getEmail());
            pstmt.setString(5, config.getMoneda());
            pstmt.setDouble(6, config.getPrecioConsulta());
            pstmt.setDouble(7, config.getPrecioHospitalizacion());
            pstmt.setString(8, config.getLogoPath());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
