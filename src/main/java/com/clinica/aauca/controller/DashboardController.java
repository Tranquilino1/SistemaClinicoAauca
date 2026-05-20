package com.clinica.aauca.controller;

import com.clinica.aauca.model.User;
import com.clinica.aauca.util.DatabaseConnector;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Separator;
import javafx.util.Duration;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

/**
 * ===================================================================
 * CONTROL DE ACCESO POR ROL — Clínica AAUCA
 * ===================================================================
 *
 *  ROL ADMIN
 *  - Gestión de Usuarios (crear, editar, eliminar, cambiar contraseña)
 *  - Reportes Clínicos (estadísticas globales, top pacientes)
 *  - Pagos y Facturas FCFA (todos los registros)
 *  - Configuración del sistema
 *  - Acceso de LECTURA a todos los módulos clínicos
 *
 *  ROL MÉDICO
 *  - Pacientes: ver, crear, editar y buscar pacientes
 *  - Consultas: registrar diagnóstico, receta, facturación FCFA
 *  - Historial Médico: ver consultas y hospitalizaciones del paciente
 *  - Hospitalización: ver ingresos (solo lectura)
 *  - Medicamentos: consultar inventario (solo lectura)
 *  - Cambio de su propia contraseña
 *
 *  ROL ENFERMERO
 *  - Hospitalización: crear, actualizar y eliminar ingresos
 *  - Medicamentos: crear, editar y eliminar stock
 *  - Pacientes: ver lista (solo lectura)
 *  - Historial: ver historial de un paciente
 *  - Cambio de su propia contraseña
 *
 * =======================================================/**
 * Controlador principal del Dashboard de la Clínica AAUCA.
 * Gestiona la navegación, el control de acceso por roles y la carga dinámica de módulos.
 */
public class DashboardController {

    // --- Elementos de la Interfaz (FXML) ---
    @FXML private Label lblWelcome;         // Etiqueta de bienvenida (Nombre del usuario)
    @FXML private Label lblRole;            // Etiqueta que muestra el rol actual
    @FXML private VBox menuContainer;       // Contenedor lateral para los botones del menú
    @FXML private Label lblStat1;           // Tarjeta de estadística 1 (Dinámica por rol)
    @FXML private Label lblStat2;           // Tarjeta de estadística 2 (Dinámica por rol)
    @FXML private Label lblStatUsuarios;    // Contador total de usuarios
    @FXML private Label lblStatPacientes;   // Contador total de pacientes
    @FXML private Label lblHora;            // Reloj en tiempo real
    @FXML private ImageView logoView;       // Imagen del logo circular
    @FXML private StackPane workingArea;    // Área central donde se cargan las diferentes vistas

    private static DashboardController instancia; // Singleton para acceso global
    private User currentUser;                     // Usuario que ha iniciado sesión

    /**
     * Obtiene la instancia actual del controlador (Patrón Singleton).
     */
    public static DashboardController getInstancia() { return instancia; }

