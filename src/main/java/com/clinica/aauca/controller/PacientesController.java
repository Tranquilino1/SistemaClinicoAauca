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
import javafx.scene.paint.Color;
import java.util.List;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

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
        // Obtenemos el usuario de la sesión a través del DashboardController (Singleton)
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
        card.getStyleClass().add("modulo-card"); // Reutilizar estilo de tarjeta
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
        
        // Efecto hover
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F0F4F8; -fx-background-radius: 15; -fx-scale-x: 1.02; -fx-scale-y: 1.02; -fx-transition: 0.3s;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        // Acción al hacer clic: Ver detalle y actividad
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
        Dialog<javafx.util.Pair<Paciente, com.clinica.aauca.model.SignosVitales>> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Paciente y Triaje");
        dialog.setHeaderText("Registro de Paciente y Signos Vitales (Enfermería)");

        ButtonType btnGuardar = new ButtonType("Guardar Todo", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 20, 10, 10));

        // Campos del Paciente
        TextField txtNombre = new TextField(); txtNombre.setPromptText("Nombres");
        TextField txtApellidos = new TextField(); txtApellidos.setPromptText("Apellidos");
        ComboBox<String> comboTipo = new ComboBox<>(FXCollections.observableArrayList("Estudiante", "Docente", "Administrativo", "Particular"));
        comboTipo.setPromptText("Seleccione Tipo");
        ComboBox<String> comboSexo = new ComboBox<>(FXCollections.observableArrayList("Masculino", "Femenino", "Otro"));
        TextField txtFechaNac = new TextField(); txtFechaNac.setPromptText("YYYY-MM-DD");
        TextField txtDireccion = new TextField(); txtDireccion.setPromptText("Residencia");
        TextField txtTelefono = new TextField(); txtTelefono.setPromptText("Número de Teléfono");
        TextField txtNacionalidad = new TextField(); txtNacionalidad.setPromptText("Nacionalidad");

        // Campos de Signos Vitales
        TextField txtPeso = new TextField(); txtPeso.setPromptText("kg");
        TextField txtTemp = new TextField(); txtTemp.setPromptText("°C");
        TextField txtPulso = new TextField(); txtPulso.setPromptText("bpm");
        TextField txtResp = new TextField(); txtResp.setPromptText("rpm");
        TextField txtPresion = new TextField(); txtPresion.setPromptText("120/80");
        TextField txtTalla = new TextField(); txtTalla.setPromptText("cm");

        int row = 0;
        grid.add(new Label("DATOS DEL PACIENTE"), 0, row++, 2, 1);
        grid.add(new Label("Nombres:"), 0, row); grid.add(txtNombre, 1, row++);
        grid.add(new Label("Apellidos:"), 0, row); grid.add(txtApellidos, 1, row++);
        grid.add(new Label("Tipo:"), 0, row); grid.add(comboTipo, 1, row++);
        grid.add(new Label("Sexo:"), 0, row); grid.add(comboSexo, 1, row++);
        grid.add(new Label("F. Nacimiento:"), 0, row); grid.add(txtFechaNac, 1, row++);
        grid.add(new Label("Dirección:"), 0, row); grid.add(txtDireccion, 1, row++);
        grid.add(new Label("Teléfono:"), 0, row); grid.add(txtTelefono, 1, row++);
        grid.add(new Label("Nacionalidad:"), 0, row); grid.add(txtNacionalidad, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(new Label("SIGNOS VITALES"), 0, row++, 2, 1);
        grid.add(new Label("Peso:"), 0, row); grid.add(txtPeso, 1, row++);
        grid.add(new Label("Temperatura:"), 0, row); grid.add(txtTemp, 1, row++);
        grid.add(new Label("Frec. Cardíaca:"), 0, row); grid.add(txtPulso, 1, row++);
        grid.add(new Label("Frec. Respiratoria:"), 0, row); grid.add(txtResp, 1, row++);
        grid.add(new Label("Presión Arterial:"), 0, row); grid.add(txtPresion, 1, row++);
        grid.add(new Label("Talla:"), 0, row); grid.add(txtTalla, 1, row++);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardar) {
                if (txtNombre.getText().isEmpty() || txtApellidos.getText().isEmpty()) return null;
                String nombreCompleto = txtNombre.getText().trim() + " " + txtApellidos.getText().trim();
                boolean esEst = "Estudiante".equals(comboTipo.getValue());
                Paciente p = new Paciente(0, nombreCompleto, comboTipo.getValue(), esEst, txtFechaNac.getText(), comboSexo.getValue(), txtDireccion.getText(), txtTelefono.getText(), txtNacionalidad.getText());
                com.clinica.aauca.model.SignosVitales sv = new com.clinica.aauca.model.SignosVitales(0, 0, java.time.LocalDate.now().toString(), txtPeso.getText(), txtTemp.getText(), txtPulso.getText(), txtResp.getText(), txtPresion.getText(), txtTalla.getText());
                return new javafx.util.Pair<>(p, sv);
            }
            return null;
        });

        java.util.Optional<javafx.util.Pair<Paciente, com.clinica.aauca.model.SignosVitales>> resultado = dialog.showAndWait();
        resultado.ifPresent(pair -> {
            try {
                dao.crearPaciente(pair.getKey());
                // Obtener el ID generado (esto es una limitación del DAO actual, deberíamos devolver el ID)
                // Pero por ahora, buscaremos el último paciente con ese nombre
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
            }
        });
    }
}
