# 📖 Manual de Usuario - Sistema de Gestión Clínica AAUCA

Bienvenido al manual oficial de usuario del **Sistema Clínico AAUCA (Versión 1.0)**. Este software ha sido desarrollado específicamente para facilitar la administración médica, registro de signos vitales, historial clínico, hospitalizaciones e inventario de farmacia de la clínica de la **Universidad Afroamericana de África Central (AAUCA)**.

---

## 📋 Índice
1. [Requisitos Previos de Sistema](#-requisitos-previos-de-sistema)
2. [Instalación del Software](#-instalación-del-software)
3. [Inicio de Sesión y Credenciales por Defecto](#-inicio-de-sesión-y-credenciales-por-defecto)
4. [Módulos del Sistema y Flujo Clínico](#-módulos-del-sistema-y-flujo-clínico)
   - [A. Registro de Pacientes](#a-registro-de-pacientes)
   - [B. Módulo de Signos Vitales](#b-módulo-de-signos-vitales)
   - [C. Módulo de Consultas y Recetas](#c-módulo-de-consultas-y-recetas)
   - [D. Módulo de Hospitalizaciones (Camas)](#d-módulo-de-hospitalizaciones-camas)
   - [E. Control de Farmacia e Inventario](#e-control-de-farmacia-e-inventario)
5. [Seguridad y Persistencia de la Base de Datos](#-seguridad-y-persistencia-de-la-base-de-datos)
6. [Copias de Seguridad (Backups)](#-copias-de-seguridad-backups)

---

## 💻 Requisitos Previos de Sistema

Para ejecutar el programa, su ordenador debe cumplir con lo siguiente:
* **Sistema Operativo**: Windows 7 SP1, 8, 10 o 11 (32 o 64 bits).
* **Software Necesario**: **Java Runtime Environment (JRE) 17** o superior.
  - *Nota*: Si no tiene Java instalado, el propio instalador profesional lo detectará de forma automática y le proporcionará el enlace de descarga gratuita oficial de Adoptium Temurin 17 LTS.

---

## 📥 Instalación del Software

El proceso de instalación utiliza un asistente profesional estándar de Windows:

1. **Descarga**: Descargue el archivo de instalación oficial:
   👉 [**ClinicaAauca_Setup.exe (Descarga Directa)**](https://github.com/Tranquilino1/SistemaClinicoAauca/releases/download/v1.0.0/ClinicaAauca_Setup.exe)
2. **Ejecución**: Haga doble clic en `ClinicaAauca_Setup.exe`. Al ser una aplicación administrativa, Windows le solicitará privilegios de **Administrador (UAC Prompt)**. Seleccione **Sí**.
3. **Selección de Idioma**: Elija entre **Español** e **Inglés** para realizar la instalación.
4. **Acuerdo de Licencia**: Lea la Licencia de Código Abierto MIT y seleccione *"Acepto el acuerdo"* para habilitar el botón **Siguiente**.
5. **Ruta de Destino**: Por defecto, se instalará de forma segura en `C:\Program Files\Sistema Clínico AAUCA`. Puede modificar la ruta o presionar **Siguiente**.
6. **Accesos Directos**: Deje marcada la casilla *"Crear un acceso directo en el escritorio"* para facilitar el acceso rápido diario.
7. **Verificación de Java**: El instalador comprobará si tiene Java en su máquina. Si no se detecta, se mostrará una advertencia ofreciendo abrir la web para instalarlo en un solo clic.
8. **Finalización**: Tras completarse la barra de progreso, marque la casilla *"Ejecutar Sistema Clínico AAUCA"* y presione **Finalizar**.

---

## 🔑 Inicio de Sesión y Credenciales por Defecto

Al abrir la aplicación, verá la pantalla de Login institucional con el logotipo oficial de la AAUCA. Para acceder, utilice alguna de las siguientes cuentas preconfiguradas según el rol del operador:

| Rol de Usuario | Nombre de Usuario | Contraseña por Defecto | Permisos y Acciones |
| :--- | :--- | :--- | :--- |
| **Administrador** | `admin` | `admin123` | Control total del sistema, administración de usuarios y configuración general. |
| **Médico** | `medico` | `medico123` | Creación de consultas clínicas, diagnósticos, recetas de medicamentos y altas. |
| **Enfermero** | `enfermero` | `enfermero123` | Registro de pacientes y toma de signos vitales. |

> [!IMPORTANT]
> Por motivos de seguridad, las contraseñas no se almacenan en texto plano en la base de datos; se encuentran encriptadas de extremo a extremo utilizando el algoritmo **BCrypt** de grado militar.

---

## 🩺 Módulos del Sistema y Flujo Clínico

El sistema sigue el flujo natural de un hospital o clínica universitaria:

### A. Registro de Pacientes
* **Acceso**: Módulo **Pacientes** en el menú izquierdo.
* **Propósito**: Registrar a los estudiantes, docentes y personal de la universidad.
* **Acciones**:
  1. Presione **Nuevo Paciente**.
  2. Rellene el nombre completo, fecha de nacimiento, sexo, teléfono y nacionalidad.
  3. Indique si el paciente es **Estudiante** (lo que aplica descuentos automáticos en consultas y medicamentos) o personal docente/administrativo.
  4. Presione **Guardar**.

### B. Módulo de Signos Vitales
* **Acceso**: Seleccione un paciente y presione **Signos Vitales** o acceda al módulo desde el menú.
* **Propósito**: Tomar las medidas físicas del paciente antes de entrar al consultorio médico.
* **Acciones**:
  1. Introduzca: **Peso (kg)**, **Temperatura (°C)**, **Frecuencia Cardíaca (lpm)**, **Presión Arterial** y **Talla (cm)**.
  2. El sistema calculará automáticamente la fecha de registro.
  3. Presione **Guardar Signos Vitales**. Quedarán registrados de forma permanente en el historial del paciente.

### C. Módulo de Consultas y Recetas
* **Acceso**: Módulo **Consultas** o seleccionando al paciente en espera.
* **Propósito**: Destinado al médico para realizar el diagnóstico y prescribir medicamentos.
* **Acciones**:
  1. Seleccione al paciente y abra una **Nueva Consulta**.
  2. Complete el **Motivo de la Consulta**, **Historia Clínica Actual**, **Examen Físico** y **Diagnóstico**.
  3. **Prescribir Receta**: Seleccione medicamentos directamente del catálogo de farmacia, el sistema deducirá automáticamente las unidades del inventario en tiempo real.
  4. Genere la **Factura** asociada. Si es estudiante, el sistema calculará un coste reducido institucional.
  5. Presione **Guardar Consulta**. Podrá exportar e imprimir el historial médico y la receta en formato profesional PDF.

### D. Módulo de Hospitalizaciones (Camas)
* **Acceso**: Módulo **Hospitalizaciones** (Icono de cama médica).
* **Propósito**: Control de pacientes que requieren observación o tratamiento continuo en la clínica.
* **Acciones**:
  1. **Ingreso**: Seleccione el paciente, asigne un número de cama libre e introduzca el motivo del ingreso y las pautas de seguimiento médico.
  2. **Evolución**: El personal médico puede actualizar las notas de evolución y estado del paciente a lo largo del tiempo.
  3. **Alta Médica**: Al finalizar el tratamiento, introduzca los costes de servicios adicionales y presione **Dar de Alta** para liberar la cama automáticamente.

### E. Control de Farmacia e Inventario
* **Acceso**: Módulo **Medicamentos** (Icono de píldoras).
* **Propósito**: Monitorear el inventario de medicamentos de la clínica de la AAUCA.
* **Acciones**:
  1. Añada nuevos fármacos al catálogo con su precio público y stock inicial.
  2. **Alertas de Stock**: Si un medicamento tiene menos de 10 unidades, el stock se resaltará en **rojo** para alertar al farmacéutico sobre la necesidad de reabastecimiento.

---

## 💾 Seguridad y Persistencia de la Base de Datos

Para evitar que los datos clínicos confidenciales se pierdan al actualizar el software o al desinstalarlo:
* La base de datos no se guarda en la carpeta de instalación de `Program Files` (que Windows suele proteger contra escritura).
* Se guarda de forma segura en la carpeta personal de datos del usuario actual de Windows:  
  `C:\Users\<TuUsuario>\ClinicaAAUCA_Datos\clinica_aauca.db`
* La aplicación utiliza la tecnología **WAL (Write-Ahead Logging)** de SQLite, lo que asegura que las escrituras se realicen en tiempo real directamente en el disco duro, evitando daños en la base de datos si ocurre un corte de luz o cierre repentino del ordenador.

---

## 🛡️ Copias de Seguridad (Backups)

Hacer una copia de seguridad de todos los pacientes, historiales clínicos, recetas e inventario es sumamente fácil y seguro:

1. Presione las teclas `Windows + R` en su teclado para abrir el cuadro Ejecutar.
2. Escriba `%USERPROFILE%` y presione Enter.
3. Busque la carpeta llamada **`ClinicaAAUCA_Datos`**.
4. Copie esa carpeta por completo en un pendrive USB, disco externo o almacenamiento en la nube seguro (Google Drive, OneDrive).
5. **Restauración**: Si cambia de ordenador o formatea su sistema, simplemente instale el software de nuevo con `ClinicaAauca_Setup.exe` y vuelva a pegar la carpeta `ClinicaAAUCA_Datos` en el mismo directorio de su usuario. La aplicación la reconocerá al instante con todos sus datos intactos.
