package com.clinica.aauca.controller;

import com.clinica.aauca.dao.PacienteDAO;
import com.clinica.aauca.dao.PacienteDAOImpl;
import com.clinica.aauca.model.Paciente;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import java.util.List;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Pos;

public class PacientesController {
    @FXML private FlowPane containerPacientes;
    @FXML private TextField txtBuscar;
    @FXML private Label lblPagina;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Button btnNuevoPaciente;

    private PacienteDAO dao = new PacienteDAOImpl();
    private com.clinica.aauca.dao.SignosVitalesDAO svDao = new com.clinica.aauca.dao.SignosVitalesDAOImpl();
    private ObservableList<Paciente> lista;
    private int paginaActual = 1;
    private int totalPaginas = 1;
    private final int LIMITE = 12; 
    private String consultaActual = "";

    @FXML
    public void initialize() {
        cargarPagina();
        verificarPermisos();
    }

    private void verificarPermisos() {
        if (DashboardController.getInstancia() != null) {
            com.clinica.aauca.model.User user = DashboardController.getInstancia().getCurrentUser();
            if (user != null && "Médico".equals(user.getRole())) {
                btnNuevoPaciente.setVisible(false);
                btnNuevoPaciente.setManaged(false);
            }
        }
    }

    private void cargarPagina() {
        int offset = (paginaActual - 1) * LIMITE;
        int totalRegistros;
        
        if (consultaActual.isEmpty()) {
            lista = FXCollections.observableArrayList(dao.obtenerPacientes(offset, LIMITE));
            totalRegistros = dao.contarTotalPacientes();
        } else {
            lista = FXCollections.observableArrayList(dao.buscarPacientePorNombre(consultaActual, offset, LIMITE));
            totalRegistros = dao.contarTotalPacientes(consultaActual);
        }

        totalPaginas = (int) Math.ceil((double) totalRegistros / LIMITE);
        if (totalPaginas == 0) totalPaginas = 1;

        renderizarTarjetas();
        actualizarEstadoUI();
    }

    private void actualizarEstadoUI() {
        lblPagina.setText("Página " + paginaActual + " de " + totalPaginas);
        btnAnterior.setDisable(paginaActual == 1);
        btnSiguiente.setDisable(paginaActual >= totalPaginas);
    }

    private void renderizarTarjetas() {
        containerPacientes.getChildren().clear();
        for (Paciente p : lista) {
            containerPacientes.getChildren().add(crearTarjetaPaciente(p));
        }
    }

    private VBox crearTarjetaPaciente(Paciente p) {
        VBox card = new VBox(10);
        card.getStyleClass().add("modulo-card");
        card.setPrefSize(220, 240);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setGlyphName("USER_CIRCLE");
        icon.setSize("6em");
        icon.setFill(p.isEsEstudiante() ? Color.valueOf("#003366") : Color.valueOf("#4A4A4A"));

        Label lblNombre = new Label(p.getNombreCompleto());
        lblNombre.getStyleClass().add("patient-name");
        lblNombre.setWrapText(true);

        Label lblId = new Label("ID: #" + p.getId());
        lblId.setStyle("-fx-text-fill: gray;");

        Label lblStatus = new Label(p.getTipo() != null ? p.getTipo().toUpperCase() : (p.isEsEstudiante() ? "ESTUDIANTE AAUCA" : "PARTICULAR / PERSONAL"));
        lblStatus.setStyle("-fx-font-size: 10px; -fx-padding: 3 8 3 8; -fx-background-radius: 10; " +
            (p.isEsEstudiante() ? "-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2;" : "-fx-background-color: #F5F5F5; -fx-text-fill: #616161;"));

        Label lblNac = new Label("Nac: " + (p.getFechaNacimiento() != null ? p.getFechaNacimiento() : "N/A"));
        lblNac.setStyle("-fx-font-size: 11px;");

        card.getChildren().addAll(icon, lblNombre, lblId, lblStatus, lblNac);
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F0F4F8; -fx-background-radius: 15; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        card.setOnMouseClicked(e -> mostrarActividadPaciente(p));

        return card;
    }

