package com.clinica.aauca.model;

public class Paciente {
    private int id;
    private String nombreCompleto;
    private String tipo; // Reemplaza o complementa esEstudiante
    private boolean esEstudiante; // Mantenemos para lógica de pagos si existe
    private String fechaNacimiento;
    private String sexo;
    private String direccion;
    private String telefono;
    private String nacionalidad;
    private boolean hospitalizado;

    public Paciente(int id, String nombreCompleto, String tipo, boolean esEstudiante, String fechaNacimiento, String sexo, String direccion, String telefono, String nacionalidad) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.tipo = tipo;
        this.esEstudiante = esEstudiante;
        this.fechaNacimiento = fechaNacimiento;
        this.sexo = sexo;
        this.direccion = direccion;
        this.telefono = telefono;
        this.nacionalidad = nacionalidad;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public boolean isEsEstudiante() { return esEstudiante; }
    public void setEsEstudiante(boolean esEstudiante) { this.esEstudiante = esEstudiante; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(String fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }
    public boolean isHospitalizado() { return hospitalizado; }
    public void setHospitalizado(boolean hosp) { this.hospitalizado = hosp; }
}
