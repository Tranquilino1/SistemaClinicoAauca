package com.clinica.aauca.controller;

// Importa la clase del modelo para almacenar signos vitales
import com.clinica.aauca.model.SignosVitales;
// Importa la anotación FXML de JavaFX para inyectar elementos de la vista
import javafx.fxml.FXML;
// Importa la clase de control de campo de texto de JavaFX
import javafx.scene.control.TextField;

/**
 * Controlador de JavaFX para gestionar el diálogo de ingreso de nuevos signos vitales.
 * Reemplaza al antiguo dialog controller de triaje.
 */
public class NuevoSignosVitalesDialogController {

    // Inyecta el campo de texto de FXML correspondiente al peso del paciente
    @FXML private TextField txtPeso;

    // Inyecta el campo de texto de FXML correspondiente a la talla o estatura del paciente
    @FXML private TextField txtTalla;

    // Inyecta el campo de texto de FXML correspondiente a la temperatura corporal del paciente
    @FXML private TextField txtTemperatura;

    // Inyecta el campo de texto de FXML correspondiente al pulso o frecuencia cardíaca del paciente
    @FXML private TextField txtPulso;

    // Inyecta el campo de texto de FXML correspondiente a la frecuencia respiratoria del paciente
    @FXML private TextField txtResp;

    // Inyecta el campo de texto de FXML correspondiente a la presión arterial (sistólica/diastólica)
    @FXML private TextField txtPresion;

    /**
     * Método de inicialización automática ejecutado tras la carga de la vista FXML.
     */
    @FXML
    public void initialize() {
        // Los campos de texto se inicializan vacíos por defecto en esta pantalla
    }

    /**
     * Mapea y recopila los datos del formulario en un objeto de tipo SignosVitales.
     *
     * @param pacienteId Identificador del paciente al cual se asocian las constantes vitales.
     * @return Objeto SignosVitales cargado con los valores del formulario.
     */
    public SignosVitales getResultData(int pacienteId) {
        // Retorna la nueva instancia construida con el ID del paciente, la fecha actual y los textos recortados
        return new SignosVitales(
            0, // El ID de los signos es 0 dado que es autoincremental en la base de datos
            pacienteId, // Vincula el ID del paciente recibido
            java.time.LocalDate.now().toString(), // Almacena la fecha actual del sistema en formato string YYYY-MM-DD
            txtPeso.getText() == null ? "" : txtPeso.getText().trim(), // Procesa el valor del peso removiendo espacios
            txtTemperatura.getText() == null ? "" : txtTemperatura.getText().trim(), // Procesa la temperatura corporal
            txtPulso.getText() == null ? "" : txtPulso.getText().trim(), // Procesa la frecuencia cardíaca
            txtResp.getText() == null ? "" : txtResp.getText().trim(), // Procesa la frecuencia respiratoria
            txtPresion.getText() == null ? "" : txtPresion.getText().trim(), // Procesa la presión arterial
            txtTalla.getText() == null ? "" : txtTalla.getText().trim() // Procesa la talla o estatura
        );
    }

    /**
     * Comprueba si el usuario ha realizado cambios en alguno de los campos de texto del formulario.
     * Sirve para alertar sobre posibles pérdidas de datos al intentar cerrar la ventana sin guardar.
     *
     * @return true si al menos un campo tiene texto ingresado, de lo contrario false.
     */
    public boolean hasChanges() {
        // Retorna verdadero si cualquiera de los campos contiene información no nula y no vacía
        return (txtPeso.getText() != null && !txtPeso.getText().trim().isEmpty()) ||
               (txtTalla.getText() != null && !txtTalla.getText().trim().isEmpty()) ||
               (txtTemperatura.getText() != null && !txtTemperatura.getText().trim().isEmpty()) ||
               (txtPulso.getText() != null && !txtPulso.getText().trim().isEmpty()) ||
               (txtResp.getText() != null && !txtResp.getText().trim().isEmpty()) ||
               (txtPresion.getText() != null && !txtPresion.getText().trim().isEmpty());
    }
}
