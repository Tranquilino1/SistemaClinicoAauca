package com.clinica.aauca.model;

public class Hospitalizacion {
    private int id;
    private int pacienteId;
    private String estado;
    private String seguimiento;
    private String fechaIngreso;
    private String motivo;
    private String nombrePaciente; // Campo dinámico para vistas

    public Hospitalizacion(int id, int pacienteId, String estado, String seguimiento, String fechaIngreso, String motivo) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.estado = estado;
        this.seguimiento = seguimiento;
        this.fechaIngreso = fechaIngreso;
        this.motivo = motivo;
    }

    public int getId() { return id; }
    public int getPacienteId() { return pacienteId; }
    public String getEstado() { return estado != null ? estado : "—"; }
    public String getSeguimiento() { return seguimiento != null ? seguimiento : "—"; }
    public String getFechaIngreso() { return fechaIngreso != null ? fechaIngreso : "—"; }
    public String getMotivo() { return motivo != null ? motivo : "—"; }
    public String getNombrePaciente() { return nombrePaciente != null ? nombrePaciente : "Cargando..."; }

    public void setEstado(String estado) { this.estado = estado; }
    public void setSeguimiento(String seguimiento) { this.seguimiento = seguimiento; }
    public void setFechaIngreso(String fecha) { this.fechaIngreso = fecha; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public void setNombrePaciente(String nombre) { this.nombrePaciente = nombre; }
}
