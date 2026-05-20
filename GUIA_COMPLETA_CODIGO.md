# 📘 Guía Maestra del Código Fuente: Clínica AAUCA

Esta guía proporciona una explicación técnica exhaustiva de cada componente, archivo y lógica dentro del sistema de gestión clínica.

---

## 🏗️ 1. Arquitectura General del Proyecto
El sistema sigue el patrón **MVC (Modelo-Vista-Controlador)** para separar la lógica de negocio de la interfaz visual.

*   **Lenguaje:** Java 17+
*   **Interfaz:** JavaFX 17 (FXML)
*   **Base de Datos:** SQLite (Local)
*   **Reportes:** OpenPDF (Librería de código abierto)

---

## 📂 2. Estructura de Carpetas y Archivos

### `com.clinica.aauca.controller` (La Lógica)
*   **`LoginController.java`**: Controla el acceso. Verifica usuarios usando encriptación BCrypt y gestiona la transición al menú principal.
*   **`DashboardController.java`**: Es el cerebro de la navegación. Carga los módulos dinámicamente en el panel central y gestiona los permisos de usuario (Admin, Médico, Enfermero).
*   **`ConsultaController.java`**: Gestiona las visitas médicas. Contiene la lógica de precios, recetas vinculadas al inventario y el bloqueo de seguridad para consultas impresas.
*   **`HospitalizacionController.java`**: Controla los pacientes internados. Calcula automáticamente el costo total basado en los días de estancia al dar el alta.
*   **`PacienteController.java`**: CRUD (Crear, Leer, Actualizar, Borrar) de la base de datos de pacientes.
*   **`MedicamentosController.java`**: Panel de control de farmacia. Permite ajustar stock y precios.
*   **`UsuariosController.java`**: (Solo para Admins) Gestión de cuentas de personal clínico.

### `com.clinica.aauca.model` (Los Objetos)
Representan las entidades de la vida real en código:
*   **`User.java`**: Datos del personal (nombre, rol, usuario).
*   **`Paciente.java`**: Datos personales y estado (estudiante/particular).
*   **`Consulta.java`**: Datos de una cita médica (diagnóstico, receta, factura).
*   **`Medicamento.java`**: Datos del fármaco (nombre, stock, precio).
*   **`Hospitalizacion.java`**: Datos de internamiento (fecha ingreso/alta, seguimiento).

### `com.clinica.aauca.dao` (El Acceso a Datos)
Contiene las sentencias SQL puras para comunicarse con SQLite:
*   **`UserDAOImpl.java`**: Implementa el registro y login seguro.
*   **`ConsultaDAO.java`**: Maneja el historial clínico.
*   **`HospitalizacionDAO.java`**: Maneja ingresos y facturas de estancia.

### `com.clinica.aauca.util` (Herramientas de Apoyo)
*   **`DatabaseConnector.java`**: Abre la conexión con el archivo `.db` y crea las tablas si no existen.
*   **`PDFService.java`**: Convierte los datos médicos en archivos PDF profesionales guardados en el Escritorio.
*   **`Operacion.java`**: Clase centinela que previene la pérdida de datos si el usuario intenta navegar sin guardar.

---

## ⚙️ 3. Lógica Clave Explicada

### 🔐 Seguridad (BCrypt)
El sistema no guarda contraseñas como "12345". En su lugar, genera un "hash" irreconocible. Cuando te logueas, el sistema no compara textos, sino que valida el hash, protegiendo la privacidad del personal.

### 📦 Gestión de Inventario Automática
En el archivo `ConsultaController.java`, al guardar una consulta:
```java
if (m != null && txtReceta.getText().contains(m.getNombre())) {
    int nuevaCant = m.getStock() - spinCant.getValue();
    m.setStock(nuevaCant);
    medDAO.actualizar(m);
}
```
Este bloque detecta si recetaste un fármaco del combo y descuenta automáticamente el stock, manteniendo la farmacia siempre actualizada.

### 💰 Facturación Diferenciada (AAUCA)
El sistema identifica si el paciente es estudiante:
*   **Estudiantes:** Factura = "EXENTO" (0 FCFA).
*   **Particulares:** Factura = Precio base (7,500 FCFA) + Costo de medicamentos recetados.

---

## 🎨 4. Recursos Visuales
*   **`src/main/resources/com/clinica/aauca/view/`**: Contiene los archivos `.fxml` que definen la posición de botones y tablas.
*   **`src/main/resources/com/clinica/aauca/css/style.css`**: Define los colores (Azul AAUCA, Gris Premium), bordes redondeados y efectos visuales de los botones.

---

## 🚀 5. Flujo de Ejecución del Sistema
1.  Se inicia `MainApp.java`.
2.  `DatabaseConnector` verifica que el archivo `clinica_aauca.db` exista.
3.  Se carga la pantalla de **Login**.
4.  Tras validar el usuario, se abre el **Dashboard**.
5.  El usuario navega por módulos. Si intenta salir de una consulta a medias, `Operacion.encurso` lanza una advertencia.
6.  Al terminar una consulta, el sistema genera un PDF y lo abre automáticamente para impresión.

---

> [!IMPORTANT]
> **Mantenimiento:** Para añadir nuevas funciones, se recomienda crear primero el Modelo, luego el DAO y finalmente el Controlador con su correspondiente vista FXML.
