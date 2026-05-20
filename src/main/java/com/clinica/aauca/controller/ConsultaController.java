package com.clinica.aauca.controller;

import com.clinica.aauca.dao.ConsultaDAO;
import com.clinica.aauca.model.Consulta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.time.LocalDate;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.awt.Desktop;

/**
 * Controlador para la gestión de Consultas Médicas en la Clínica AAUCA.
 * Permite registrar diagnósticos, recetas, antecedentes y gestionar la facturación.
 */
public class ConsultaController {
    // --- Elementos de la Interfaz (Inyectados desde FXML) ---
    @FXML private TextField txtPacienteId;      // HC o ID del paciente
    @FXML private Label lblEstadoPaciente;       // Nombre y estado de verificación del paciente
    @FXML private Label lblStatusConsulta;       // Estado de la consulta (NUEVO, GUARDADO, IMPRESO)
    @FXML private TextField txtMotivo;           // Razón de la consulta
    @FXML private TextArea txtHistoriaActual;    // Historia de la enfermedad actual
    @FXML private TextField txtAntFamiliares;    // Antecedentes familiares
    @FXML private TextField txtAntPersonales;    // Antecedentes personales
    @FXML private TextArea txtExamenFisico;      // Examen físico
    @FXML private TextField txtDiagnostico;      // Conclusión médica
    @FXML private TextField txtLaboratorio;      // Resultados de laboratorio
    @FXML private TextArea txtTratamiento;       // Instrucciones médicas
    @FXML private TextArea txtReceta;            // Listado de medicamentos y dosis
    @FXML private Label lblSignosVitales;        // Visualización de signos vitales (Triaje)
    @FXML private VBox cajaFacturacion;          // Panel de cobros (FCFA)
    @FXML private TextField txtFactura;          // Monto total a cobrar
    @FXML private Label lblMensajeEstudiante;    // Aviso de exención de pago para alumnos
    @FXML private Button btnGuardar;
    @FXML private Button btnEditar;
    @FXML private Button btnImprimir;
    @FXML private Button btnEliminar;            // Ahora funciona como "Vaciar Formulario"

    @FXML private ComboBox<com.clinica.aauca.model.Medicamento> comboMeds; // Selección de fármacos
    @FXML private Spinner<Integer> spinCant;                             // Cantidad a recetar

    private com.clinica.aauca.dao.MedicamentoDAO medDAO = new com.clinica.aauca.dao.MedicamentoDAO();

    // --- Tabla de Historial ---
    @FXML private TableView<Consulta> tablaConsultas;
    @FXML private TableColumn<Consulta, String> colConsultaId;
    @FXML private TableColumn<Consulta, String> colMotivo;
    @FXML private TableColumn<Consulta, String> colFecha;

    private ConsultaDAO dao = new ConsultaDAO();
    private com.clinica.aauca.dao.ConfiguracionDAO configDAO = new com.clinica.aauca.dao.ConfiguracionDAO();
    private com.clinica.aauca.dao.SignosVitalesDAO svDao = new com.clinica.aauca.dao.SignosVitalesDAOImpl();
    
    private boolean isEstudiante = false;         // Indica si el paciente tiene descuento total
    private int pacienteActualId = -1;            // ID del paciente en pantalla
    private Consulta consultaSeleccionada = null; // Referencia a la consulta cargada para editar

    /**
     * Se ejecuta al cargar la vista. Configura tablas y listeners de edición.
     */
    @FXML
    public void initialize() {
        // Vinculación de datos con las columnas de la tabla
        colConsultaId.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty("C" + cellData.getValue().getId()));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        // Detectar cuando el usuario empieza a escribir para activar el aviso de "Navegación bloqueada"
        txtMotivo.textProperty().addListener((o, old, v) -> com.clinica.aauca.util.Operacion.iniciar());
        txtHistoriaActual.textProperty().addListener((o, old, v) -> com.clinica.aauca.util.Operacion.iniciar());
        txtDiagnostico.textProperty().addListener((o, old, v) -> com.clinica.aauca.util.Operacion.iniciar());
        txtTratamiento.textProperty().addListener((o, old, v) -> com.clinica.aauca.util.Operacion.iniciar());
        txtReceta.textProperty().addListener((o, old, v) -> com.clinica.aauca.util.Operacion.iniciar());

