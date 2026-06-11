package com.clinica.aauca.controller;

import com.clinica.aauca.dao.ConsultaDAO;
import com.clinica.aauca.dao.HospitalizacionDAO;
import com.clinica.aauca.model.Consulta;
import com.clinica.aauca.model.Hospitalizacion;
import com.clinica.aauca.model.Paciente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;

public class PacienteDetalleController {

    @FXML private Label lblNombre;
    @FXML private Label lblTipo;
    @FXML private Label lblEdad;
    @FXML private Label lblNacimiento;
    @FXML private Label lblEstadoHosp; 
    @FXML private Button btnNuevaConsulta;
    @FXML private Button btnHospitalizar;
    // Botón inyectado desde FXML para registrar una nueva tanda de signos vitales (triaje)
    @FXML private Button btnRegistrarTriaje;
    @FXML private Tab tabConsultas;

    @FXML private TableView<Consulta> tablaConsultas;
    @FXML private TableColumn<Consulta, String> colConsFecha;
    @FXML private TableColumn<Consulta, String> colConsMotivo;
    @FXML private TableColumn<Consulta, String> colConsDiag;
    @FXML private TableColumn<Consulta, String> colConsTrat;
    @FXML private TableColumn<Consulta, String> colConsEstado;

    @FXML private TableView<Hospitalizacion> tablaHospitalizaciones;
    @FXML private TableColumn<Hospitalizacion, String> colHospFecha;
    @FXML private TableColumn<Hospitalizacion, String> colHospMotivo;
    @FXML private TableColumn<Hospitalizacion, String> colHospEstado;
    @FXML private TableColumn<Hospitalizacion, String> colHospSeg;

    @FXML private TableView<com.clinica.aauca.model.SignosVitales> tablaSignosVitales;
    @FXML private TableColumn<com.clinica.aauca.model.SignosVitales, String> colSvFecha;
    @FXML private TableColumn<com.clinica.aauca.model.SignosVitales, String> colSvPeso;
    @FXML private TableColumn<com.clinica.aauca.model.SignosVitales, String> colSvTemp;
    @FXML private TableColumn<com.clinica.aauca.model.SignosVitales, String> colSvPulso;
    @FXML private TableColumn<com.clinica.aauca.model.SignosVitales, String> colSvResp;
    @FXML private TableColumn<com.clinica.aauca.model.SignosVitales, String> colSvPresion;
    @FXML private TableColumn<com.clinica.aauca.model.SignosVitales, String> colSvTalla;

    @FXML private VBox cajaDetalleConsulta;
    @FXML private Label lblDetMotivo;
    @FXML private Label lblDetDiag;
    @FXML private Label lblDetTrat;
    @FXML private Label lblDetAnt;

    private Paciente pacienteActual;
    private ConsultaDAO consultaDAO = new ConsultaDAO();
    private HospitalizacionDAO hospitalizacionDAO = new HospitalizacionDAO();
    private com.clinica.aauca.dao.SignosVitalesDAO svDAO = new com.clinica.aauca.dao.SignosVitalesDAOImpl();

