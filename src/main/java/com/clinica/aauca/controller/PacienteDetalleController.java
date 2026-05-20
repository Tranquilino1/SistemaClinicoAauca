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
}
