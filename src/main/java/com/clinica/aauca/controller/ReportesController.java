package com.clinica.aauca.controller;

import com.clinica.aauca.util.DatabaseConnector;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class ReportesController {

    @FXML private Label lblTotalPacientes;
    @FXML private Label lblTotalConsultas;
    @FXML private Label lblTotalHosp;
    @FXML private Label lblTotalMed;
    @FXML private Label lblTotalIngresos;

    @FXML private TableView<ObservableList<Object>> tablaTopPacientes;
    @FXML private TableColumn<ObservableList<Object>, String> colRpNombre;
    @FXML private TableColumn<ObservableList<Object>, Integer> colRpConsultas;
    @FXML private TableColumn<ObservableList<Object>, Integer> colRpHosp;
    @FXML private TableColumn<ObservableList<Object>, String> colRpTotal;

    @FXML
    public void initialize() {
        colRpNombre.setCellValueFactory(row -> new SimpleStringProperty((String) row.getValue().get(0)));
        colRpConsultas.setCellValueFactory(row -> new SimpleIntegerProperty((Integer) row.getValue().get(1)).asObject());
        colRpHosp.setCellValueFactory(row -> new SimpleIntegerProperty((Integer) row.getValue().get(2)).asObject());
        colRpTotal.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().get(3) + " FCFA"));
        actualizarEstadisticas();
    }

    @FXML
    public void actualizarEstadisticas() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            // Totales
            lblTotalPacientes.setText(contar(conn, "SELECT COUNT(*) FROM pacientes"));
            lblTotalConsultas.setText(contar(conn, "SELECT COUNT(*) FROM consultas"));
            lblTotalHosp.setText(contar(conn, "SELECT COUNT(*) FROM hospitalizaciones"));
            lblTotalMed.setText(contar(conn, "SELECT COUNT(*) FROM medicamentos"));

            // Total facturado
            String sqlIngresos = "SELECT SUM(CAST(factura AS REAL)) FROM consultas WHERE factura != 'EXENTO' AND factura != '' AND factura IS NOT NULL";
            try (PreparedStatement ps = conn.prepareStatement(sqlIngresos);
                 ResultSet rs = ps.executeQuery()) {
                double total = rs.next() ? rs.getDouble(1) : 0;
                lblTotalIngresos.setText(String.format("%.0f FCFA", total));
            }

            // Top 5 pacientes
            String sqlTop = "SELECT p.nombre_completo, " +
                "(SELECT COUNT(*) FROM consultas c WHERE c.paciente_id = p.id) AS num_consultas, " +
                "(SELECT COUNT(*) FROM hospitalizaciones h WHERE h.paciente_id = p.id) AS num_hosp, " +
                "(SELECT COALESCE(SUM(CAST(c2.factura AS REAL)),0) FROM consultas c2 WHERE c2.paciente_id = p.id AND c2.factura != 'EXENTO') AS total_fcfa " +
                "FROM pacientes p ORDER BY num_consultas DESC LIMIT 5";

            ObservableList<ObservableList<Object>> datos = FXCollections.observableArrayList();
            try (PreparedStatement ps = conn.prepareStatement(sqlTop);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ObservableList<Object> fila = FXCollections.observableArrayList();
                    fila.add(rs.getString(1));
                    fila.add(rs.getInt(2));
                    fila.add(rs.getInt(3));
                    fila.add(String.format("%.0f", rs.getDouble(4)));
                    datos.add(fila);
                }
            }
            tablaTopPacientes.setItems(datos);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void exportarPDF() {
        try {
            java.util.List<String[]> datos = new java.util.ArrayList<>();
            datos.add(new String[]{"Indicador Estadístico", "Valor Actual"});
            datos.add(new String[]{"Total de Pacientes Registrados", lblTotalPacientes.getText()});
            datos.add(new String[]{"Total de Consultas Médicas", lblTotalConsultas.getText()});
            datos.add(new String[]{"Total de Hospitalizaciones", lblTotalHosp.getText()});
            datos.add(new String[]{"Stock de Medicamentos", lblTotalMed.getText()});
            datos.add(new String[]{"Facturación Total Recaudada", lblTotalIngresos.getText()});

            String path = com.clinica.aauca.util.PDFService.generarDocumento("Reporte_Estadistico_Clinico", 
                "Resumen de Actividad Clínica", "Estadísticas Globales de la Institución", datos);
            
            if (path == null) {
                Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo generar el reporte PDF.");
                a.show();
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    private String contar(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? String.valueOf(rs.getInt(1)) : "0";
        }
    }
}
