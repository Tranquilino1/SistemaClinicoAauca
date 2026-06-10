package com.clinica.aauca.controller;

import com.clinica.aauca.model.Paciente;
import com.clinica.aauca.model.SignosVitales;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.util.Pair;
import java.time.format.DateTimeFormatter;

public class NuevoPacienteDialogController {

    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private ComboBox<String> comboTipo;
    @FXML private ComboBox<String> comboSexo;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtNacionalidad;

    @FXML private TextField txtPeso;
    @FXML private TextField txtTalla;
    @FXML private TextField txtTemperatura;
    @FXML private TextField txtPulso;
    @FXML private TextField txtResp;
    @FXML private TextField txtPresion;

    @FXML
    public void initialize() {
        comboTipo.setItems(FXCollections.observableArrayList("Estudiante", "Docente", "Administrativo", "Particular"));
        comboSexo.setItems(FXCollections.observableArrayList("Masculino", "Femenino", "Otro"));
        comboSexo.setValue("Masculino");
    }

    public boolean isInputValid() {
        if (txtNombres.getText() == null || txtNombres.getText().trim().isEmpty() ||
            txtApellidos.getText() == null || txtApellidos.getText().trim().isEmpty()) {
            return false;
        }
        return true;
    }

    public Pair<Paciente, SignosVitales> getResultData() {
        if (!isInputValid()) {
            return null;
        }
        
        String nombreCompleto = txtNombres.getText().trim() + " " + txtApellidos.getText().trim();
        boolean esEst = "Estudiante".equals(comboTipo.getValue());
        
        String fechaNacStr = "";
        if (dpFechaNacimiento.getValue() != null) {
            fechaNacStr = dpFechaNacimiento.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        Paciente p = new Paciente(0, nombreCompleto, comboTipo.getValue(), esEst, 
            fechaNacStr, comboSexo.getValue(), 
            txtDireccion.getText() == null ? "" : txtDireccion.getText(), 
            txtTelefono.getText() == null ? "" : txtTelefono.getText(), 
            txtNacionalidad.getText() == null ? "" : txtNacionalidad.getText());
            
        SignosVitales sv = new SignosVitales(0, 0, 
            java.time.LocalDate.now().toString(), 
            txtPeso.getText() == null ? "" : txtPeso.getText(), 
            txtTemperatura.getText() == null ? "" : txtTemperatura.getText(), 
            txtPulso.getText() == null ? "" : txtPulso.getText(), 
            txtResp.getText() == null ? "" : txtResp.getText(), 
            txtPresion.getText() == null ? "" : txtPresion.getText(), 
            txtTalla.getText() == null ? "" : txtTalla.getText());
            
        return new Pair<>(p, sv);
    }

    public boolean hasChanges() {
        return (txtNombres.getText() != null && !txtNombres.getText().trim().isEmpty()) ||
               (txtApellidos.getText() != null && !txtApellidos.getText().trim().isEmpty()) ||
               (txtDireccion.getText() != null && !txtDireccion.getText().trim().isEmpty()) ||
               (txtTelefono.getText() != null && !txtTelefono.getText().trim().isEmpty()) ||
               (txtNacionalidad.getText() != null && !txtNacionalidad.getText().trim().isEmpty()) ||
               (dpFechaNacimiento.getValue() != null) ||
               (txtPeso.getText() != null && !txtPeso.getText().trim().isEmpty()) ||
               (txtTalla.getText() != null && !txtTalla.getText().trim().isEmpty()) ||
               (txtTemperatura.getText() != null && !txtTemperatura.getText().trim().isEmpty()) ||
               (txtPulso.getText() != null && !txtPulso.getText().trim().isEmpty()) ||
               (txtResp.getText() != null && !txtResp.getText().trim().isEmpty()) ||
               (txtPresion.getText() != null && !txtPresion.getText().trim().isEmpty());
    }
}
