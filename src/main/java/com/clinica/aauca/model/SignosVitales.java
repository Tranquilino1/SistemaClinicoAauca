package com.clinica.aauca.model;

public class SignosVitales {
    private int id;
    private int pacienteId;
    private String fecha;
    private String peso;
    private String temperatura;
    private String frecuenciaCardiaca;
    private String frecuenciaRespiratoria;
    private String presionArterial;
    private String talla;

    public SignosVitales(int id, int pacienteId, String fecha, String peso, String temperatura, String frecuenciaCardiaca, String frecuenciaRespiratoria, String presionArterial, String talla) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.fecha = fecha;
        this.peso = peso;
        this.temperatura = temperatura;
        this.frecuenciaCardiaca = frecuenciaCardiaca;
        this.frecuenciaRespiratoria = frecuenciaRespiratoria;
        this.presionArterial = presionArterial;
        this.talla = talla;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPacienteId() { return pacienteId; }
    public void setPacienteId(int pacienteId) { this.pacienteId = pacienteId; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getPeso() { return peso; }
    public void setPeso(String peso) { this.peso = peso; }
    public String getTemperatura() { return temperatura; }
    public void setTemperatura(String temperatura) { this.temperatura = temperatura; }
    public String getFrecuenciaCardiaca() { return frecuenciaCardiaca; }
    public void setFrecuenciaCardiaca(String frecuenciaCardiaca) { this.frecuenciaCardiaca = frecuenciaCardiaca; }
    public String getFrecuenciaRespiratoria() { return frecuenciaRespiratoria; }
    public void setFrecuenciaRespiratoria(String frecuenciaRespiratoria) { this.frecuenciaRespiratoria = frecuenciaRespiratoria; }
    public String getPresionArterial() { return presionArterial; }
    public void setPresionArterial(String presionArterial) { this.presionArterial = presionArterial; }
    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }
}
