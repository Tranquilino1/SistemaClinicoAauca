package com.clinica.aauca.dao;

import com.clinica.aauca.model.Paciente;
import com.clinica.aauca.util.DatabaseConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAOImpl implements PacienteDAO {

    @Override
    public List<Paciente> obtenerPacientes(int offset, int limit) {
        List<Paciente> pacientes = new ArrayList<>();
        String sql = "SELECT p.*, " +
                     "(SELECT COUNT(*) FROM hospitalizaciones WHERE paciente_id = p.id AND estado = 'ingresado') > 0 as hosp_activo " +
                     "FROM pacientes p LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Paciente p = new Paciente(
                    rs.getInt("id"),
                    rs.getString("nombre_completo"),
                    rs.getString("tipo"),
                    rs.getBoolean("es_estudiante"),
                    rs.getString("fecha_nacimiento"),
                    rs.getString("sexo"),
                    rs.getString("direccion"),
                    rs.getString("telefono"),
                    rs.getString("nacionalidad")
                );
                p.setHospitalizado(rs.getBoolean("hosp_activo"));
                pacientes.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pacientes;
    }

    @Override
    public void crearPaciente(Paciente paciente) {
        String sql = "INSERT INTO pacientes(nombre_completo, tipo, es_estudiante, fecha_nacimiento, sexo, direccion, telefono, nacionalidad) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, paciente.getNombreCompleto());
            pstmt.setString(2, paciente.getTipo());
            pstmt.setBoolean(3, paciente.isEsEstudiante());
            pstmt.setString(4, paciente.getFechaNacimiento());
            pstmt.setString(5, paciente.getSexo());
            pstmt.setString(6, paciente.getDireccion());
            pstmt.setString(7, paciente.getTelefono());
            pstmt.setString(8, paciente.getNacionalidad());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actualizarPaciente(Paciente paciente) {
        String sql = "UPDATE pacientes SET nombre_completo=?, tipo=?, es_estudiante=?, fecha_nacimiento=?, sexo=?, direccion=?, telefono=?, nacionalidad=? WHERE id=?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, paciente.getNombreCompleto());
            pstmt.setString(2, paciente.getTipo());
            pstmt.setBoolean(3, paciente.isEsEstudiante());
            pstmt.setString(4, paciente.getFechaNacimiento());
            pstmt.setString(5, paciente.getSexo());
            pstmt.setString(6, paciente.getDireccion());
            pstmt.setString(7, paciente.getTelefono());
            pstmt.setString(8, paciente.getNacionalidad());
            pstmt.setInt(9, paciente.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void eliminarPaciente(int id) {
        String sql = "DELETE FROM pacientes WHERE id=?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Paciente> buscarPacientePorNombre(String nombre, int offset, int limit) {
        List<Paciente> pacientes = new ArrayList<>();
        String sql = "SELECT p.*, " +
                     "(SELECT COUNT(*) FROM hospitalizaciones WHERE paciente_id = p.id AND estado = 'ingresado') > 0 as hosp_activo " +
                     "FROM pacientes p WHERE nombre_completo LIKE ? LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + nombre + "%");
            pstmt.setInt(2, limit);
            pstmt.setInt(3, offset);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Paciente p = new Paciente(
                    rs.getInt("id"),
                    rs.getString("nombre_completo"),
                    rs.getString("tipo"),
                    rs.getBoolean("es_estudiante"),
                    rs.getString("fecha_nacimiento"),
                    rs.getString("sexo"),
                    rs.getString("direccion"),
                    rs.getString("telefono"),
                    rs.getString("nacionalidad")
                );
                p.setHospitalizado(rs.getBoolean("hosp_activo"));
                pacientes.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pacientes;
    }

    @Override
    public int contarTotalPacientes() {
        String sql = "SELECT COUNT(*) FROM pacientes";
        try (Connection conn = DatabaseConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public int contarTotalPacientes(String query) {
        String sql = "SELECT COUNT(*) FROM pacientes WHERE nombre_completo LIKE ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
