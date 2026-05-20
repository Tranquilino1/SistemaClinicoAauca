package com.clinica.aauca.controller;

import com.clinica.aauca.dao.MedicamentoDAO;
import com.clinica.aauca.model.Medicamento;
import com.clinica.aauca.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class MedicamentosController {
    @FXML private TableView<Medicamento> tablaMedicamentos;
    @FXML private TableColumn<Medicamento, Integer> colId;
    @FXML private TableColumn<Medicamento, String> colNombre;
    @FXML private TableColumn<Medicamento, Integer> colStock;
    @FXML private TableColumn<Medicamento, Double> colPrecio;
    @FXML private TableColumn<Medicamento, String> colEstado;
    @FXML private TextField txtNombre;
    @FXML private TextField txtStock;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtBuscarMed;
    @FXML private Button btnGuardar;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;

    private MedicamentoDAO dao = new MedicamentoDAO();
    private ObservableList<Medicamento> lista;
    private User currentUser;
    private Medicamento medicamentoSeleccionado;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoStock"));
        cargarDatos();

        tablaMedicamentos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                medicamentoSeleccionado = newSel;
                txtNombre.setText(newSel.getNombre());
                txtStock.setText(String.valueOf(newSel.getStock()));
                txtPrecio.setText(String.valueOf(newSel.getPrecio()));

                // Admin y Enfermero siempre pueden editar, Médico solo visualización
                boolean esAdmin = currentUser != null && "Admin".equals(currentUser.getRole());
                boolean esEnfermero = currentUser != null && "Enfermero".equals(currentUser.getRole());
                boolean esCreador = currentUser != null && currentUser.getId() == newSel.getCreadorId();
                
                btnEditar.setDisable(!(esAdmin || esEnfermero || esCreador));
                btnEliminar.setDisable(!(esAdmin || esEnfermero || esCreador));
            }
        });
    }

    private void cargarDatos() {
        lista = FXCollections.observableArrayList(dao.obtenerTodos());
        tablaMedicamentos.setItems(lista);
    }

    @FXML
    private void buscarMedicamento() {
        String query = txtBuscarMed.getText().trim();
        if (query.isEmpty()) {
            cargarDatos();
            return;
        }
        lista = FXCollections.observableArrayList(dao.buscarPorNombre(query));
        tablaMedicamentos.setItems(lista);
    }

    @FXML
    private void verTodos() {
        txtBuscarMed.clear();
        cargarDatos();
    }

    @FXML
    private void guardarMedicamento() {
        if (txtNombre.getText().trim().isEmpty() || txtStock.getText().trim().isEmpty() || txtPrecio.getText().trim().isEmpty()) {
            mostrarAlerta("Atención", "Campos Incompletos", "Nombre, Stock y Precio son obligatorios.", Alert.AlertType.WARNING);
            return;
        }
        if (currentUser == null) {
            mostrarAlerta("Error", "Sin Sesión", "No hay usuario activo.", Alert.AlertType.ERROR);
            return;
        }
        try {
            int stock = Integer.parseInt(txtStock.getText().trim());
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            if (stock < 0 || precio < 0) throw new NumberFormatException();
            
            Medicamento m = new Medicamento(0, txtNombre.getText().trim(), stock, precio, currentUser.getId());
            dao.crear(m);
            limpiarFormulario();
            cargarDatos();
            mostrarAlerta("Éxito", "Medicamento Registrado", "El medicamento ha sido añadido.", Alert.AlertType.INFORMATION);
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Valores Inválidos", "Stock debe ser entero y Precio debe ser numérico (ej: 1500.5).", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void editarMedicamento() {
        if (medicamentoSeleccionado == null) return;
        try {
            int stock = Integer.parseInt(txtStock.getText().trim());
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            medicamentoSeleccionado.setNombre(txtNombre.getText().trim());
            medicamentoSeleccionado.setStock(stock);
            medicamentoSeleccionado.setPrecio(precio);
            
            dao.actualizar(medicamentoSeleccionado);
            limpiarFormulario();
            cargarDatos();
            mostrarAlerta("Éxito", "Cambios Guardados", "El medicamento ha sido actualizado.", Alert.AlertType.INFORMATION);
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Valores Inválidos", "Revise stock y precio.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarMedicamento() {
        if (medicamentoSeleccionado == null) {
            mostrarAlerta("Atención", "Sin Selección", "Seleccione un medicamento de la tabla para eliminar.", Alert.AlertType.WARNING);
            return;
        }
        boolean esAdmin = currentUser != null && "Admin".equals(currentUser.getRole());
        boolean esCreador = currentUser != null && currentUser.getId() == medicamentoSeleccionado.getCreadorId();

        if (!esAdmin && !esCreador) {
            mostrarAlerta("Acceso Denegado", "No Autorizado", "Solo el administrador o el usuario que registró este medicamento puede eliminarlo.", Alert.AlertType.ERROR);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminación");
        confirm.setHeaderText("¿Eliminar '" + medicamentoSeleccionado.getNombre() + "'?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                dao.eliminar(medicamentoSeleccionado.getId());
                limpiarFormulario();
                cargarDatos();
                mostrarAlerta("Éxito", "Eliminado", "El medicamento ha sido eliminado correctamente.", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtStock.clear();
        medicamentoSeleccionado = null;
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);
        tablaMedicamentos.getSelectionModel().clearSelection();
    }

    @FXML
    public void exportarStockPDF() {
        if (tablaMedicamentos.getItems().isEmpty()) {
            mostrarAlerta("Atención", "Sin Datos", "No hay medicamentos registrados para exportar.", Alert.AlertType.WARNING);
            return;
        }

        try {
            java.util.List<String[]> datos = new java.util.ArrayList<>();
            datos.add(new String[]{"ID", "Nombre Medicamento", "Stock", "Precio (FCFA)", "Estado"});

            for (Medicamento m : tablaMedicamentos.getItems()) {
                datos.add(new String[]{
                    String.valueOf(m.getId()),
                    m.getNombre(),
                    String.valueOf(m.getStock()),
                    String.valueOf(m.getPrecio()),
                    m.getEstadoStock()
                });
            }

            com.clinica.aauca.util.PDFService.generarDocumento("Inventario_Medicamentos", 
                "Inventario de Farmacia", "Estado de Existencias de Medicamentos", datos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String cabecera, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecera);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
