package com.clinica.aauca.dao;

import com.clinica.aauca.model.SignosVitales;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface SignosVitalesDAO {
    void registrarSignos(SignosVitales sv) throws SQLException;
    Optional<SignosVitales> obtenerUltimosSignos(int pacienteId) throws SQLException;
    List<SignosVitales> obtenerTodosSignos(int pacienteId) throws SQLException;
}

