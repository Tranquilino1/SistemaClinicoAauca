package com.clinica.aauca.controller;

import com.clinica.aauca.dao.UserDAO;
import com.clinica.aauca.dao.UserDAOImpl;
import com.clinica.aauca.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import java.util.Optional;

/**
 * Controlador para la vista de Inicio de Sesión.
 */
/**
 * Controlador de la pantalla de acceso (Login).
 * Se encarga de validar credenciales y redirigir al Dashboard principal.
 */
public class LoginController {

    @FXML private TextField txtUsername;    // Campo de texto para nombre de usuario
    @FXML private PasswordField txtPassword; // Campo oculto para contraseña
    @FXML private Label lblError;           // Etiqueta para mostrar mensajes de error
    @FXML private Button btnLogin;          // Botón de acción principal

    // DAO para la comunicación con la tabla de usuarios
    private final UserDAO userDAO = new UserDAOImpl();

    @FXML
    public void initialize() {
        // Al iniciar, el mensaje de error debe estar oculto
        lblError.setVisible(false);
    }

    /**
     * Proceso de autenticación al hacer clic en "Entrar".
     */
    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        // Validación básica de campos vacíos
        if (username.isEmpty() || password.isEmpty()) {
            showError("Por favor, complete todos los campos.");
            return;
        }

        // UX: Deshabilitamos el botón para evitar clics múltiples durante la verificación
        btnLogin.setDisable(true);
        lblError.setVisible(false);
        
        // Ejecución asíncrona: No bloqueamos la interfaz mientras consultamos la base de datos
        new Thread(() -> {
            try {
                // Se llama al método login del DAO, que usa BCrypt internamente
                Optional<User> userOpt = userDAO.login(username, password);
                
                // Volvemos al hilo principal de JavaFX para actualizar la UI
                Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    if (userOpt.isPresent()) {
                        onLoginSuccess(userOpt.get()); // Paso a la siguiente pantalla
                    } else {
                        showError("Usuario o contraseña incorrectos.");
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    showError(ex.getMessage());
                });
            }
        }).start();
    }

    /**
     * Acción a realizar cuando las credenciales son válidas.
     * Carga el Dashboard y transfiere el objeto Usuario para persistir la sesión.
     */
    private void onLoginSuccess(User user) {
        Platform.runLater(() -> {
            try {
                // Carga dinámica del archivo FXML del Dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/clinica/aauca/view/dashboard.fxml"));
                javafx.scene.Parent root = loader.load();
                
                // IMPORTANTE: Obtenemos el controlador del Dashboard para inyectar el usuario logueado
                DashboardController dashboardController = loader.getController();
                dashboardController.setUser(user);
                
                // Obtenemos la ventana actual y cambiamos el root de la escena activa para evitar parpadeos
                javafx.stage.Stage stage = (javafx.stage.Stage) btnLogin.getScene().getWindow();
                stage.setMinWidth(1024);
                stage.setMinHeight(700);
                stage.getScene().setRoot(root);
                if (!stage.isMaximized()) {
                    stage.setMaximized(true);
                }
                
            } catch (java.io.IOException e) {
                showError("Error al cargar el dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Muestra mensajes de error visuales en la pantalla de login.
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            lblError.setText(message);
            lblError.setVisible(true);
        });
    }
}
