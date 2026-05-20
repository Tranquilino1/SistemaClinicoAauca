package com.clinica.aauca.controller;

import com.clinica.aauca.dao.UserDAOImpl;
import com.clinica.aauca.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;

public class ConfiguracionController {

    // Tab Clínica
    @FXML private TextField txtNombreClinica;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtLogoPath;

    // Tab Precios
    @FXML private TextField txtPrecioConsulta;
    @FXML private TextField txtPrecioHosp;

    // Tab Seguridad
    @FXML private PasswordField txtPassActual;
    @FXML private PasswordField txtPassNueva;
    @FXML private PasswordField txtPassConfirmar;

    // Tab Mi Perfil
    @FXML private TextField txtPerfilUsername;
    @FXML private TextField txtPerfilNombre;
    @FXML private TextField txtPerfilRol;

    private User currentUser;
    private DashboardController dashboardController;
    private UserDAOImpl userDAO = new UserDAOImpl();
    private com.clinica.aauca.dao.ConfiguracionDAO configDAO = new com.clinica.aauca.dao.ConfiguracionDAO();

    public void setDashboardController(DashboardController dash) {
        this.dashboardController = dash;
    }

    @FXML
    public void initialize() {
        // Autoguardado en tiempo real para el perfil
        txtPerfilUsername.textProperty().addListener((obs, old, val) -> autoGuardarPerfil());
        txtPerfilNombre.textProperty().addListener((obs, old, val) -> autoGuardarPerfil());

        cargarConfiguracionGlobal();
    }

    private void cargarConfiguracionGlobal() {
        com.clinica.aauca.model.Configuracion config = configDAO.obtenerConfiguracion();
        if (config != null) {
            txtNombreClinica.setText(config.getNombreClinica());
            txtDireccion.setText(config.getDireccion());
            txtTelefono.setText(config.getTelefono());
            txtEmail.setText(config.getEmail());
            txtLogoPath.setText(config.getLogoPath());
            txtPrecioConsulta.setText(String.valueOf(config.getPrecioConsulta()));
            txtPrecioHosp.setText(String.valueOf(config.getPrecioHospitalizacion()));
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            txtPerfilUsername.setText(user.getUsername());
            txtPerfilNombre.setText(user.getFullName());
            txtPerfilRol.setText(user.getRole());
        }
    }

    @FXML
    public void guardarConfiguracion() {
        if (currentUser == null || !"Admin".equals(currentUser.getRole())) {
            mostrarAlerta("Acceso Denegado", "Solo Admin", "Solo el administrador puede cambiar datos globales.", Alert.AlertType.ERROR);
            return;
        }
        
        com.clinica.aauca.model.Configuracion config = configDAO.obtenerConfiguracion();
        if (config == null) config = new com.clinica.aauca.model.Configuracion();
        
        config.setNombreClinica(txtNombreClinica.getText().trim());
        config.setDireccion(txtDireccion.getText().trim());
        config.setTelefono(txtTelefono.getText().trim());
        config.setEmail(txtEmail.getText().trim());
        config.setLogoPath(txtLogoPath.getText().trim());
        
        configDAO.guardarConfiguracion(config);
        mostrarAlerta("✅ Éxito", "Configuración Guardada", "Los datos de la clínica han sido actualizados en la base de datos.", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void guardarPrecios() {
        if (currentUser == null || !"Admin".equals(currentUser.getRole())) {
            mostrarAlerta("Acceso Denegado", "Solo Admin", "Solo el administrador puede cambiar precios.", Alert.AlertType.ERROR);
            return;
        }

        try {
            double pCons = Double.parseDouble(txtPrecioConsulta.getText().trim());
            double pHosp = Double.parseDouble(txtPrecioHosp.getText().trim());
            
            com.clinica.aauca.model.Configuracion config = configDAO.obtenerConfiguracion();
            if (config == null) config = new com.clinica.aauca.model.Configuracion();
            
            config.setPrecioConsulta(pCons);
            config.setPrecioHospitalizacion(pHosp);
            
            configDAO.guardarConfiguracion(config);
            mostrarAlerta("✅ Éxito", "Tarifas Actualizadas", "Los nuevos precios han sido guardados.", Alert.AlertType.INFORMATION);
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Valor Inválido", "Asegúrese de ingresar números válidos para los precios.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void seleccionarLogo() {
       javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
       fileChooser.setTitle("Seleccionar Logo");
       fileChooser.getExtensionFilters().addAll(
           new javafx.stage.FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
       );
       java.io.File file = fileChooser.showOpenDialog(txtLogoPath.getScene().getWindow());
       if (file != null) {
           txtLogoPath.setText(file.getAbsolutePath());
       }
    }

    private void autoGuardarPerfil() {
        if (currentUser == null || txtPerfilUsername.getText().trim().isEmpty() || txtPerfilNombre.getText().trim().isEmpty()) {
            return;
        }

        String nuevoUser = txtPerfilUsername.getText().trim();
        String nuevoNombre = txtPerfilNombre.getText().trim();

        // Actualizar objeto y base de datos
        currentUser.setUsername(nuevoUser);
        currentUser.setFullName(nuevoNombre);
        userDAO.actualizar(currentUser);

        // Refrescar UI global si el dashboard está disponible
        if (dashboardController != null) {
            dashboardController.refrescarSesionUI();
        }
    }

    @FXML
    public void guardarPerfil() {
        // El guardado es automático ahora, este método queda para compatibilidad o puede eliminarse
        mostrarAlerta("✨ Info", "Autoguardado", "Tus cambios se guardan automáticamente mientras escribes.", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void cambiarPassword() {
        if (currentUser == null) {
            mostrarAlerta("Error", "Sin sesión activa", "No hay usuario activo en el sistema.", Alert.AlertType.ERROR);
            return;
        }
        String actual = txtPassActual.getText();
        String nueva = txtPassNueva.getText();
        String confirmar = txtPassConfirmar.getText();

        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            mostrarAlerta("Atención", "Campos Requeridos", "Complete todos los campos de contraseña.", Alert.AlertType.WARNING);
            return;
        }
        if (!nueva.equals(confirmar)) {
            mostrarAlerta("Error", "Contraseñas no coinciden", "La nueva contraseña y la confirmación son distintas.", Alert.AlertType.ERROR);
            return;
        }
        if (!BCrypt.checkpw(actual, currentUser.getPassword())) {
            mostrarAlerta("Error", "Contraseña Incorrecta", "La contraseña actual no es correcta.", Alert.AlertType.ERROR);
            return;
        }
        if (nueva.length() < 6) {
            mostrarAlerta("Error", "Contraseña Muy Corta", "La nueva contraseña debe tener al menos 6 caracteres.", Alert.AlertType.ERROR);
            return;
        }

        String nuevoHash = BCrypt.hashpw(nueva, BCrypt.gensalt(12));
        currentUser.setPassword(nuevoHash);
        userDAO.actualizarPassword(currentUser.getId(), nuevoHash);

        txtPassActual.clear(); txtPassNueva.clear(); txtPassConfirmar.clear();
        mostrarAlerta("✅ Éxito", "Contraseña Actualizada", "Su contraseña ha sido cambiada exitosamente.", Alert.AlertType.INFORMATION);
    }

    private void mostrarAlerta(String t, String h, String c, Alert.AlertType tipo) {
        Alert a = new Alert(tipo); a.setTitle(t); a.setHeaderText(h); a.setContentText(c); a.showAndWait();
    }
}