    private void mostrarActividadPaciente(Paciente p) {
        try {
            javafx.scene.Scene scene = containerPacientes.getScene();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/clinica/aauca/view/paciente_detalle.fxml"));
            javafx.scene.Node view = loader.load();
            
            PacienteDetalleController ctrl = loader.getController();
            ctrl.setPaciente(p);

            StackPane workingArea = (StackPane) scene.lookup("#workingArea");
            if (workingArea != null) {
                workingArea.getChildren().clear();
                workingArea.getChildren().add(view);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void paginaAnterior() {
        if (paginaActual > 1) {
            paginaActual--;
            cargarPagina();
        }
    }

    @FXML
    private void paginaSiguiente() {
        if (paginaActual < totalPaginas) {
            paginaActual++;
            cargarPagina();
        }
    }

    @FXML
    private void buscarPaciente() {
        consultaActual = txtBuscar.getText().trim();
        paginaActual = 1;
        cargarPagina();
    }

    @FXML
    private void crearPaciente() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/clinica/aauca/view/nuevo_paciente_dialog.fxml"));
            javafx.scene.Parent root = loader.load();
            NuevoPacienteDialogController dialogController = loader.getController();

            Dialog<javafx.util.Pair<Paciente, com.clinica.aauca.model.SignosVitales>> dialog = new Dialog<>();
            dialog.setTitle("Nuevo Paciente y Triaje");
            dialog.setHeaderText(null);
            
            ButtonType btnGuardar = new ButtonType("Guardar Todo", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(root);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == btnGuardar) {
                    if (!dialogController.isInputValid()) {
                        mostrarAlertaError("Los campos Nombres y Apellidos son obligatorios.");
                        return null;
                    }
                    return dialogController.getResultData();
                }
                return null;
            });

            // Intercept close request (e.g. from the 'X' button)
            dialog.getDialogPane().getScene().getWindow().setOnCloseRequest(event -> {
                if (dialogController.hasChanges()) {
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
                            if (dialogController.isInputValid()) {
                                Button okButton = (Button) dialog.getDialogPane().lookupButton(btnGuardar);
                                if (okButton != null) {
                                    okButton.fire();
                                }
                            } else {
                                mostrarAlertaError("Los campos Nombres y Apellidos son obligatorios.");
                                event.consume();
                            }
                        } else if (opt.get() == btnNo) {
                            dialog.close();
                        } else {
                            event.consume(); // keep dialog open
                        }
                    } else {
                        event.consume(); // keep dialog open
                    }
                }
            });
            
            // Intercept Cancel button click
            Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
            if (cancelButton != null) {
                cancelButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    if (dialogController.hasChanges()) {
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
                                if (dialogController.isInputValid()) {
                                    Button okButton = (Button) dialog.getDialogPane().lookupButton(btnGuardar);
                                    if (okButton != null) {
                                        okButton.fire();
                                    }
                                    event.consume();
                                } else {
                                    mostrarAlertaError("Los campos Nombres y Apellidos son obligatorios.");
                                    event.consume();
                                }
                            } else if (opt.get() == btnNo) {
                                // Let the cancel action proceed (closes dialog)
                            } else {
                                event.consume(); // Keep dialog open
                            }
                        } else {
                            event.consume(); // Keep dialog open
                        }
                    }
                });
            }

            java.util.Optional<javafx.util.Pair<Paciente, com.clinica.aauca.model.SignosVitales>> resultado = dialog.showAndWait();
            resultado.ifPresent(pair -> {
                try {
                    dao.crearPaciente(pair.getKey());
                    int nuevoId = 0;
                    List<Paciente> busqueda = dao.buscarPacientePorNombre(pair.getKey().getNombreCompleto(), 0, 1);
                    if (!busqueda.isEmpty()) nuevoId = busqueda.get(0).getId();
                    
                    if (nuevoId > 0) {
                        pair.getValue().setPacienteId(nuevoId);
                        svDao.registrarSignos(pair.getValue());
                    }

                    Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                    alerta.setTitle("Éxito");
                    alerta.setHeaderText("Paciente y Signos Registrados");
                    alerta.setContentText("El paciente ha sido guardado y sus signos vitales registrados.");
                    alerta.showAndWait();
                    cargarPagina();
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarAlertaError("Error al guardar: " + e.getMessage());
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlertaError("Error al cargar el diálogo: " + ex.getMessage());
        }
    }

    private void mostrarAlertaError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}