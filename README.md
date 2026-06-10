# 🩺 Sistema de Gestión Clínica - AAUCA v1.0

Este repositorio contiene la versión estable y de producción del **Sistema de Gestión Clínica AAUCA**, una plataforma de escritorio robusta desarrollada en **JavaFX** y estructurada mediante el patrón arquitectónico **MVC (Modelo-Vista-Controlador)**. La base de datos local utiliza **SQLite** y la seguridad de acceso se gestiona mediante encriptación de grado militar **BCrypt**.

## 🚀 Características Principales

*   **Autenticación y Seguridad**:
    *   Cifrado de contraseñas de usuarios mediante hashes **BCrypt**.
    *   Búsqueda insensible a mayúsculas/minúsculas y auto-recorte (`trim`) de espacios para garantizar accesos sin incidencias.
*   **Gestión Clínica Completa**:
    *   Registro integral de Pacientes con división nativa de campos de nombres y apellidos.
    *   Módulo de Signos Vitales (Triaje) en tiempo real con historial médico cronológico.
    *   Hospitalización de pacientes (seguimiento activo de camas e ingresos).
    *   Gestión de inventario de Medicamentos con alertas automáticas de bajo stock.
*   **Base de Datos Resiliente**:
    *   Persistencia real en el perfil local de usuario (`%USERPROFILE%\ClinicaAAUCA_Datos\`).
    *   Escritura inmediata a disco físico mediante el modo **WAL (Write-Ahead Logging)** de SQLite para evitar bloqueos por concurrencia.
*   **Experiencia Visual de Producción**:
    *   Ventana principal auto-maximizada al iniciar la sesión que se adapta al tamaño del monitor.
    *   Integración del logotipo corporativo oficial circular en la barra de tareas y el marco de las ventanas.

---

## 🛠️ Requisitos del Entorno

1.  **Java Runtime Environment (JRE) / JDK 17 o superior**:
    *   Se requiere tener configurada la variable de entorno `JAVA_HOME`.
2.  **Motor SQLite**:
    *   Embebido de forma transparente a través de los controladores de dependencias (`sqlite-jdbc`).
3.  **Maven**:
    *   Para compilar y empaquetar de forma estructurada.

---

## ⚙️ Estructura del Proyecto

```bash
Codigo_Fuente/
├── .gitignore                  # Exclusiones estándar para subidas a GitHub
├── pom.xml                     # Gestión de dependencias Maven (JavaFX, SQLite, BCrypt, iText)
├── Wrapper.cs                  # Envoltorio C# compilable para crear el .exe portable
├── src/
│   └── main/
│       ├── java/               # Lógica del Sistema (Controllers, DAO, Models, Utilidades)
│       └── resources/          # Vistas FXML, Hojas de Estilo CSS e Imágenes del Sistema
```

---

## 🏗️ Instrucciones de Compilación y Lanzamiento

### Compilar el JAR Sombreado (Shaded)
Para compilar y empaquetar todas las dependencias dentro de un único archivo ejecutable `.jar`, ejecute en su terminal:
```bash
mvn clean package
```
El archivo JAR resultante se ubicará en la carpeta `target/clinica-sistema-gestion-1.0.jar`.

### Compilar el Ejecutable Portable (EXE) en Windows
El ejecutable utiliza un wrapper ligero en C# que descomprime las bibliotecas y recursos de Java en un entorno temporal aislado. Para compilarlo utilizando el compilador nativo de .NET Framework:
```powershell
# Crear paquete ZIP
Compress-Archive -Path ClinicaAAUCA_Portable\* -DestinationPath app_bundle.zip -Force

# Compilar C# con icono institucional incrustado
C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe /target:winexe /out:ClinicaAauca_Final.exe /win32icon:app_icon.ico /resource:app_bundle.zip,ClinicaAauca.App.app_bundle.zip /r:System.IO.Compression.FileSystem.dll /r:System.IO.Compression.dll /r:System.Windows.Forms.dll Wrapper.cs
```

---

## 🛡️ Licencia y Créditos
© 2026 Clínica AAUCA. Todos los derechos reservados. Desarrollado de manera profesional para la gestión médica integral.
