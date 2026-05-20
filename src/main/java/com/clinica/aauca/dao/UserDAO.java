package com.clinica.aauca.dao;

import com.clinica.aauca.model.User;
import java.util.Optional;

/**
 * Capa de Acceso a Datos (DAO) para Usuarios.
 */
public interface UserDAO {
    Optional<User> findByUsername(String username);
    boolean authenticate(String username, String password);
    Optional<User> login(String username, String password); // Nuevo método
}
