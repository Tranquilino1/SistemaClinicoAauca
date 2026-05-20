package com.clinica.aauca.dao;

import com.clinica.aauca.model.User;
import com.clinica.aauca.util.DatabaseConnector;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM usuarios WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
            throw new RuntimeException("Error de conexión a la base de datos: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean authenticate(String username, String password) {
        return login(username, password).isPresent();
    }

    @Override
    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            try {
                if (BCrypt.checkpw(password, user.getPassword())) {
                    return Optional.of(user);
                }
            } catch (Exception e) {
                System.err.println("Error al verificar password: " + e.getMessage());
            }
        }
        return Optional.empty();
    }

    public List<User> obtenerTodos() {
        List<User> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY nombre_completo";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapUser(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public void crear(User user) {
        String sql = "INSERT INTO usuarios(username, password, nombre_completo, rol) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getRole());
            ps.executeUpdate();
        } catch (SQLException e) { 
            e.printStackTrace();
            throw new RuntimeException("No se pudo crear el usuario: " + e.getMessage());
        }
    }

    public void actualizar(User user) {
        String sql = "UPDATE usuarios SET username=?, nombre_completo=?, rol=?, password=? WHERE id=?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getPassword());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
        } catch (SQLException e) { 
            e.printStackTrace();
            throw new RuntimeException("No se pudo actualizar el usuario: " + e.getMessage());
        }
    }

    public void actualizarPassword(int userId, String newHash) {
        String sql = "UPDATE usuarios SET password=? WHERE id=?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { 
            e.printStackTrace();
            throw new RuntimeException("No se pudo actualizar la contraseña: " + e.getMessage());
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM usuarios WHERE id=?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { 
            e.printStackTrace();
            throw new RuntimeException("No se pudo eliminar el usuario: " + e.getMessage());
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("nombre_completo"),
            rs.getString("rol")
        );
    }
}