    @FXML
    public void initialize() {
        // Init columnas consultas
        colConsFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colConsMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colConsDiag.setCellValueFactory(new PropertyValueFactory<>("diagnostico"));
        colConsTrat.setCellValueFactory(new PropertyValueFactory<>("tratamiento"));
        colConsEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Init columnas hospitalizaciones
        colHospFecha.setCellValueFactory(new PropertyValueFactory<>("fechaIngreso"));
        colHospMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colHospEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colHospSeg.setCellValueFactory(new PropertyValueFactory<>("seguimiento"));

        // Init columnas signos vitales
        colSvFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colSvPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        colSvTemp.setCellValueFactory(new PropertyValueFactory<>("temperatura"));
        colSvPulso.setCellValueFactory(new PropertyValueFactory<>("frecuenciaCardiaca"));
        colSvResp.setCellValueFactory(new PropertyValueFactory<>("frecuenciaRespiratoria"));
        colSvPresion.setCellValueFactory(new PropertyValueFactory<>("presionArterial"));
        colSvTalla.setCellValueFactory(new PropertyValueFactory<>("talla"));

        // Listener para ver detalles de consulta
        tablaConsultas.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                mostrarDetalleConsulta(sel);
            }
        });

        aplicarPermisosRol();
    }

    private void aplicarPermisosRol() {
        com.clinica.aauca.model.User user = DashboardController.getInstancia().getCurrentUser();
        if (user != null && "Enfermero".equals(user.getRole())) {
            btnNuevaConsulta.setVisible(false);
            btnNuevaConsulta.setManaged(false);
            btnHospitalizar.setVisible(false);
            btnHospitalizar.setManaged(false);
            
            // El enfermero no debe ver las consultas (confidencial)
            tabConsultas.setDisable(true); 
            tablaConsultas.setVisible(false);
        }
    }

    public void setPaciente(Paciente p) {
        this.pacienteActual = p;
        lblNombre.setText(p.getNombreCompleto());
        lblTipo.setText(p.isEsEstudiante() ? "ESTUDIANTE AAUCA" : "PARTICULAR / PERSONAL");
        lblNacimiento.setText("F. Nac: " + p.getFechaNacimiento());
        
        if (p.getFechaNacimiento() != null && !p.getFechaNacimiento().isEmpty()) {
            try {
                LocalDate birthDate = LocalDate.parse(p.getFechaNacimiento());
                int edad = Period.between(birthDate, LocalDate.now()).getYears();
                lblEdad.setText("Edad: " + edad + " años");
            } catch (Exception e) {
                lblEdad.setText("Edad: N/A");
            }
        }

        // Configurar Etiqueta de Hospitalización
        if (p.isHospitalizado()) {
            lblEstadoHosp.setText("HOSPITALIZADO");
            lblEstadoHosp.setStyle("-fx-background-color: #FFF3E0; -fx-text-fill: #E65100; -fx-padding: 2 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            lblEstadoHosp.setText("NO HOSPITALIZADO");
            lblEstadoHosp.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-padding: 2 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
        }

        cargarActividad();
    }

    private void cargarActividad() {
        try {
            ObservableList<Consulta> consultas = FXCollections.observableArrayList(consultaDAO.obtenerConsultasPorPaciente(pacienteActual.getId()));
            
            // Si es enfermero, no cargamos los datos de consulta en la tabla
            com.clinica.aauca.model.User user = DashboardController.getInstancia().getCurrentUser();
            if (user != null && "Enfermero".equals(user.getRole())) {
                tablaConsultas.setItems(FXCollections.emptyObservableList());
            } else {
                tablaConsultas.setItems(consultas);
            }

            ObservableList<Hospitalizacion> hosp = FXCollections.observableArrayList(hospitalizacionDAO.obtenerPorPaciente(pacienteActual.getId()));
            tablaHospitalizaciones.setItems(hosp);

            ObservableList<com.clinica.aauca.model.SignosVitales> svList = FXCollections.observableArrayList(svDAO.obtenerTodosSignos(pacienteActual.getId()));
            tablaSignosVitales.setItems(svList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void mostrarDetalleConsulta(Consulta c) {
        cajaDetalleConsulta.setVisible(true);
        cajaDetalleConsulta.setManaged(true);
        lblDetMotivo.setText(c.getMotivo());
        lblDetDiag.setText(c.getDiagnostico());
        lblDetTrat.setText(c.getTratamiento());
        lblDetAnt.setText(c.getAntecedentesPersonales());
    }

    @FXML
    public void nuevaConsulta() {
        if (pacienteActual != null) {
            DashboardController.getInstancia().navegarAPaciente("Nueva Consulta", "consulta_form.fxml", pacienteActual.getId());
        }
    }

    @FXML
    public void hospitalizar() {
        if (pacienteActual != null) {
            DashboardController.getInstancia().navegarAPaciente("Hospitalización", "hospitalizacion_view.fxml", pacienteActual.getId());
        }
    }

    /**
     * Carga e inicializa el diálogo FXML de registro de signos vitales (triaje) para el paciente actual.
     * Gestiona la confirmación de cambios pendientes y la persistencia de datos mediante el DAO correspondiente.
     */
    @FXML
    public void registrarTriaje() {
        // Verifica que exista un paciente actual seleccionado
        if (pacienteActual != null) {
            try {
                // Instancia el cargador FXML para cargar el diseño del diálogo de triaje
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/clinica/aauca/view/nuevo_triaje_dialog.fxml"));
                // Carga el nodo raíz de la interfaz gráfica
                javafx.scene.Parent root = loader.load();
                // Obtiene el controlador asociado para interactuar con la lógica del formulario
                NuevoTriajeDialogController dialogController = loader.getController();

                // Instancia el contenedor del diálogo tipado para retornar un objeto SignosVitales
                Dialog<com.clinica.aauca.model.SignosVitales> dialog = new Dialog<>();
                // Establece el título de la ventana
                dialog.setTitle("Nuevo Triaje de Paciente");
                // Remueve el texto de cabecera por defecto para un look más limpio
                dialog.setHeaderText(null);

                // Define los botones de acción del diálogo: Guardar y Cancelar
                ButtonType btnGuardar = new ButtonType("Guardar Triaje", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
                // Asigna el contenido FXML al panel de diálogo
                dialog.getDialogPane().setContent(root);

                // Define el convertidor de resultado al presionar los botones del diálogo
                dialog.setResultConverter(dialogButton -> {
                    // Si el usuario presiona "Guardar Triaje", retorna los datos ingresados
                    if (dialogButton == btnGuardar) {
                        return dialogController.getResultData(pacienteActual.getId());
                    }
                    // Si presiona Cancelar, retorna null
                    return null;
                });

                // Intercepta el evento de cierre de la ventana (por ejemplo, clic en el botón 'X')
                dialog.getDialogPane().getScene().getWindow().setOnCloseRequest(event -> {
                    // Si hay cambios no guardados en el formulario
                    if (dialogController.hasChanges()) {
                        // Muestra una confirmación al usuario para advertir la pérdida de datos
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Cambios no guardados");
                        alert.setHeaderText("Tiene cambios sin guardar en el formulario.");
                        alert.setContentText("¿Desea guardarlos antes de salir?");
                        
                        // Define los botones de la confirmación
                        ButtonType btnSi = new ButtonType("Guardar");
                        ButtonType btnNo = new ButtonType("Descartar");
                        ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
                        alert.getButtonTypes().setAll(btnSi, btnNo, btnCancel);
                        
                        // Captura la respuesta del usuario
                        java.util.Optional<ButtonType> opt = alert.showAndWait();
                        if (opt.isPresent()) {
                            if (opt.get() == btnSi) {
                                // Intenta simular el clic en el botón de guardar
                                Button okButton = (Button) dialog.getDialogPane().lookupButton(btnGuardar);
                                if (okButton != null) {
                                    okButton.fire();
                                }
                            } else if (opt.get() == btnNo) {
                                // Cierra el diálogo descartando cambios
                                dialog.close();
                            } else {
                                // Cancela el cierre y mantiene el diálogo abierto
                                event.consume();
                            }
                        } else {
                            // Mantiene el diálogo abierto si no hay respuesta clara
                            event.consume();
                        }
                    }
                });

                // Intercepta el clic del botón de cancelación estándar del diálogo
                Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
                if (cancelButton != null) {
                    cancelButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                        // Si el usuario tiene cambios pendientes de guardar
                        if (dialogController.hasChanges()) {
                            // Pregunta si desea guardar, descartar o cancelar la salida
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Cambios no guardados");
                            alert.setHeaderText("Tiene cambios sin guardar en el formulario.");
                            alert.setContentText("¿Desea guardarlos antes de salir?");
                            
                            ButtonType btnSi = new ButtonType("Guardar");
                            ButtonType btnNo = new ButtonType("Descartar");
                            ButtonType btnCancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
                            alert.getButtonTypes().setAll(btnSi, btnNo, btnCancel);
                            
                            java.util.Optional<ButtonType> opt = alert.showAndWait();
                            if (opt.isPresent()) {
                                if (opt.get() == btnSi) {
                                    // Invoca el proceso de guardado y detiene el evento de cancelación inmediata
                                    Button okButton = (Button) dialog.getDialogPane().lookupButton(btnGuardar);
                                    if (okButton != null) {
                                        okButton.fire();
                                    }
                                    event.consume();
                                } else if (opt.get() == btnNo) {
                                    // Permite continuar la cancelación (cerrar el diálogo)
                                } else {
                                    // Consume el evento para abortar la cancelación y seguir editando
                                    event.consume();
                                }
                            } else {
                                // Aborta la cancelación
                                event.consume();
                            }
                        }
                    });
                }

                // Despliega el diálogo y espera a que el usuario complete la acción
                java.util.Optional<com.clinica.aauca.model.SignosVitales> resultado = dialog.showAndWait();
                resultado.ifPresent(sv -> {
                    try {
                        // Registra los nuevos signos vitales en la persistencia local de la base de datos
                        svDAO.registrarSignos(sv);
                        
                        // Muestra una ventana emergente informando el éxito de la operación
                        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                        alerta.setTitle("Éxito");
                        alerta.setHeaderText("Triaje Registrado");
                        alerta.setContentText("Los nuevos signos vitales han sido registrados correctamente.");
                        alerta.showAndWait();
                        
                        // Refresca la información del paciente en pantalla para reflejar los nuevos datos en las tablas
                        cargarActividad();
                    } catch (Exception e) {
                        e.printStackTrace();
                        mostrarAlertaError("Error al registrar signos vitales: " + e.getMessage());
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                mostrarAlertaError("Error al cargar el formulario de triaje: " + ex.getMessage());
            }
        }
    }

    /**
     * Muestra una ventana emergente de tipo error al usuario.
     * 
     * @param mensaje El mensaje descriptivo del fallo ocurrido.
     */
    private void mostrarAlertaError(String mensaje) {
        // Instancia una alerta de tipo ERROR de JavaFX
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        // Asigna el texto informativo del error
        alerta.setContentText(mensaje);
        // Muestra la ventana y bloquea la ejecución hasta que se cierre
        alerta.showAndWait();
    }
}
