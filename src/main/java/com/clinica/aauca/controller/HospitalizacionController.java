package com.clinica.aauca.controller;

import com.clinica.aauca.dao.HospitalizacionDAO;
import com.clinica.aauca.model.Hospitalizacion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;

/**
 * Controlador para la gestión de Hospitalizaciones e Ingresos Clínicos.
 * Permite registrar la evolución del paciente, gestionar altas y calcular costos de estancia.
 */
public class HospitalizacionController {
    // --- Elementos de la Interfaz (FXML) ---
    @FXML private TextField txtPacienteId;      // HC o ID del paciente a buscar
    @FXML private Label lblNombrePaciente;       // Muestra el nombre tras la verificación
    @FXML private ComboBox<String> comboEstado;  // Estado: 'ingresado' o 'de alta'
    @FXML private TextField txtMotivo;           // Causa del internamiento
    @FXML private TextArea txtSeguimiento;       // Evolución médica diaria
    @FXML private Button btnNuevoIngreso;        // Botón para iniciar un nuevo registro

    private com.clinica.aauca.model.User currentUser; // Usuario actual de la sesión

    // --- Tabla de Registros ---
    @FXML private TableView<Hospitalizacion> tablaHospitalizaciones;
    @FXML private TableColumn<Hospitalizacion, Integer> colHospId;
    @FXML private TableColumn<Hospitalizacion, String> colNombrePaciente;
    @FXML private TableColumn<Hospitalizacion, String> colFechaTabla;
    @FXML private TableColumn<Hospitalizacion, String> colEstadoTabla;
    @FXML private TableColumn<Hospitalizacion, String> colMotivoTabla;
    @FXML private TableColumn<Hospitalizacion, String> colSeguimientoTabla;

    private HospitalizacionDAO dao = new HospitalizacionDAO();
    private int hospitalizacionActualId = -1;  // ID del internamiento seleccionado
    private int pacienteActualId = -1;         // ID del paciente activo

    /**
     * Configura el acceso y visibilidad de funciones según el rol (Médico/Enfermero).
     */
    public void setCurrentUser(com.clinica.aauca.model.User user) {
        this.currentUser = user;
        if (user != null && "Enfermero".equals(user.getRole())) {
            // Un enfermero suele gestionar la evolución, no decide el ingreso inicial (ejemplo de regla)
            btnNuevoIngreso.setVisible(false);
            btnNuevoIngreso.setManaged(false);
        }
    }