    /**
     * Inicializa el controlador al cargar la vista.
     * Configura el logo circular y el reloj en tiempo real.
     */
    @FXML
    public void initialize() {
        instancia = this; 
        
        // Efecto circular para el logo de la clínica
        if (logoView != null) {
            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(40, 40, 40);
            logoView.setClip(clip);
        }

        // Configuración de un Timeline para actualizar el reloj cada segundo
        if (lblHora != null) {
            Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                lblHora.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }));
            clock.setCycleCount(Animation.INDEFINITE);
            clock.play();
        }
    }

    /**
     * Define el usuario actual de la sesión y configura la interfaz según su rol.
     * @param user Objeto usuario obtenido tras el login.
     */
    public void setUser(User user) {
        this.currentUser = user;
        lblRole.setText(user.getRole());
        lblWelcome.setText(user.getFullName());
        actualizarEstadisticasDashboard();      // Carga números reales de la BD
        setupDynamicMenu(user.getRole());        // Crea los botones laterales según permisos
        mostrarDashboard();                     // Carga la pantalla de inicio por defecto
    }

    public User getCurrentUser() { return currentUser; }

    /**
     * Refresca las etiquetas de la sesión si el usuario cambia sus datos en configuración.
     */
    public void refrescarSesionUI() {
        if (currentUser != null) {
            lblRole.setText(currentUser.getRole());
            lblWelcome.setText(currentUser.getFullName());
        }
    }

    /**
     * Consulta la base de datos para actualizar las tarjetas de estadísticas del dashboard.
     */
    private void actualizarEstadisticasDashboard() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String totalPacientes = contar(conn, "SELECT COUNT(*) FROM pacientes");
            String totalUsuarios  = contar(conn, "SELECT COUNT(*) FROM usuarios");

            if (lblStatPacientes != null) lblStatPacientes.setText(totalPacientes);
            if (lblStatUsuarios  != null) lblStatUsuarios.setText(totalUsuarios);

            // Estadísticas diferenciadas por rol
            switch (currentUser.getRole()) {
                case "Admin":
                    lblStat1.setText("Consultas: " + contar(conn, "SELECT COUNT(*) FROM consultas"));
                    lblStat2.setText("Med. bajos: " + contar(conn, "SELECT COUNT(*) FROM medicamentos WHERE stock <= 5"));
                    break;
                case "Médico":
                    lblStat1.setText("Hoy: " + contar(conn, "SELECT COUNT(*) FROM consultas WHERE fecha = date('now')") + " consultas");
                    lblStat2.setText(contar(conn, "SELECT COUNT(*) FROM hospitalizaciones") + " ingresos");
                    break;
                case "Enfermero":
                    lblStat1.setText(contar(conn, "SELECT COUNT(*) FROM hospitalizaciones") + " ingresos");
                    lblStat2.setText(contar(conn, "SELECT COUNT(*) FROM medicamentos WHERE stock <= 5") + " med. bajos");
                    break;
            }
        } catch (Exception e) {
            lblStat1.setText("Sistema activo");
            lblStat2.setText("OK");
        }
    }

    // --- Métodos de respuesta a eventos de clic directo en el Dashboard ---
    @FXML public void abrirConfiguracion(MouseEvent event) { cargarModulo("Configuración", "configuracion_view.fxml"); }
    @FXML public void clickActividad(MouseEvent event) { cargarModulo("CONSULTAS", "consulta_form.fxml"); }
    @FXML public void clickUsuarios(MouseEvent event) { 
        if ("Admin".equals(currentUser.getRole())) cargarModulo("USUARIOS", "usuarios_view.fxml"); 
    }
    @FXML public void clickPacientes(MouseEvent event) { cargarModulo("PACIENTES", "pacientes_view.fxml"); }
    @FXML public void clickIngresos(MouseEvent event) { 
        if ("Admin".equals(currentUser.getRole())) cargarModulo("PAGOS", "pagos_view.fxml"); 
    }

    /**
     * Función auxiliar para ejecutar consultas de conteo en la base de datos.
     */
    private String contar(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? String.valueOf(rs.getInt(1)) : "0";
        }
    }

    /**
     * Construye dinámicamente el menú lateral basado en el rol del usuario.
     * @param role El rol del usuario (Admin, Médico, Enfermero).
     */
    private void setupDynamicMenu(String role) {
        menuContainer.getChildren().clear();

        addMenuItem("🏠 INICIO", "HOME");
        addSeparator();

        switch (role) {
            case "Admin":
                addSectionHeader("GESTIÓN CLÍNICA");
                addMenuItem("👥 PACIENTES", "USERS");
                addMenuItem("🩺 CONSULTAS", "STETHOSCOPE");
                addMenuItem("🏥 HOSPITALIZACIÓN", "BED");
                addMenuItem("💊 MEDICAMENTOS", "MEDKIT");
                addMenuItem("📖 HISTORIAL", "HEARTBEAT");
                addSeparator();
                addMenuItem("👤 GESTIÓN USUARIOS", "USER");
                addMenuItem("📊 REPORTES CLÍNICOS", "FILE_TEXT");
                addMenuItem("💰 PAGOS (FCFA)", "MONEY");
                addSeparator();
                addMenuItem("⚙ CONFIGURACIÓN", "GEARS");
                break;

            case "Médico":
                addSectionHeader("ATENCIÓN AL PACIENTE");
                addMenuItem("👥 MIS PACIENTES", "USERS");
                addMenuItem("🩺 CONSULTA", "STETHOSCOPE");
                addMenuItem("📖 HISTORIAL MÉDICO", "HEARTBEAT");
                addSeparator();
                addSectionHeader("APOYO CLÍNICO");
                addMenuItem("🏥 VER HOSPITALIZACIONES", "BED");
                addMenuItem("💊 VER MEDICAMENTOS", "MEDKIT");
                break;

            case "Enfermero":
                addSectionHeader("CUIDADOS Y STOCK");
                addMenuItem("🏥 HOSPITALIZACIÓN", "BED");
                addMenuItem("💊 MEDICAMENTOS", "MEDKIT");
                addSeparator();
                addSectionHeader("CONSULTA");
                addMenuItem("👥 VER PACIENTES", "USERS");
                addMenuItem("📖 HISTORIAL", "HEARTBEAT");
                break;
        }

        addSeparator();
        addMenuItem("🚪 CERRAR SESIÓN", "SIGN_OUT");
    }

    private void addSectionHeader(String text) {
        Label header = new Label(text);
        header.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 8 15 2 15; -fx-letter-spacing: 1;");
        menuContainer.getChildren().add(header);
    }

    private void addSeparator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.2; -fx-padding: 4 10;");
        menuContainer.getChildren().add(sep);
    }

    /**
     * Crea un elemento de menú con ícono y etiqueta.
     * @param text Texto a mostrar.
     * @param iconName Nombre del ícono FontAwesome.
     */
    private void addMenuItem(String text, String iconName) {
        HBox item = new HBox(12);
        item.getStyleClass().add("menu-item");
        item.setCursor(javafx.scene.Cursor.HAND);

        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setGlyphName(iconName);
        icon.setSize("16");
        icon.getStyleClass().add("menu-icon");
        icon.setMouseTransparent(true);

        String labelText = text.contains(" ") ? text.substring(text.indexOf(" ") + 1) : text;
        Label label = new Label(labelText);
        label.getStyleClass().add("menu-label");
        label.setMouseTransparent(true);

        item.getChildren().addAll(icon, label);
        item.setOnMouseClicked(e -> handleMenuClick(text));
        menuContainer.getChildren().add(item);
    }

    /**
     * Maneja el clic en cualquier opción del menú lateral.
     * Valida permisos y controla si hay operaciones en curso.
     */
    private void handleMenuClick(String menuText) {
        String opt = menuText.toUpperCase();
        String role = currentUser.getRole();

        // Control de navegación para evitar pérdida de datos
        if (opt.contains("INICIO") || opt.contains("DASHBOARD") || opt.contains("HOME")) {
            mostrarDashboard(); return;
        }
        
        // Bloque de cierre de sesión con verificación de operación activa
        if (opt.contains("SESIÓN") || opt.contains("CERRAR")) {
            if (com.clinica.aauca.util.Operacion.encurso) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Operación en Curso");
                confirm.setHeaderText("¿Quieres anular la operación en curso?");
                confirm.setContentText("Si cierras sesión ahora, perderás los datos no guardados.");
                java.util.Optional<ButtonType> result = confirm.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    com.clinica.aauca.util.Operacion.finalizar();
                } else {
                    return;
                }
            }
            cerrarSesion(); return;
        }

        // Lógica de carga de módulos basada en el texto del menú
        if (opt.contains("PACIENTES")) {
            cargarModulo("Pacientes", "pacientes_view.fxml");
        } else if (opt.contains("CONSULTA") || opt.contains("NUEVA CONSULTA")) {
            if (role.equals("Médico") || role.equals("Admin")) {
                cargarModulo("Consultas", "consulta_form.fxml");
            } else {
                denegarAcceso("Las consultas médicas son exclusivas del Médico.");
            }
        } else if (opt.contains("HOSPITALIZACIÓN") || opt.contains("HOSPITALIZACION")) {
            cargarModulo("Hospitalización", "hospitalizacion_view.fxml");
        } else if (opt.contains("MEDICAMENTOS")) {
            cargarModulo("Medicamentos", "medicamentos_view.fxml");
        } else if (opt.contains("HISTORIAL")) {
            cargarModulo("Historial Médico", "historial_view.fxml");
        } else if (opt.contains("USUARIOS") || opt.contains("GESTIÓN")) {
            if (role.equals("Admin")) {
                cargarModulo("Gestión de Usuarios", "usuarios_view.fxml");
            } else {
                denegarAcceso("La gestión de usuarios es exclusiva del Administrador.");
            }
        } else if (opt.contains("REPORTES")) {
            if (role.equals("Admin")) {
                cargarModulo("Reportes Clínicos", "reportes_view.fxml");
            } else {
                denegarAcceso("Los reportes son exclusivos del Administrador.");
            }
        } else if (opt.contains("PAGOS") || opt.contains("FACTURAS")) {
            if (role.equals("Admin")) {
                cargarModulo("Pagos y Facturas", "pagos_view.fxml");
            } else {
                denegarAcceso("La gestión de pagos es exclusiva del Administrador.");
            }
        } else if (opt.contains("CONFIGURACIÓN") || opt.contains("MI CUENTA")) {
            cargarModulo("Configuración", "configuracion_view.fxml");
        }
    }

    private void denegarAcceso(String motivo) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Acceso Denegado");
        alert.setHeaderText("No tiene permiso para acceder a esta sección");
        alert.setContentText(motivo + "\n\nContacte al administrador si necesita acceso.");
        alert.showAndWait();
    }

    /**
     * Muestra la pantalla de inicio con tarjetas de acceso directo personalizadas por rol.
     */
    private void mostrarDashboard() {
        workingArea.getChildren().clear();
        actualizarEstadisticasDashboard();

        VBox container = new VBox(20);
        container.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        container.setStyle("-fx-padding: 30;");

        VBox banner = new VBox(5);
        banner.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        banner.setStyle("-fx-padding: 20; -fx-background-color: #003366; -fx-background-radius: 12;");
        Label lblTitle = new Label("Panel de Inicio — Clínica AAUCA");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label lblRoleDesc = new Label(getRoleDescription(currentUser.getRole()));
        lblRoleDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #BBDEFB;");
        banner.getChildren().addAll(lblTitle, lblRoleDesc);

        Label lblSub = new Label("Seleccione un módulo:");
        lblSub.setStyle("-fx-text-fill: gray; -fx-font-size: 13px; -fx-padding: 5 0 0 0;");

        HBox row1 = new HBox(20); row1.setAlignment(javafx.geometry.Pos.CENTER);
        HBox row2 = new HBox(20); row2.setAlignment(javafx.geometry.Pos.CENTER);
        HBox row3 = new HBox(20); row3.setAlignment(javafx.geometry.Pos.CENTER);

        // Generar tarjetas según el rol para acceso rápido
        switch (currentUser.getRole()) {
            case "Admin":
                row1.getChildren().addAll(
                    tarjeta("PACIENTES", "USERS", "#003366", "pacientes_view.fxml", "Ver y gestionar pacientes"),
                    tarjeta("CONSULTAS", "STETHOSCOPE", "#2ecc71", "consulta_form.fxml", "Registrar consultas médicas"),
                    tarjeta("HOSPITALIZACIÓN", "BED", "#e67e22", "hospitalizacion_view.fxml", "Seguimiento de ingresos")
                );
                row2.getChildren().addAll(
                    tarjeta("MEDICAMENTOS", "MEDKIT", "#e74c3c", "medicamentos_view.fxml", "Inventario farmacéutico"),
                    tarjeta("HISTORIAL", "HEARTBEAT", "#1976D2", "historial_view.fxml", "Historial por paciente"),
                    tarjeta("USUARIOS", "USER", "#8e44ad", "usuarios_view.fxml", "Cuentas del sistema")
                );
                row3.getChildren().addAll(
                    tarjeta("REPORTES", "FILE_TEXT", "#16a085", "reportes_view.fxml", "Estadísticas clínicas"),
                    tarjeta("PAGOS (FCFA)", "MONEY", "#f39c12", "pagos_view.fxml", "Facturación total"),
                    tarjeta("CONFIGURACIÓN", "GEARS", "#34495e", "configuracion_view.fxml", "Ajustes del sistema")
                );
                break;
            case "Médico":
                row1.getChildren().addAll(
                    tarjeta("MIS PACIENTES", "USERS", "#003366", "pacientes_view.fxml", "Ver y registrar pacientes"),
                    tarjeta("NUEVA CONSULTA", "STETHOSCOPE", "#2ecc71", "consulta_form.fxml", "Registrar diagnóstico y receta"),
                    tarjeta("HISTORIAL MÉDICO", "HEARTBEAT", "#1976D2", "historial_view.fxml", "Consultas e ingresos previos")
                );
                row2.getChildren().addAll(
                    tarjeta("VER HOSPITALIZACIONES", "BED", "#e67e22", "hospitalizacion_view.fxml", "Estado de ingresos"),
                    tarjeta("VER MEDICAMENTOS", "MEDKIT", "#e74c3c", "medicamentos_view.fxml", "Consultar inventario")
                );
                break;
            case "Enfermero":
                row1.getChildren().addAll(
                    tarjeta("HOSPITALIZACIÓN", "BED", "#e67e22", "hospitalizacion_view.fxml", "Gestionar ingresos y evolución"),
                    tarjeta("MEDICAMENTOS", "MEDKIT", "#e74c3c", "medicamentos_view.fxml", "Gestionar stock e inventario"),
                    tarjeta("VER PACIENTES", "USERS", "#003366", "pacientes_view.fxml", "Consultar lista de pacientes")
                );
                row2.getChildren().addAll(
                    tarjeta("HISTORIAL", "HEARTBEAT", "#1976D2", "historial_view.fxml", "Ver historial por paciente")
                );
                break;
        }

        container.getChildren().addAll(banner, lblSub, row1, row2);
        if (!row3.getChildren().isEmpty()) container.getChildren().add(row3);
        workingArea.getChildren().add(container);
    }

    private String getRoleDescription(String role) {
        switch (role) {
            case "Admin":    return "Administrador del Sistema — Acceso completo a todos los módulos y configuraciones";
            case "Médico":   return "Médico — Gestión de pacientes, consultas e historial médico";
            case "Enfermero": return "Enfermero — Gestión de hospitalizaciones y stock de medicamentos";
            default:         return role;
        }
    }

    /**
     * Crea una tarjeta visual (Card) para el Dashboard.
     * Incluye animaciones de escala y cambio de color al pasar el ratón.
     */
    private VBox tarjeta(String titulo, String icono, String color, String fxml, String descripcion) {
        VBox card = new VBox(10);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.setPrefSize(210, 165);
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setStyle(estiloTarjeta(color, false));

        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setGlyphName(icono);
        icon.setSize("3.5em");
        icon.setFill(javafx.scene.paint.Color.web(color));
        icon.setMouseTransparent(true);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #2c3e50; -fx-text-alignment: center;");
        lblTitulo.setMouseTransparent(true);
        lblTitulo.setWrapText(true);

        Label lblDesc = new Label(descripcion);
        lblDesc.setStyle("-fx-font-size: 10px; -fx-text-fill: gray; -fx-text-alignment: center;");
        lblDesc.setMouseTransparent(true);
        lblDesc.setWrapText(true);
        lblDesc.setMaxWidth(190);

        card.getChildren().addAll(icon, lblTitulo, lblDesc);

        // Animaciones Hover
        card.setOnMouseEntered(e -> {
            card.setStyle(estiloTarjeta(color, true));
            icon.setFill(javafx.scene.paint.Color.WHITE);
            lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: white; -fx-text-alignment: center;");
            lblDesc.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.8); -fx-text-alignment: center;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle(estiloTarjeta(color, false));
            icon.setFill(javafx.scene.paint.Color.web(color));
            lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #2c3e50; -fx-text-alignment: center;");
            lblDesc.setStyle("-fx-font-size: 10px; -fx-text-fill: gray; -fx-text-alignment: center;");
        });
        card.setOnMouseClicked(e -> cargarModulo(titulo, fxml));
        return card;
    }

    private String estiloTarjeta(String color, boolean hover) {
        if (hover) {
            return "-fx-background-color: " + color + "; -fx-background-radius: 14; " +
                   "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 12, 0, 0, 6); " +
                   "-fx-scale-x: 1.04; -fx-scale-y: 1.04;";
        }
        return "-fx-background-color: white; -fx-background-radius: 14; " +
               "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 3); " +
               "-fx-border-color: " + color + "; -fx-border-width: 0 0 3 0; -fx-border-radius: 0 0 14 14;";
    }

    /**
     * Carga un archivo FXML dinámicamente en el área central.
     * Verifica si hay una operación activa antes de proceder.
     */
    private void cargarModulo(String titulo, String fxmlFile) {
        try {
            if (com.clinica.aauca.util.Operacion.encurso) {
                Alert alertC = new Alert(Alert.AlertType.CONFIRMATION);
                alertC.setTitle("Operación en Curso");
                alertC.setHeaderText("¿Quieres anular la operación en curso?");
                alertC.setContentText("Los cambios no guardados se perderán.");
                java.util.Optional<ButtonType> result = alertC.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    com.clinica.aauca.util.Operacion.finalizar();
                } else {
                    return;
                }
            }

            java.net.URL url = getClass().getResource("/com/clinica/aauca/view/" + fxmlFile);
            if (url == null) throw new IOException("FXML no encontrado: " + fxmlFile);

            FXMLLoader loader = new FXMLLoader(url);
            Node view = loader.load();

            // Pasar datos de sesión a controladores que lo requieran
            Object ctrl = loader.getController();
            if (ctrl instanceof HospitalizacionController) ((HospitalizacionController) ctrl).setCurrentUser(currentUser);
            if (ctrl instanceof MedicamentosController) ((MedicamentosController) ctrl).setCurrentUser(currentUser);
            if (ctrl instanceof ConfiguracionController) {
                ConfiguracionController configCtrl = (ConfiguracionController) ctrl;
                configCtrl.setCurrentUser(currentUser);
                configCtrl.setDashboardController(this);
            }

            VBox wrapper = new VBox(0);
            wrapper.setStyle("-fx-background-color: white;");

            // Barra de encabezado para el módulo cargado
            HBox header = new HBox(10);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            header.setStyle("-fx-padding: 10 20; -fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

            Button btnVolver = new Button("⬅ Inicio");
            btnVolver.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 15; -fx-background-radius: 5;");
            btnVolver.setCursor(javafx.scene.Cursor.HAND);
            btnVolver.setOnAction(e -> { 
                if (com.clinica.aauca.util.Operacion.encurso) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Operación en Curso");
                    confirm.setHeaderText("¿Quieres anular la operación en curso?");
                    confirm.setContentText("Los cambios no guardados se perderán.");
                    java.util.Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        com.clinica.aauca.util.Operacion.finalizar();
                    } else {
                        return;
                    }
                }
                mostrarDashboard(); 
                actualizarEstadisticasDashboard(); 
            });

            Label lblModuleTitle = new Label("📍 Módulo: " + titulo.toUpperCase());
            lblModuleTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #003366; -fx-font-size: 13px;");

            header.getChildren().addAll(btnVolver, new Separator(javafx.geometry.Orientation.VERTICAL), lblModuleTitle);

            wrapper.getChildren().addAll(header, view);
            VBox.setVgrow(view, javafx.scene.layout.Priority.ALWAYS);

            workingArea.getChildren().clear();
            workingArea.getChildren().add(wrapper);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar: " + titulo);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Cierra la sesión actual y vuelve a la pantalla de login.
     */
    private void cerrarSesion() {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) menuContainer.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/clinica/aauca/view/login.fxml"));
            stage.getScene().setRoot(loader.load());
            if (!stage.isMaximized()) {
                stage.setMaximized(true);
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error al cerrar sesión: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Método para navegar a un paciente específico desde otros módulos.
     */
    public void navegarAPaciente(String modulo, String fxml, int pacienteId) {
        try {
            java.net.URL url = getClass().getResource("/com/clinica/aauca/view/" + fxml);
            FXMLLoader loader = new FXMLLoader(url);
            Node view = loader.load();
            Object ctrl = loader.getController();

            if (ctrl instanceof HospitalizacionController) {
                ((HospitalizacionController) ctrl).setCurrentUser(currentUser);
                ((HospitalizacionController) ctrl).setPacienteHC(pacienteId);
            }
            if (ctrl instanceof ConsultaController) ((ConsultaController) ctrl).setPacienteHC(pacienteId);

            VBox wrapper = new VBox(0);
            HBox header = new HBox(10);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            header.setStyle("-fx-padding: 10 20; -fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
            Button btnVolver = new Button("⬅ Inicio");
            btnVolver.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 15; -fx-background-radius: 5;");
            btnVolver.setOnAction(e -> mostrarDashboard());
            header.getChildren().addAll(btnVolver, new Separator(javafx.geometry.Orientation.VERTICAL), new Label("📍 Módulo: " + modulo.toUpperCase()));

            wrapper.getChildren().addAll(header, view);
            VBox.setVgrow(view, javafx.scene.layout.Priority.ALWAYS);
            workingArea.getChildren().clear();
            workingArea.getChildren().add(wrapper);
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
    }
}
