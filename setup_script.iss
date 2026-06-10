; Script de Inno Setup para Clínica AAUCA
; Basado en el Plan Maestro de Empaquetado y Despliegue Profesional

#define MyAppName "Clinica AAUCA"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Afro-American University of Central Africa (AAUCA)"
#define MyAppPublisherURL "https://www.aauca.edu.gq/"
#define MyAppExeName "ClinicaAauca_Final.exe"

[Setup]
; AppId único para la aplicación
AppId={{9DC2349E-621E-41EF-B4AF-E968BDC8A5B8}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppPublisherURL}
DefaultDirName={autopf}\{#MyAppName}
DisableProgramGroupPage=yes
LicenseFile=build-dist\EULA_es.rtf
SetupIconFile=build-dist\installer_icon.ico
WizardStyle=modern
Compression=lzma2/ultra64
SolidCompression=yes
OutputDir=dist_ready
OutputBaseFilename=ClinicaAauca_Setup

; NOTA PARA LA FIRMA DIGITAL:
; Si dispone de un certificado de firma de código (.pfx), desactive la siguiente línea
; y configure su herramienta de firma en Inno Setup (Tools -> Configure Sign Tools)
; con el nombre "mysigntool".
; SignTool=mysigntool

[Languages]
Name: "spanish"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; Binario principal ejecutable (Launcher nativo C#)
Source: "build-dist\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
; Archivo base de la aplicación Java (JAR)
Source: "build-dist\Clinica_AAUCA.jar"; DestDir: "{app}"; Flags: ignoreversion
; Base de datos para la primera ejecución
Source: "build-dist\clinica_aauca.db"; DestDir: "{app}"; Flags: ignoreversion; Permissions: users-modify
; JRE Privado para ejecución autónoma sin dependencias globales de Java
Source: "C:\Java\jdk-17\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

