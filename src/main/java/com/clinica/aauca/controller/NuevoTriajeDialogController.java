package com.clinica.aauca.controller;

// Importaciones de modelos y utilidades requeridas
import com.clinica.aauca.model.SignosVitales;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Controlador para la interfaz del diálogo de ingreso de nuevos signos vitales (Triaje).
 * Vincula los controles FXML y proporciona métodos para capturar y validar los datos.
 */
public class NuevoTriajeDialogController {

    // Campo de entrada de texto para el peso en kilogramos
    @FXML private TextField txtPeso;

    // Campo de entrada de texto para la talla o estatura en centímetros
    @FXML private TextField txtTalla;

    // Campo de entrada de texto para la temperatura corporal en grados Celsius
    @FXML private TextField txtTemperatura;

    // Campo de entrada de texto para el pulso o frecuencia cardíaca en latidos por minuto (bpm)
    @FXML private TextField txtPulso;

    // Campo de entrada de texto para la frecuencia respiratoria en respiraciones por minuto (rpm)
    @FXML private TextField txtResp;

    // Campo de entrada de texto para la presión arterial del paciente (ej. 120/80)
    @FXML private TextField txtPresion;

    /**
     * Método de inicialización automática llamado después de cargar el FXML.
     */
    @FXML
    public void initialize() {
        // En este diálogo los campos de entrada se inicializan vacíos por defecto.
    }

    /**
     * Método para construir el objeto del modelo SignosVitales a partir de los datos ingresados.
     * 
     * @param pacienteId El identificador único del paciente para asociar las constantes físicas.
     * @return Un objeto de tipo SignosVitales cargado con la información recolectada.
     */
    public SignosVitales getResultData(int pacienteId) {
        // Retorna la instancia del modelo mapeada con la fecha actual del sistema y los inputs limpios
        return new SignosVitales(
            0, // El ID se genera automáticamente en la base de datos (clave primaria autoincrementable)
            pacienteId, // Asociación al paciente correspondiente
            java.time.LocalDate.now().toString(), // Fecha del triaje (hoy)
            txtPeso.getText() == null ? "" : txtPeso.getText().trim(), // Captura de peso limpiando espacios
            txtTemperatura.getText() == null ? "" : txtTemperatura.getText().trim(), // Captura de temperatura corporal
            txtPulso.getText() == null ? "" : txtPulso.getText().trim(), // Captura de frecuencia cardíaca
            txtResp.getText() == null ? "" : txtResp.getText().trim(), // Captura de frecuencia respiratoria
            txtPresion.getText() == null ? "" : txtPresion.getText().trim(), // Captura de presión arterial
            txtTalla.getText() == null ? "" : txtTalla.getText().trim() // Captura de talla
        );
    }

    /**
     * Evalúa si el usuario ha ingresado algún texto en cualquiera de los campos de entrada.
     * Permite alertar si el usuario intenta cancelar la operación con cambios sin guardar.
     * 
     * @return true si al menos un campo tiene texto ingresado; de lo contrario, false.
     */
    public boolean hasChanges() {
        // Verifica si alguno de los campos de texto contiene información no vacía
        return (txtPeso.getText() != null && !txtPeso.getText().trim().isEmpty()) ||
               (txtTalla.getText() != null && !txtTalla.getText().trim().isEmpty()) ||
               (txtTemperatura.getText() != null && !txtTemperatura.getText().trim().isEmpty()) ||
               (txtPulso.getText() != null && !txtPulso.getText().trim().isEmpty()) ||
               (txtResp.getText() != null && !txtResp.getText().trim().isEmpty()) ||
               (txtPresion.getText() != null && !txtPresion.getText().trim().isEmpty());
    }
}
