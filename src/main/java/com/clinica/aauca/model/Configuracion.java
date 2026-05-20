package com.clinica.aauca.model;

public class Configuracion {
    private String nombreClinica;
    private String direccion;
    private String telefono;
    private String email;
    private String moneda;
    private double precioConsulta;
    private double precioHospitalizacion;
    private String logoPath;

    public Configuracion() {}

    // Getters and Setters
    public String getNombreClinica() { return nombreClinica; }
    public void setNombreClinica(String nombreClinica) { this.nombreClinica = nombreClinica; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public double getPrecioConsulta() { return precioConsulta; }
    public void setPrecioConsulta(double precioConsulta) { this.precioConsulta = precioConsulta; }

    public double getPrecioHospitalizacion() { return precioHospitalizacion; }
    public void setPrecioHospitalizacion(double precioHospitalizacion) { this.precioHospitalizacion = precioHospitalizacion; }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }
}