    /**
     * Inicializa las columnas de la tabla y carga los registros activos.
     */
    @FXML
    public void initialize() {
        colHospId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombrePaciente.setCellValueFactory(new PropertyValueFactory<>("nombrePaciente"));
        colFechaTabla.setCellValueFactory(new PropertyValueFactory<>("fechaIngreso"));
        colEstadoTabla.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colMotivoTabla.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colSeguimientoTabla.setCellValueFactory(new PropertyValueFactory<>("seguimiento"));

        comboEstado.setItems(FXCollections.observableArrayList("ingresado", "de alta"));
        comboEstado.setValue("ingresado");

        // Cargar solo hospitalizaciones activas por defecto al abrir el módulo
        cargarHospitalizacionesActivas();

        // Listener: Cargar datos en los campos al seleccionar una fila de la tabla
        tablaHospitalizaciones.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                hospitalizacionActualId = sel.getId();
                comboEstado.setValue(sel.getEstado());
                txtMotivo.setText(sel.getMotivo());
                txtSeguimiento.setText(sel.getSeguimiento());
            }
        });
    }

    /**
     * Obtiene de la base de datos todos los pacientes actualmente ingresados.
     */
    private void cargarHospitalizacionesActivas() {
        try {
            ObservableList<Hospitalizacion> lista = FXCollections.observableArrayList(dao.obtenerHospitalizacionesActivas());
            tablaHospitalizaciones.setItems(lista);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Busca un paciente por su ID y muestra su historial de internamientos.
     */
    @FXML
    public void buscarInternamiento() {
        String idStr = txtPacienteId.getText().trim();
        if (idStr.isEmpty()) return;

        try {
            pacienteActualId = Integer.parseInt(idStr);
            String nombre = dao.obtenerNombrePaciente(pacienteActualId);

            if (nombre != null) {
                lblNombrePaciente.setText("✔ " + nombre);
                lblNombrePaciente.setStyle("-fx-text-fill: #1976D2; -fx-font-weight: bold;");
                cargarHistorial(pacienteActualId);
            } else {
                lblNombrePaciente.setText("✗ Paciente no encontrado");
                lblNombrePaciente.setStyle("-fx-text-fill: red;");
                pacienteActualId = -1;
                tablaHospitalizaciones.setItems(FXCollections.emptyObservableList());
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Búsqueda Fallida", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarHistorial(int pacienteId) {
        try {
            ObservableList<Hospitalizacion> lista = FXCollections.observableArrayList(dao.obtenerPorPaciente(pacienteId));
            tablaHospitalizaciones.setItems(lista);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prepara la interfaz para crear un nuevo registro de ingreso.
     */
    @FXML
    public void nuevoIngreso() {
        if (pacienteActualId == -1) {
            mostrarAlerta("Atención", "Paciente Requerido", "Busque un paciente primero.", Alert.AlertType.WARNING);
            return;
        }
        comboEstado.setValue("ingresado");
        txtMotivo.clear();
        txtSeguimiento.clear();
        hospitalizacionActualId = -1;
    }

    /**
     * Guarda un nuevo ingreso o actualiza la evolución de uno existente.
     * Si el estado cambia a "de alta", se dispara el proceso de facturación.
     */
    @FXML
    public void actualizarEvolucion() {
        if (pacienteActualId == -1) {
            mostrarAlerta("Atención", "Sin Paciente", "Busque un paciente antes de guardar.", Alert.AlertType.WARNING);
            return;
        }
        if (txtMotivo.getText().trim().isEmpty()) {
            mostrarAlerta("Atención", "Campo Requerido", "El campo 'Motivo' es obligatorio.", Alert.AlertType.WARNING);
            return;
        }

        try {
            if (hospitalizacionActualId != -1) {
                // Actualizar registro existente
                String nuevoEstado = comboEstado.getValue();
                dao.actualizarEvolucion(hospitalizacionActualId, nuevoEstado, txtSeguimiento.getText());
                
                // Lógica de Alta: Cálculo de días y precio
                if ("de alta".equalsIgnoreCase(nuevoEstado)) {
                    procesarAlta(hospitalizacionActualId);
                }
                mostrarAlerta("Éxito", "Datos Actualizados", "Seguimiento guardado correctamente.", Alert.AlertType.INFORMATION);
            } else {
                // Crear nuevo registro
                dao.crearNuevaHospitalizacion(pacienteActualId, comboEstado.getValue(), txtSeguimiento.getText(), txtMotivo.getText());
                mostrarAlerta("Éxito", "Ingreso Creado", "Nuevo registro de hospitalización generado.", Alert.AlertType.INFORMATION);
            }
            cargarHistorial(pacienteActualId);
            limpiarCampos();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al Guardar", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void limpiarCampos() {
        txtMotivo.clear();
        txtSeguimiento.clear();
        hospitalizacionActualId = -1;
    }

    /**
     * Genera un reporte PDF con el historial del internamiento seleccionado.
     */
    @FXML
    public void imprimirReporteHosp() {
        Hospitalizacion sel = tablaHospitalizaciones.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Atención", "Sin Selección", "Seleccione un ingreso de la tabla.", Alert.AlertType.WARNING);
            return;
        }

        try {
            java.util.List<String[]> datos = new java.util.ArrayList<>();
            datos.add(new String[]{"Campo", "Información"});
            datos.add(new String[]{"N° Expediente", String.valueOf(sel.getId())});
            datos.add(new String[]{"Paciente", sel.getNombrePaciente()});
            datos.add(new String[]{"Fecha Ingreso", sel.getFechaIngreso()});
            datos.add(new String[]{"Motivo", sel.getMotivo()});
            datos.add(new String[]{"Estado Actual", sel.getEstado()});
            datos.add(new String[]{"Evolución", sel.getSeguimiento()});

            com.clinica.aauca.util.PDFService.generarDocumento("Reporte_Hospitalizacion_" + sel.getId(), 
                "Reporte de Evolución Hospitalaria", "Información de Internamiento", datos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina permanentemente un registro de hospitalización (Solo para corrección de errores).
     */
    @FXML
    public void eliminarHospitalizacion() {
        Hospitalizacion sel = tablaHospitalizaciones.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea eliminar este registro permanentemente?");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                try {
                    dao.eliminarHospitalizacion(sel.getId());
                    cargarHistorial(pacienteActualId);
                    mostrarAlerta("Éxito", "Eliminado", "Registro borrado.", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    mostrarAlerta("Error", "Fallo", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    /**
     * Calcula los días de estancia y genera el monto total a pagar tras el alta.
     */
    private void procesarAlta(int hospId) {
        try {
            Hospitalizacion h = dao.obtenerPorId(hospId);
            if (h == null) return;

            // Cálculo de estancia
            java.time.LocalDate ingreso = java.time.LocalDate.parse(h.getFechaIngreso());
            java.time.LocalDate hoy = java.time.LocalDate.now();
            long dias = java.time.temporal.ChronoUnit.DAYS.between(ingreso, hoy);
            if (dias <= 0) dias = 1; // Mínimo se cobra un día

            // Verificar si el paciente es estudiante (exento de pago)
            boolean esEst = dao.esEstudiante(h.getPacienteId());
            
            // Obtener precio base de hospitalización desde configuración
            com.clinica.aauca.dao.ConfiguracionDAO configDAO = new com.clinica.aauca.dao.ConfiguracionDAO();
            com.clinica.aauca.model.Configuracion config = configDAO.obtenerConfiguracion();
            double precioDia = (config != null) ? config.getPrecioHospitalizacion() : 15000;
            
            double total = esEst ? 0 : (dias * precioDia);
            dao.registrarAltaYFactura(hospId, hoy.toString(), total);

            String msg = esEst ? "Paciente Estudiante: EXENTO DE PAGO" : "Total a pagar: " + total + " FCFA";
            mostrarAlerta("Facturación de Alta", "Resumen de Cobro", 
                "Días de estancia: " + dias + "\n" + msg, Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            System.err.println("Error en proceso de alta: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String cabecera, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecera);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }

    public void setPacienteHC(int id) {
        txtPacienteId.setText(String.valueOf(id));
        buscarInternamiento();
    }
}
