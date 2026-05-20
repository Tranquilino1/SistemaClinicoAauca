package com.clinica.aauca.dao;

import com.clinica.aauca.model.Medicamento;
import com.clinica.aauca.util.DatabaseConnector;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicamentoDAO {

    public List<Medicamento> obtenerTodos() {
        List<Medicamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM medicamentos ORDER BY nombre ASC";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Medicamento(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getInt("stock"),
                    rs.getDouble("precio"),
                    rs.getInt("creador_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Medicamento> buscarPorNombre(String nombre) {
        List<Medicamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM medicamentos WHERE nombre LIKE ? ORDER BY nombre ASC";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + nombre + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lista.add(new Medicamento(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getInt("stock"),
                    rs.getDouble("precio"),
                    rs.getInt("creador_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void crear(Medicamento m) {
        String sql = "INSERT INTO medicamentos(nombre, stock, precio, creador_id) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, m.getNombre());
            pstmt.setInt(2, m.getStock());
            pstmt.setDouble(3, m.getPrecio());
            pstmt.setInt(4, m.getCreadorId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void actualizar(Medicamento m) {
        String sql = "UPDATE medicamentos SET nombre=?, stock=?, precio=? WHERE id=?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, m.getNombre());
            pstmt.setInt(2, m.getStock());
            pstmt.setDouble(3, m.getPrecio());
            pstmt.setInt(4, m.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM medicamentos WHERE id=?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
