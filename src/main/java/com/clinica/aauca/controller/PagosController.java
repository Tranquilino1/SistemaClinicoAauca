package com.clinica.aauca.controller;

import com.clinica.aauca.model.Consulta;
import com.clinica.aauca.util.DatabaseConnector;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class PagosController {

    @FXML private TextField txtFiltroId;
    @FXML private Label lblTotalPago;

    @FXML private TableView<ObservableList<Object>> tablaFacturas;
    @FXML private TableColumn<ObservableList<Object>, String> colFId;
    @FXML private TableColumn<ObservableList<Object>, String> colFPaciente;
    @FXML private TableColumn<ObservableList<Object>, String> colFFecha;
    @FXML private TableColumn<ObservableList<Object>, String> colFReceta;
    @FXML private TableColumn<ObservableList<Object>, String> colFMonto;
    @FXML private TableColumn<ObservableList<Object>, String> colFEstado;

    @FXML private Label lblSumaCobrado;
    @FXML private Label lblSumaExento;
    @FXML private Label lblTotalRegistros;

    @FXML
    public void initialize() {
        colFId.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().get(0))));
        colFPaciente.setCellValueFactory(r -> new SimpleStringProperty((String) r.getValue().get(1)));
        colFFecha.setCellValueFactory(r -> new SimpleStringProperty((String) r.getValue().get(2)));
        colFReceta.setCellValueFactory(r -> new SimpleStringProperty((String) r.getValue().get(3)));
        colFMonto.setCellValueFactory(r -> new SimpleStringProperty((String) r.getValue().get(4)));
        colFEstado.setCellValueFactory(r -> new SimpleStringProperty((String) r.getValue().get(5)));

        tablaFacturas.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                String monto = (String) sel.get(4);
                lblTotalPago.setText(monto);
            }
        });

        verTodas();
    }

    @FXML
    public void verTodas() {
        cargarFacturas(null);
    }

    @FXML
    public void filtrarPorPaciente() {
        String id = txtFiltroId.getText().trim();
        cargarFacturas(id.isEmpty() ? null : id);
    }

    private void cargarFacturas(String pacienteIdFiltro) {
        ObservableList<ObservableList<Object>> datos = FXCollections.observableArrayList();
        long sumaCobrado = 0;
        int sumaExento = 0;

        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT c.id, p.nombre_completo, c.fecha, c.receta, c.factura " +
                         "FROM consultas c JOIN pacientes p ON c.paciente_id = p.id";
            if (pacienteIdFiltro != null) sql += " WHERE c.paciente_id = " + pacienteIdFiltro;
            sql += " ORDER BY c.fecha DESC";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ObservableList<Object> fila = FXCollections.observableArrayList();
                    fila.add(rs.getInt("id"));
                    fila.add(rs.getString("nombre_completo"));
                    fila.add(rs.getString("fecha") != null ? rs.getString("fecha") : "—");
                    String receta = rs.getString("receta");
                    fila.add(receta != null && receta.length() > 50 ? receta.substring(0, 50) + "..." : receta);
                    String factura = rs.getString("factura");
                    boolean esExento = factura == null || factura.isBlank() || factura.equals("EXENTO");
                    fila.add(esExento ? "EXENTO" : factura + " FCFA");
                    fila.add(esExento ? "✅ Exento" : "💰 Cobrado");
                    if (!esExento) {
                        try { sumaCobrado += Long.parseLong(factura.trim()); } catch (NumberFormatException ignored) {}
                    } else {
                        sumaExento++;
                    }
                    datos.add(fila);
                }
            }

            // AÑADIR DEUDAS DE HOSPITALIZACIÓN
            String sqlHosp = "SELECT h.id, p.nombre_completo, h.fecha_alta, h.motivo, h.monto_total " +
                             "FROM hospitalizaciones h JOIN pacientes p ON h.paciente_id = p.id " +
                             "WHERE h.estado = 'de alta' AND h.monto_total > 0";
            if (pacienteIdFiltro != null) sqlHosp += " AND h.paciente_id = " + pacienteIdFiltro;

            try (PreparedStatement ps = conn.prepareStatement(sqlHosp);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ObservableList<Object> fila = FXCollections.observableArrayList();
                    fila.add("H-" + rs.getInt("id"));
                    fila.add(rs.getString("nombre_completo"));
                    fila.add(rs.getString("fecha_alta") != null ? rs.getString("fecha_alta") : "—");
                    fila.add("Hosp: " + rs.getString("motivo"));
                    double monto = rs.getDouble("monto_total");
                    fila.add(monto + " FCFA");
                    fila.add("🏥 Hosp.");
                    sumaCobrado += monto;
                    datos.add(fila);
                }
            }

            tablaFacturas.setItems(datos);
            lblSumaCobrado.setText(sumaCobrado + " FCFA");
            lblSumaExento.setText(String.valueOf(sumaExento));
            lblTotalRegistros.setText(String.valueOf(datos.size()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void imprimirTicket() {
        ObservableList<Object> sel = tablaFacturas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione una factura de la tabla para imprimir el ticket.").showAndWait();
            return;
        }

        try {
            java.util.List<String[]> datos = new java.util.ArrayList<>();
            datos.add(new String[]{"Concepto", "Detalle"});
            datos.add(new String[]{"N° Comprobante", String.valueOf(sel.get(0))});
            datos.add(new String[]{"Paciente", (String) sel.get(1)});
            datos.add(new String[]{"Fecha Emisión", (String) sel.get(2)});
            datos.add(new String[]{"Descripción", (String) sel.get(3)});
            datos.add(new String[]{"Estado", (String) sel.get(5)});
            datos.add(new String[]{"MONTO TOTAL", (String) sel.get(4)});

            com.clinica.aauca.util.PDFService.generarDocumento("Ticket_Pago_" + sel.get(0), 
                "Comprobante de Pago / Ticket", "Documento de Carácter Oficial - Clínica AAUCA", datos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void eliminarFactura() {
        ObservableList<Object> sel = tablaFacturas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.WARNING, "Seleccione un registro de la tabla.").showAndWait();
            return;
        }

        Object rawId = sel.get(0);
        String idStr = String.valueOf(rawId);
        boolean esHosp = idStr.startsWith("H-");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar este registro de cobro del sistema?");
        confirm.setHeaderText("Confirmar Eliminación");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                try (Connection conn = DatabaseConnector.getConnection()) {
                    if (esHosp) {
                        int idH = Integer.parseInt(idStr.replace("H-", ""));
                        try (PreparedStatement ps = conn.prepareStatement("UPDATE hospitalizaciones SET monto_total = 0 WHERE id=?")) {
                            ps.setInt(1, idH);
                            ps.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM consultas WHERE id=?")) {
                            // Convertir a int de forma segura (soporta Integer y Long de SQLite)
                            int idInt = ((Number) rawId).intValue();
                            ps.setInt(1, idInt);
                            ps.executeUpdate();
                        }
                    }
                    verTodas();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).showAndWait();
                }
            }
        });
    }
}
