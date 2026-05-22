; =====================================================================
; SCRIPT DE INSTALACIÓN - SISTEMA CLÍNICO AAUCA (INNO SETUP 6)
; Universidad Afroamericana de África Central (AAUCA)
; =====================================================================

#define MyAppName "Sistema Clínico AAUCA"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Universidad Afroamericana de África Central (AAUCA)"
#define MyAppPublisherURL "https://www.aauca.edu.gq/"
#define MyAppExeName "ClinicaAauca_Final.exe"
#define MyAppIconName "app_icon.ico"

[Setup]
; Identificador único generado para la aplicación (AppId)
AppId={{C24B2763-718B-4DC0-9DA7-550C489DFE8A}}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppPublisherURL}
AppSupportURL={#MyAppPublisherURL}
AppUpdatesURL={#MyAppPublisherURL}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
; Requerir privilegios de administrador (UAC Prompt)
PrivilegesRequired=admin
PrivilegesRequiredOverridesAllowed=dialog
; Directorio de salida y nombre del instalador compilado
OutputDir=.
OutputBaseFilename=ClinicaAauca_Setup
; Icono oficial del instalador y del panel de control
SetupIconFile={#MyAppIconName}
UninstallDisplayIcon={app}\{#MyAppIconName}
; Pantalla de licencia obligatoria antes de instalar
LicenseFile=LICENSE.txt
; Compresión ultra eficiente
Compression=lzma2/max
SolidCompression=yes
WizardStyle=modern
; Configuración estética avanzada
DisableProgramGroupPage=yes
DisableWelcomePage=no

[Messages]
spanish.WelcomeLabel1=Bienvenido al Asistente de Instalación del Sistema Clínico AAUCA
spanish.WelcomeLabel2=Este programa instalará {#MyAppName} versión {#MyAppVersion} en su ordenador.%n%nSe recomienda cerrar todas las demás aplicaciones antes de continuar.
english.WelcomeLabel1=Welcome to the {#MyAppName} Setup Wizard
english.WelcomeLabel2=This will install {#MyAppName} version {#MyAppVersion} on your computer.%n%nIt is recommended that you close all other applications before continuing.


[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: checkedonce

[Files]
; Archivos principales de la aplicación en la carpeta de distribución
Source: "Clinica_AAUCA_V1.0_App\{#MyAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "Clinica_AAUCA_V1.0_App\Clinica_AAUCA.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "Clinica_AAUCA_V1.0_App\Iniciar_Clinica.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "Clinica_AAUCA_V1.0_App\LEEME_INSTRUCCIONES.txt"; DestDir: "{app}"; Flags: ignoreversion
Source: "Clinica_AAUCA_V1.0_App\clinica_aauca.db"; DestDir: "{app}"; Flags: ignoreversion; Permissions: users-modify
Source: "{#MyAppIconName}"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
; Accesos directos en el menú de inicio y escritorio
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\{#MyAppIconName}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; IconFilename: "{app}\{#MyAppIconName}"; Tasks: desktopicon

[Run]
; Opción de ejecutar la aplicación al finalizar el asistente
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[Code]
// Función personalizada para verificar si Java está instalado en el sistema
function IsJavaInstalled(): Boolean;
var
  ResultCode: Integer;
begin
  Result := False;
  // Ejecutamos silenciosamente "where java" que devuelve 0 si java está en el PATH
  if Exec('cmd.exe', '/c "where java"', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then
  begin
    if ResultCode = 0 then
    begin
      Result := True;
    end;
  end;
end;

// Evento llamado cuando el usuario hace clic en el botón Siguiente en el Asistente
function NextButtonClick(CurPageID: Integer): Boolean;
var
  ErrorCode: Integer;
  UserResponse: Integer;
  MsgTitle: String;
  MsgText: String;
begin
  Result := True;
  
  // Justo antes del proceso de instalación (en la pantalla wpReady)
  if CurPageID = wpReady then
  begin
    if not IsJavaInstalled() then
    begin
      if ActiveLanguage = 'spanish' then
      begin
        MsgTitle := 'Requisito de Sistema: Java no detectado';
        MsgText := 'ATENCIÓN: No se ha detectado Java (JRE/JDK) instalado en este ordenador.' + #13#10#13#10 +
                   'El Sistema Clínico AAUCA requiere Java 17 o superior para poder funcionar.' + #13#10#13#10 +
                   '¿Desea abrir la página oficial de descargas para descargar e instalar Java 17 (OpenJDK Temurin) gratis ahora mismo?';
      end
      else
      begin
        MsgTitle := 'System Requirement: Java not detected';
        MsgText := 'WARNING: Java (JRE/JDK) was not detected on this computer.' + #13#10#13#10 +
                   'Sistema Clínico AAUCA requires Java 17 or higher to run.' + #13#10#13#10 +
                   'Would you like to open the official download page to install Java 17 (OpenJDK Temurin) for free right now?';
      end;

      UserResponse := MsgBox(MsgText, mbConfirmation, MB_YESNO);
      if UserResponse = IDYES then
      begin
        // Abre el navegador predeterminado en la página de descargas de Adoptium Temurin 17 LTS
        ShellExec('open', 'https://adoptium.net/temurin/releases/?version=17', '', '', SW_SHOWNORMAL, ewNoWait, ErrorCode);
      end;
    end;
  end;
end;
