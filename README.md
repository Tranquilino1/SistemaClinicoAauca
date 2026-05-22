# 🩺 Sistema de Gestión Clínica - AAUCA v1.0

Este repositorio contiene la versión de producción del **Sistema de Gestión Clínica AAUCA**, una plataforma de escritorio robusta desarrollada en **JavaFX** y estructurada mediante el patrón arquitectónico **MVC (Modelo-Vista-Controlador)**. La base de datos local utiliza **SQLite** y la seguridad de acceso se gestiona mediante encriptación de grado militar **BCrypt**.

Esta versión incluye un instalador profesional con asistente para Windows y detección inteligente de dependencias.

---

## 📥 Descarga y Distribución Oficial

Ofrecemos dos alternativas optimizadas para la distribución en ordenadores de la Clínica:

1.  **Instalador de Asistente de Windows (`ClinicaAauca_Setup.exe` - Recomendado)**:
    *   Un asistente de instalación estándar tipo "Siguiente, Siguiente, Instalar" con soporte bilingüe (Español / Inglés).
    *   Solicita privilegios de administrador (UAC Prompt) para asegurar los permisos de directorio.
    *   Crea accesos directos en el Escritorio y Menú Inicio.
    *   **Detección inteligente de Java 17+**: Comprueba el entorno y, si falta Java, guía al operador para instalarlo.
    *   Registra el desinstalador limpio en "Configuración > Aplicaciones" (Panel de Control) de Windows.
    *   👉 [**Descargar ClinicaAauca_Setup.exe**](https://github.com/Tranquilino1/SistemaClinicoAauca/releases/download/v1.0.0/ClinicaAauca_Setup.exe)

2.  **Ejecutable Portable Directo (`ClinicaAauca_Final.exe`)**:
    *   Para ejecución inmediata desde memorias USB o carpetas sin requerir instalación.
    *   Empaqueta los binarios mediante un envoltorio ligero en C# que extrae los recursos en directorios seguros temporales.
    *   👉 [**Descargar ClinicaAauca_Final.exe**](https://github.com/Tranquilino1/SistemaClinicoAauca/releases/download/v1.0.0/ClinicaAauca_Final.exe)

---

## 📖 Documentación Disponible

*   **Para Operadores y Médicos**: [Manual de Usuario (MANUAL_DE_USUARIO.md)](MANUAL_DE_USUARIO.md) - Guía completa paso a paso con las credenciales por defecto, explicación detallada de los flujos clínicos (Pacientes, Triaje, Consultas, Hospitalizaciones, Medicamentos) e instrucciones para realizar copias de seguridad de la base de datos de pacientes.
*   **Para Programadores e IT**: [Guía Técnica de Código (GUIA_COMPLETA_CODIGO.md)](GUIA_COMPLETA_CODIGO.md) - Manual de arquitectura de software, esquema de base de datos SQLite y compilación manual de binarios y wrappers C#.

---

## 🚀 Características Principales

*   **Autenticación y Seguridad**:
    *   Cifrado de contraseñas de usuarios mediante hashes **BCrypt**.
    *   Búsqueda insensible a mayúsculas/minúsculas y auto-recorte (`trim`) de espacios para garantizar accesos sin incidencias.
*   **Gestión Clínica Completa**:
    *   Registro integral de Pacientes con división nativa de campos de nombres y apellidos.
    *   Módulo de Signos Vitales (Triaje) en tiempo real con historial médico cronológico.
    *   Hospitalización de pacientes (seguimiento activo de camas e ingresos).
    *   Gestión de inventario de Medicamentos con alertas automáticas de bajo stock (resaltado en rojo por debajo de 10 unidades).
*   **Base de Datos Resiliente**:
    *   Persistencia real en el perfil local de usuario (`%USERPROFILE%\ClinicaAAUCA_Datos\`).
    *   Escritura inmediata a disco físico mediante el modo **WAL (Write-Ahead Logging)** de SQLite para evitar bloqueos por concurrencia. Esto garantiza que las actualizaciones del software **nunca** borren los datos de pacientes.
*   **Experiencia Visual de Producción**:
    *   Ventana principal auto-maximizada al iniciar la sesión que se adapta al tamaño del monitor.
    *   Integración del logotipo corporativo oficial circular en la barra de tareas y el marco de las ventanas.

---

## 🛠️ Requisitos del Entorno

1.  **Java Runtime Environment (JRE) / JDK 17 o superior**:
    *   Se requiere tener configurada la variable de entorno `JAVA_HOME`.
2.  **Motor SQLite**:
    *   Embebido de forma transparente a través de los controladores de dependencias (`sqlite-jdbc`).
3.  **Inno Setup 6 (Para recompilar el Instalador)**:
    *   Permite compilar el instalador `setup.iss` mediante consola.

---

## ⚙️ Estructura del Proyecto

```bash
Codigo_Fuente/
├── .gitignore                  # Exclusiones estándar para subidas a GitHub
├── LICENSE.txt                 # Licencia de código abierto MIT bilingüe oficial
├── setup.iss                   # Script de compilación de Inno Setup 6 para el instalador
├── upload_setup.js             # Utilidad NodeJS para subir automáticamente el instalador a GitHub
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

### Compilar el Instalador de Windows (`setup.iss`)
Si realiza modificaciones en los archivos y desea volver a compilar el instalador profesional ejecutable:
```powershell
# Compilar instalador con Inno Setup Compiler por consola
& "C:\Users\<TuUsuario>\AppData\Local\Programs\Inno Setup 6\ISCC.exe" setup.iss
```

---

## 🛡️ Licencia y Créditos
El código de este proyecto y su instalador están bajo la **[Licencia MIT (MIT License)](LICENSE.txt)**.
© 2026 Clínica AAUCA - Universidad Afroamericana de África Central (AAUCA). Todos los derechos reservados. Desarrollado de manera profesional para la gestión médica integral de la clínica universitaria.
