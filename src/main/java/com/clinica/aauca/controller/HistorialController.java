package com.clinica.aauca.controller;

import com.clinica.aauca.dao.ConsultaDAO;
import com.clinica.aauca.dao.HospitalizacionDAO;
import com.clinica.aauca.model.Consulta;
import com.clinica.aauca.model.Hospitalizacion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;
import java.util.List;

public class HistorialController {

    @FXML private TextField txtPacienteId;
    @FXML private Label lblNombrePaciente;
    @FXML private TabPane tabPane;

    // Consultas tab
    @FXML private TableView<Consulta> tablaConsultasH;
    @FXML private TableColumn<Consulta, String> colHFecha;
    @FXML private TableColumn<Consulta, String> colHReceta;
    @FXML private TableColumn<Consulta, String> colHFactura;

    // Hospitalizaciones tab
    @FXML private TableView<Hospitalizacion> tablaHospH;
    @FXML private TableColumn<Hospitalizacion, String> colHEstado;
    @FXML private TableColumn<Hospitalizacion, String> colHSeguimiento;

    // Resumen tab
    @FXML private Label lblResumenNombre;
    @FXML private Label lblTotalConsultas;
    @FXML private Label lblTotalHosp;
    @FXML private Label lblTotalFacturado;

    private ConsultaDAO consultaDAO = new ConsultaDAO();
    private HospitalizacionDAO hospDAO = new HospitalizacionDAO();

    @FXML
    public void initialize() {
        colHFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHReceta.setCellValueFactory(new PropertyValueFactory<>("receta"));
        colHFactura.setCellValueFactory(new PropertyValueFactory<>("factura"));
        colHEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colHSeguimiento.setCellValueFactory(new PropertyValueFactory<>("seguimiento"));
    }

    @FXML
    public void cargarHistorial() {
        String idStr = txtPacienteId.getText().trim();
        if (idStr.isEmpty()) return;

        try {
            int id = Integer.parseInt(idStr);
            String nombre = consultaDAO.obtenerNombrePaciente(id);

            if (nombre == null) {
                lblNombrePaciente.setText("✗ Paciente no encontrado");
                lblNombrePaciente.setStyle("-fx-text-fill: red;");
                return;
            }

            lblNombrePaciente.setText("✔ " + nombre);
            lblNombrePaciente.setStyle("-fx-text-fill: #1976D2; -fx-font-weight: bold;");

            // Cargar consultas
            List<Consulta> consultas = consultaDAO.obtenerConsultasPorPaciente(id);
            tablaConsultasH.setItems(FXCollections.observableArrayList(consultas));

            // Cargar hospitalizaciones
            List<Hospitalizacion> hosps = hospDAO.obtenerPorPaciente(id);
            tablaHospH.setItems(FXCollections.observableArrayList(hosps));

            // Calcular resumen
            lblResumenNombre.setText("Paciente: " + nombre);
            lblTotalConsultas.setText(String.valueOf(consultas.size()));
            lblTotalHosp.setText(String.valueOf(hosps.size()));

            long totalFcfa = consultas.stream()
                .mapToLong(c -> {
                    try {
                        String f = c.getFactura();
                        if (f == null || f.isBlank() || f.equals("EXENTO") || f.equals("—")) return 0;
                        return Long.parseLong(f.trim().replace("FCFA", "").trim());
                    } catch (NumberFormatException e) { return 0; }
                }).sum();
            lblTotalFacturado.setText(totalFcfa + " FCFA");

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "ID Inválido", "Ingrese un número de ID válido.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error BD", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void exportarHistorialPDF() {
        if (tablaConsultasH.getItems().isEmpty() && tablaHospH.getItems().isEmpty()) {
            mostrarAlerta("Atención", "Sin Datos", "Cargue primero el historial de un paciente.", Alert.AlertType.WARNING);
            return;
        }

        try {
            String nombreP = lblNombrePaciente.getText().replace("✔ ", "");
            java.util.List<String[]> datos = new java.util.ArrayList<>();
            datos.add(new String[]{"Fecha", "Actividad / Diagnóstico", "Estado / Monto"});

            for (Consulta c : tablaConsultasH.getItems()) {
                datos.add(new String[]{c.getFecha(), "CONSULTA: " + c.getDiagnostico(), c.getFactura()});
            }
            for (Hospitalizacion h : tablaHospH.getItems()) {
                datos.add(new String[]{h.getFechaIngreso(), "HOSPITALIZACIÓN: " + h.getMotivo(), h.getEstado()});
            }

            com.clinica.aauca.util.PDFService.generarDocumento("Historial_Medico_" + txtPacienteId.getText(), 
                "Historial Clínico Completo", "Paciente: " + nombreP, datos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String t, String h, String c, Alert.AlertType tipo) {
        Alert a = new Alert(tipo); a.setTitle(t); a.setHeaderText(h); a.setContentText(c); a.showAndWait();
    }
}
