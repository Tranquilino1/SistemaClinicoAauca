package com.clinica.aauca.controller;

import com.clinica.aauca.dao.UserDAOImpl;
import com.clinica.aauca.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;

public class UsuariosController {

    @FXML private TextField txtBuscarUsuario;
    @FXML private TableView<User> tablaUsuarios;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUserUsername;
    @FXML private TableColumn<User, String> colUserNombre;
    @FXML private TableColumn<User, String> colUserRol;

    @FXML private TextField txtUsername;
    @FXML private TextField txtNombreCompleto;
    @FXML private ComboBox<String> cmbRol;
    @FXML private PasswordField txtNewPass;

    private UserDAOImpl dao = new UserDAOImpl();
    private User usuarioSeleccionado;

    @FXML
    public void initialize() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUserUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUserNombre.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUserRol.setCellValueFactory(new PropertyValueFactory<>("role"));

        cmbRol.setItems(FXCollections.observableArrayList("Admin", "Médico", "Enfermero"));
        cargarTodos();

        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                usuarioSeleccionado = sel;
                txtUsername.setText(sel.getUsername());
                txtNombreCompleto.setText(sel.getFullName());
                cmbRol.setValue(sel.getRole());
                txtNewPass.clear();
            }
        });
    }

    private void cargarTodos() {
        List<User> usuarios = dao.obtenerTodos();
        tablaUsuarios.setItems(FXCollections.observableArrayList(usuarios));
    }

    @FXML
    public void verTodos() {
        txtBuscarUsuario.clear();
        cargarTodos();
    }

    @FXML
    public void buscarUsuario() {
        String query = txtBuscarUsuario.getText().trim().toLowerCase();
        if (query.isEmpty()) { cargarTodos(); return; }
        List<User> todos = dao.obtenerTodos();
        ObservableList<User> filtrado = FXCollections.observableArrayList();
        for (User u : todos) {
            if (u.getUsername().toLowerCase().contains(query) || u.getFullName().toLowerCase().contains(query)) {
                filtrado.add(u);
            }
        }
        tablaUsuarios.setItems(filtrado);
    }

    @FXML
    public void nuevoUsuario() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Usuario");
        dialog.setHeaderText("Registrar Nuevo Usuario del Sistema");

        ButtonType btnGuardar = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField fUsername = new TextField();
        TextField fNombre = new TextField();
        PasswordField fPass = new PasswordField();
        ComboBox<String> fRol = new ComboBox<>(FXCollections.observableArrayList("Admin", "Médico", "Enfermero"));
        fRol.setValue("Médico");

        grid.add(new Label("Username:"), 0, 0); grid.add(fUsername, 1, 0);
        grid.add(new Label("Nombre Completo:"), 0, 1); grid.add(fNombre, 1, 1);
        grid.add(new Label("Contraseña:"), 0, 2); grid.add(fPass, 1, 2);
        grid.add(new Label("Rol:"), 0, 3); grid.add(fRol, 1, 3);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                String hash = BCrypt.hashpw(fPass.getText(), BCrypt.gensalt(12));
                return new User(0, fUsername.getText().trim(), hash, fNombre.getText().trim(), fRol.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newUser -> {
            if (newUser.getUsername().isEmpty() || newUser.getFullName().isEmpty()) {
                mostrarAlerta("Error", "Datos Incompletos", "El nombre y usuario son obligatorios.", Alert.AlertType.ERROR);
                return;
            }
            try {
                dao.crear(newUser);
                mostrarAlerta("Éxito", "Usuario Creado", "El usuario " + newUser.getUsername() + " ha sido registrado.", Alert.AlertType.INFORMATION);
                cargarTodos();
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo crear", "Es posible que el nombre de usuario ya exista.", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    public void actualizarUsuario() {
        if (usuarioSeleccionado == null) {
            mostrarAlerta("Atención", "Sin selección", "Seleccione un usuario de la tabla.", Alert.AlertType.WARNING);
            return;
        }
        usuarioSeleccionado.setUsername(txtUsername.getText().trim());
        usuarioSeleccionado.setFullName(txtNombreCompleto.getText().trim());
        usuarioSeleccionado.setRole(cmbRol.getValue());

        if (!txtNewPass.getText().isEmpty()) {
            String hash = BCrypt.hashpw(txtNewPass.getText(), BCrypt.gensalt(12));
            usuarioSeleccionado.setPassword(hash);
        }

        try {
            dao.actualizar(usuarioSeleccionado);
            mostrarAlerta("Éxito", "Usuario Actualizado", "Los datos han sido guardados correctamente.", Alert.AlertType.INFORMATION);
            cargarTodos();
        } catch (Exception e) {
            mostrarAlerta("Error", "Fallo al actualizar", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void eliminarUsuario() {
        if (usuarioSeleccionado == null) {
            mostrarAlerta("Atención", "Sin selección", "Seleccione un usuario de la tabla para eliminar.", Alert.AlertType.WARNING);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminación");
        confirm.setHeaderText("¿Eliminar al usuario '" + usuarioSeleccionado.getUsername() + "'?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                dao.eliminar(usuarioSeleccionado.getId());
                usuarioSeleccionado = null;
                cargarTodos();
                mostrarAlerta("Éxito", "Usuario Eliminado", "El usuario ha sido eliminado del sistema.", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void mostrarAlerta(String t, String h, String c, Alert.AlertType tipo) {
        Alert a = new Alert(tipo); a.setTitle(t); a.setHeaderText(h); a.setContentText(c); a.showAndWait();
    }
}