        // Cargar datos en el formulario al hacer clic en una fila del historial
        tablaConsultas.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                consultaSeleccionada = sel;
                cargarDatosConsulta(sel);
            }
        });

        // Configurar selector numérico y cargar catálogo de farmacia
        spinCant.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 400, 1));
        cargarMedicinas();
    }

    /**
     * Carga el listado de medicamentos disponibles en el ComboBox.
     */
    private void cargarMedicinas() {
        ObservableList<com.clinica.aauca.model.Medicamento> listaMeds = FXCollections.observableArrayList(medDAO.obtenerTodos());
        comboMeds.setItems(listaMeds);
        
        // Formateo visual de la lista desplegable
        comboMeds.setCellFactory(lv -> new ListCell<com.clinica.aauca.model.Medicamento>() {
            @Override protected void updateItem(com.clinica.aauca.model.Medicamento m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty ? "" : m.getNombre() + " (Stock: " + m.getStock() + " | " + m.getPrecio() + " FCFA)");
            }
        });
        comboMeds.setButtonCell(new ListCell<com.clinica.aauca.model.Medicamento>() {
            @Override protected void updateItem(com.clinica.aauca.model.Medicamento m, boolean empty) {
                super.updateItem(m, empty);
                setText(empty ? "" : (m != null ? m.getNombre() : ""));
            }
        });
    }

    /**
     * Añade el fármaco seleccionado a la receta y suma su precio al total.
     */
    @FXML
    public void addMedAReceta() {
        com.clinica.aauca.model.Medicamento m = comboMeds.getValue();
        if (m == null) return;
        int cant = spinCant.getValue();
        
        // Validación de inventario antes de recetar
        if (cant > m.getStock()) {
            mostrarAlerta("Sin Stock", "Inválido", "No hay suficiente stock para " + m.getNombre(), Alert.AlertType.ERROR);
            return;
        }

        // Concatenar al área de receta médica
        String actual = txtReceta.getText();
        txtReceta.setText(actual + (actual.isEmpty() ? "" : "\n") + "- " + m.getNombre() + " [" + cant + " unds]");
        
        // Aplicar cobro solo si el paciente es particular
        if (!isEstudiante) {
            try {
                double actualFactura = Double.parseDouble(txtFactura.getText().trim());
                double extra = m.getPrecio() * cant;
                txtFactura.setText(String.valueOf(actualFactura + extra));
            } catch (Exception e) {}
        }
    }

    /**
     * Rellena el formulario con datos de una consulta guardada.
     * Si la consulta fue impresa, bloquea todos los campos para evitar cambios.
     */
    private void cargarDatosConsulta(Consulta c) {
        txtMotivo.setText(c.getMotivo());
        txtHistoriaActual.setText(c.getHistoriaActual());
        txtAntFamiliares.setText(c.getAntecedentesFamiliares());
        txtAntPersonales.setText(c.getAntecedentesPersonales());
        txtExamenFisico.setText(c.getExamenFisico());
        txtDiagnostico.setText(c.getDiagnostico());
        txtLaboratorio.setText(c.getLaboratorio());
        txtTratamiento.setText(c.getTratamiento());
        txtReceta.setText(c.getReceta());
        txtFactura.setText(c.getFactura());
        lblStatusConsulta.setText("Estado: " + c.getEstado().toUpperCase());

        boolean isImpresa = "impreso".equalsIgnoreCase(c.getEstado());
        
        // Bloqueo de seguridad: No se puede editar ni volver a guardar si ya se imprimió
        btnGuardar.setDisable(isImpresa || consultaSeleccionada != null);
        btnEditar.setDisable(isImpresa || consultaSeleccionada == null);
        btnEliminar.setDisable(false);
        
        txtMotivo.setEditable(!isImpresa);
        txtHistoriaActual.setEditable(!isImpresa);
        txtAntFamiliares.setEditable(!isImpresa);
        txtAntPersonales.setEditable(!isImpresa);
        txtExamenFisico.setEditable(!isImpresa);
        txtDiagnostico.setEditable(!isImpresa);
        txtLaboratorio.setEditable(!isImpresa);
        txtTratamiento.setEditable(!isImpresa);
        txtReceta.setEditable(!isImpresa);
        txtFactura.setEditable(!isImpresa);
    }

    /**
     * Busca al paciente por ID y determina si debe pagar o está exento.
     */
    @FXML
    public void verificarPaciente() {
        String idStr = txtPacienteId.getText().trim();
        if (idStr.isEmpty()) return;

        try {
            int id = Integer.parseInt(idStr);
            String nombre = dao.obtenerNombrePaciente(id);
            if (nombre != null) {
                pacienteActualId = id;
                isEstudiante = dao.esEstudiante(id);
                lblEstadoPaciente.setText("✔ " + nombre);
                lblEstadoPaciente.setStyle("-fx-text-fill: #1976D2; -fx-font-weight: bold;");
                cargarConsultasPaciente(id);
                
                // Cargar Signos Vitales
                java.util.Optional<com.clinica.aauca.model.SignosVitales> svOpt = svDao.obtenerUltimosSignos(id);
                if (svOpt.isPresent()) {
                    com.clinica.aauca.model.SignosVitales sv = svOpt.get();
                    lblSignosVitales.setText(String.format("Peso: %s kg | Temp: %s °C | Pulso: %s bpm | Resp: %s rpm | P.A: %s | Talla: %s cm",
                        sv.getPeso(), sv.getTemperatura(), sv.getFrecuenciaCardiaca(), sv.getFrecuenciaRespiratoria(), sv.getPresionArterial(), sv.getTalla()));
                } else {
                    lblSignosVitales.setText("No hay registros de signos vitales recientes.");
                }

                // Configurar mensaje de exención si es estudiante AAUCA
                if (isEstudiante) {
                    cajaFacturacion.setDisable(true);
                    lblMensajeEstudiante.setVisible(true);
                    lblMensajeEstudiante.setManaged(true);
                    txtFactura.setText("EXENTO");
                } else {
                    cajaFacturacion.setDisable(false);
                    lblMensajeEstudiante.setVisible(false);
                    lblMensajeEstudiante.setManaged(false);
                    com.clinica.aauca.model.Configuracion config = configDAO.obtenerConfiguracion();
                    // Tarifa base incrementada para particulares según plan
                    double precio = (config != null) ? config.getPrecioConsulta() : 5000;
                    txtFactura.setText(String.valueOf(precio));
                }
                limpiarCamposFormulario();
                consultaSeleccionada = null;
                btnGuardar.setDisable(false);
                btnEditar.setDisable(true);
            } else {
                lblEstadoPaciente.setText("✗ Paciente no encontrado");
                lblEstadoPaciente.setStyle("-fx-text-fill: red;");
                lblSignosVitales.setText("—");
                pacienteActualId = -1;
                tablaConsultas.setItems(FXCollections.emptyObservableList());
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "ID Inválido", "El ID debe ser un número entero.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error de Base de Datos", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Limpia los campos del formulario y libera el bloqueo de navegación.
     */
    @FXML
    public void limpiarCamposFormulario() {
        txtMotivo.clear();
        txtHistoriaActual.clear();
        txtAntFamiliares.clear();
        txtAntPersonales.clear();
        txtExamenFisico.clear();
        txtDiagnostico.clear();
        txtLaboratorio.clear();
        txtTratamiento.clear();
        txtReceta.clear();
        lblStatusConsulta.setText("Estado: NUEVO");
        com.clinica.aauca.util.Operacion.finalizar(); // Finalizar bloqueo de navegación
        consultaSeleccionada = null;
        btnGuardar.setDisable(pacienteActualId == -1);
        btnEditar.setDisable(true);
    }

    /**
     * Obtiene y muestra todas las consultas previas del paciente seleccionado.
     */
    private void cargarConsultasPaciente(int pacienteId) {
        try {
            ObservableList<Consulta> lista = FXCollections.observableArrayList(dao.obtenerConsultasPorPaciente(pacienteId));
            tablaConsultas.setItems(lista);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registra una nueva consulta validando que los campos mínimos estén completos.
     */
    @FXML
    public void guardarConsulta() {
        if (pacienteActualId == -1) {
            mostrarAlerta("Atención", "Paciente No Verificado", "Debe buscar y verificar un paciente antes de guardar.", Alert.AlertType.WARNING);
            return;
        }
        // Validación de campos requeridos (Motivo e Impresión Diagnóstica)
        if (txtMotivo.getText().trim().isEmpty() || txtDiagnostico.getText().trim().isEmpty()) {
            mostrarAlerta("Atención", "Campos Vacíos", "Los campos Motivo e Impresión Diagnóstica son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Consulta c = new Consulta(0, pacienteActualId, LocalDate.now().toString(),
                    txtMotivo.getText(), txtHistoriaActual.getText(), txtAntFamiliares.getText(),
                    txtAntPersonales.getText(), txtExamenFisico.getText(), txtDiagnostico.getText(),
                    txtLaboratorio.getText(), txtTratamiento.getText(), txtReceta.getText(),
                    txtFactura.getText(), "guardado");
            
            dao.registrarConsulta(c);
            
            // Lógica de Descuento de Stock de medicamentos
            com.clinica.aauca.model.Medicamento m = comboMeds.getValue();
            if (m != null && txtReceta.getText().contains(m.getNombre())) {
                int nuevaCant = m.getStock() - spinCant.getValue();
                if (nuevaCant >= 0) {
                    m.setStock(nuevaCant);
                    medDAO.actualizar(m);
                }
            }

            mostrarAlerta("Éxito", "Consulta Registrada", "La consulta se ha guardado correctamente.", Alert.AlertType.INFORMATION);
            com.clinica.aauca.util.Operacion.finalizar();
            limpiarCamposFormulario();
            cargarConsultasPaciente(pacienteActualId);
            cargarMedicinas(); 
        } catch (Exception e) {
            mostrarAlerta("Error", "Fallo al Guardar", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Guarda cambios en una consulta seleccionada (Solo si no está impresa).
     */
    @FXML
    public void editarConsulta() {
        if (consultaSeleccionada == null) return;
        if ("impreso".equalsIgnoreCase(consultaSeleccionada.getEstado())) {
            mostrarAlerta("Acceso Denegado", "Consulta Impresa", "No se puede modificar una consulta que ya ha sido impresa.", Alert.AlertType.ERROR);
            return;
        }

        try {
            consultaSeleccionada.setMotivo(txtMotivo.getText());
            consultaSeleccionada.setHistoriaActual(txtHistoriaActual.getText());
            consultaSeleccionada.setAntecedentesFamiliares(txtAntFamiliares.getText());
            consultaSeleccionada.setAntecedentesPersonales(txtAntPersonales.getText());
            consultaSeleccionada.setExamenFisico(txtExamenFisico.getText());
            consultaSeleccionada.setDiagnostico(txtDiagnostico.getText());
            consultaSeleccionada.setLaboratorio(txtLaboratorio.getText());
            consultaSeleccionada.setTratamiento(txtTratamiento.getText());
            consultaSeleccionada.setReceta(txtReceta.getText());
            consultaSeleccionada.setFactura(txtFactura.getText());
            
            dao.actualizarConsulta(consultaSeleccionada);
            mostrarAlerta("Éxito", "Consulta Actualizada", "Los cambios han sido guardados.", Alert.AlertType.INFORMATION);
            com.clinica.aauca.util.Operacion.finalizar();
            cargarConsultasPaciente(pacienteActualId);
        } catch (Exception e) {
            mostrarAlerta("Error", "Fallo al Actualizar", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Genera el informe PDF y bloquea permanentemente la consulta.
     */
    @FXML
    public void imprimirConsulta() {
        if (consultaSeleccionada == null) {
            mostrarAlerta("Atención", "Sin Selección", "Seleccione una consulta para imprimir.", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            java.util.List<String[]> datos = new java.util.ArrayList<>();
            datos.add(new String[]{"Campo", "Detalle de la Consulta"});
            datos.add(new String[]{"Código Consulta", "C" + consultaSeleccionada.getId()});
            datos.add(new String[]{"Fecha", consultaSeleccionada.getFecha()});
            datos.add(new String[]{"Motivo", consultaSeleccionada.getMotivo()});
            datos.add(new String[]{"Historia Actual", consultaSeleccionada.getHistoriaActual()});
            datos.add(new String[]{"Ant. Fam.", consultaSeleccionada.getAntecedentesFamiliares()});
            datos.add(new String[]{"Ant. Pers.", consultaSeleccionada.getAntecedentesPersonales()});
            datos.add(new String[]{"Examen Físico", consultaSeleccionada.getExamenFisico()});
            datos.add(new String[]{"Diagnóstico", consultaSeleccionada.getDiagnostico()});
            datos.add(new String[]{"Laboratorio", consultaSeleccionada.getLaboratorio()});
            datos.add(new String[]{"Tratamiento", consultaSeleccionada.getTratamiento()});
            datos.add(new String[]{"Receta Médica", consultaSeleccionada.getReceta()});
            datos.add(new String[]{"Factura (FCFA)", consultaSeleccionada.getFactura()});

            String path = com.clinica.aauca.util.PDFService.generarDocumento("Consulta_C" + consultaSeleccionada.getId(), 
                "Informe de Consulta Médica", "Paciente ID: " + consultaSeleccionada.getPacienteId(), datos);

            if (path != null) {
                // Sellar como impreso para evitar ediciones posteriores
                consultaSeleccionada.setEstado("impreso");
                dao.actualizarConsulta(consultaSeleccionada);
                cargarConsultasPaciente(pacienteActualId);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Acción del botón Eliminar (Sustituido por Vaciar Formulario).
     */
    @FXML
    public void eliminarConsulta() {
        limpiarCamposFormulario();
    }

    private void mostrarAlerta(String titulo, String cabecera, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecera);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }

    /**
     * Carga un paciente externo al módulo de consultas.
     */
    public void setPacienteHC(int id) {
        txtPacienteId.setText(String.valueOf(id));
        verificarPaciente();
    }
}
