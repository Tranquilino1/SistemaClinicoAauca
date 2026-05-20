package com.clinica.aauca.dao;

import com.clinica.aauca.model.Paciente;
import java.util.List;

public interface PacienteDAO {
    List<Paciente> obtenerPacientes(int offset, int limit);
    void crearPaciente(Paciente paciente);
    void actualizarPaciente(Paciente paciente);
    void eliminarPaciente(int id);
    List<Paciente> buscarPacientePorNombre(String nombre, int offset, int limit);
    int contarTotalPacientes();
    int contarTotalPacientes(String query);
}
