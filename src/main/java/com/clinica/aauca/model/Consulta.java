package com.clinica.aauca.model;

public class Consulta {
    private int id;
    private int pacienteId;
    private String fecha;
    private String motivo;
    private String historiaActual;
    private String antecedentesFamiliares;
    private String antecedentesPersonales;
    private String examenFisico;
    private String diagnostico;
    private String laboratorio;
    private String tratamiento;
    private String receta;
    private String factura;
    private String estado;

    public Consulta(int id, int pacienteId, String fecha, String motivo, String historiaActual, String antecedentesFamiliares, String antecedentesPersonales, String examenFisico, String diagnostico, String laboratorio, String tratamiento, String receta, String factura, String estado) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.fecha = fecha;
        this.motivo = motivo;
        this.historiaActual = historiaActual;
        this.antecedentesFamiliares = antecedentesFamiliares;
        this.antecedentesPersonales = antecedentesPersonales;
        this.examenFisico = examenFisico;
        this.diagnostico = diagnostico;
        this.laboratorio = laboratorio;
        this.tratamiento = tratamiento;
        this.receta = receta;
        this.factura = factura;
        this.estado = estado;
    }

    // Getters
    public int getId() { return id; }
    public int getPacienteId() { return pacienteId; }
    public String getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public String getHistoriaActual() { return historiaActual; }
    public String getAntecedentesFamiliares() { return antecedentesFamiliares; }
    public String getAntecedentesPersonales() { return antecedentesPersonales; }
    public String getExamenFisico() { return examenFisico; }
    public String getDiagnostico() { return diagnostico; }
    public String getLaboratorio() { return laboratorio; }
    public String getTratamiento() { return tratamiento; }
    public String getReceta() { return receta != null ? receta : "—"; }
    public String getFactura() { return factura != null ? factura : "—"; }
    public String getEstado() { return estado != null ? estado : "guardado"; }

    // Setters
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public void setHistoriaActual(String historiaActual) { this.historiaActual = historiaActual; }
    public void setAntecedentesFamiliares(String antecedentesFamiliares) { this.antecedentesFamiliares = antecedentesFamiliares; }
    public void setAntecedentesPersonales(String antecedentesPersonales) { this.antecedentesPersonales = antecedentesPersonales; }
    public void setExamenFisico(String examenFisico) { this.examenFisico = examenFisico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public void setLaboratorio(String laboratorio) { this.laboratorio = laboratorio; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public void setReceta(String receta) { this.receta = receta; }
    public void setFactura(String factura) { this.factura = factura; }
    public void setEstado(String estado) { this.estado = estado; }
}
