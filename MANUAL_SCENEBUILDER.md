# Manual de Modificación de Interfaz con Scene Builder

Este manual detalla paso a paso cómo abrir los archivos FXML modificados de la Clínica AAUCA en Scene Builder, cómo agregar controles y cómo vincular la interfaz con los controladores de Java.

## 1. Requisitos Iniciales

* **Herramientas**: Asegúrese de tener instalado Scene Builder (versión compatible con JavaFX 17 o superior).
* **Ruta de los archivos**: Encontrará las vistas FXML en la siguiente ruta del proyecto:
  `Codigo_Fuente/src/main/resources/com/clinica/aauca/view/`
* **Estilos**: Todos los estilos visuales heredan de la hoja de estilos institucional ubicada en:
  `Codigo_Fuente/src/main/resources/com/clinica/aauca/css/style.css`

---

## 2. Modificar el Detalle del Paciente

En este paso añadiremos el botón "Registrar Triaje" al panel lateral de acciones rápidas del paciente.

* **Abrir el archivo**: Inicie Scene Builder y abra `paciente_detalle.fxml`.
* **Localizar el contenedor**: En el panel izquierdo de jerarquía (`Hierarchy`), seleccione el `VBox` de botones dentro del encabezado principal (`HBox`).
* **Arrastrar el botón**: Busque `Button` en la librería (`Library`), arrástrelo y suéltelo dentro de la caja `VBox` (debajo de `btnHospitalizar`).
* **Configurar las propiedades (Inspector -> Properties)**:
  - **Text**: Establezca el texto en `📊 Registrar Triaje`.
  - **Pref Width**: Defina el ancho preferido en `180` para mantener la simetría.
  - **Style Class**: Agregue la clase `button` (para aplicar el estilo institucional).
* **Vincular el código (Inspector -> Code)**:
  - **fx:id**: Asigne el identificador `btnRegistrarTriaje`.
  - **On Action**: Ingrese el método `#registrarTriaje`.
* **Guardar**: Presione `Ctrl + S`.

---

## 3. Crear o Modificar el Diálogo de Triaje

Diseñaremos una ventana de diálogo dedicada únicamente a capturar constantes vitales para el triaje.

* **Abrir el archivo**: Abra `nuevo_triaje_dialog.fxml` en Scene Builder.
* **Estructura principal**:
  - Un contenedor raíz `VBox` con un espaciado (`Spacing`) de `20` y un relleno de márgenes (`Padding`) de `30`.
  - Un contenedor horizontal `HBox` que actúa como tarjeta visual, con un fondo blanco (`white`), padding de `40`, bordes redondeados (`border-radius: 12`) y sombra paralela (`dropshadow`).
  - Un contenedor `VBox` dentro del `HBox` con la propiedad de expansión horizontal `Hgrow` en `ALWAYS`.
* **Añadir el Título**:
  - Arrastre un control `Label` con el texto `REGISTRAR NUEVOS SIGNOS VITALES (Triaje)`.
  - Defina su estilo con fuente en negrita, tamaño `18px`, color `#1E293B` y un borde inferior verde `#10B981`.
* **Agregar la cuadrícula de entrada**:
  - Arrastre un control `GridPane` dentro del `VBox`.
  - Configure el espacio entre celdas en `Hgap: 25` y `Vgap: 18`.
  - Configure las restricciones de columna (`Column Constraints`):
    - Columna 0 (Etiquetas): Ancho mínimo `130`, ancho preferido `150`.
    - Columna 1 (Inputs): Hgrow configurado en `ALWAYS`.
* **Campos de entrada requeridos**: En cada fila del GridPane, coloque una etiqueta `Label` y un `TextField` enlazando sus propiedades de código:

| Fila | Constante Vital | ID de Código (fx:id) | Texto de Ayuda (Prompt Text) |
| :---: | :--- | :--- | :--- |
| **0** | Peso (kg): | `txtPeso` | Ej: 70.5 |
| **1** | Talla (cm): | `txtTalla` | Ej: 175 |
| **2** | Temperatura (°C): | `txtTemperatura` | Ej: 36.5 |
| **3** | Frec. Cardíaca: | `txtPulso` | Ej: 72 |
| **4** | Frec. Respiratoria: | `txtResp` | Ej: 16 |
| **5** | Presión Arterial: | `txtPresion` | Ej: 120/80 |

* **Estilo de los inputs**: Configure en cada `TextField` el estilo:  
  `-fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #CBD5E1; -fx-border-radius: 6; -fx-font-size: 13px;`

---

## 4. Conexión del Diálogo con el Controlador Java

* **Definir el controlador**: En la esquina inferior izquierda de Scene Builder, abra la pestaña **Controller** y en **Controller Class** escriba la ruta del controlador:  
  `com.clinica.aauca.controller.NuevoTriajeDialogController`
* **Vincular campos**: Seleccione cada `TextField` y asigne su respectivo ID (`fx:id`) en el panel **Code** de la derecha para que la lógica Java pueda recolectar los signos.
* **Guardar cambios**: Presione `Ctrl + S`.
