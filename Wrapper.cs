using System;
using System.IO;
using System.Reflection;
using System.Diagnostics;
using System.IO.Compression;

namespace ClinicaAauca.App
{
    class Program
    {
        [STAThread]
        static void Main()
        {
            try
            {
                // Crear un directorio temporal seguro para la ejecución portable
                string tempDir = Path.Combine(Path.GetTempPath(), "ClinicaAAUCA_" + Guid.NewGuid().ToString());
                Directory.CreateDirectory(tempDir);

                // Extraer el archivo ZIP de los recursos incrustados en el binario
                Assembly assembly = Assembly.GetExecutingAssembly();
                using (Stream stream = assembly.GetManifestResourceStream("app_bundle.zip"))
                {
                    if (stream == null) {
                        // Buscar el recurso utilizando el espacio de nombres
                        using (Stream stream2 = assembly.GetManifestResourceStream("ClinicaAauca.App.app_bundle.zip")) {
                             if (stream2 == null) throw new Exception("Recurso de la aplicación no encontrado.");
                             GuardarYEjecutar(stream2, tempDir);
                        }
                    } else {
                        GuardarYEjecutar(stream, tempDir);
                    }
                }
            }
            catch (Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Error de inicialización: " + ex.Message, "Clínica AAUCA", System.Windows.Forms.MessageBoxButtons.OK, System.Windows.Forms.MessageBoxIcon.Error);
            }
        }

        static void GuardarYEjecutar(Stream stream, string tempDir) {
            string zipPath = Path.Combine(tempDir, "bundle.zip");
            using (FileStream fs = new FileStream(zipPath, FileMode.Create))
            {
                stream.CopyTo(fs);
            }

            // Descomprimir el paquete en el directorio temporal
            ZipFile.ExtractToDirectory(zipPath, tempDir);

            // Obtener la ruta donde se ejecuta este Wrapper
            string appDir = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
            string privateJava = Path.Combine(appDir, @"jre\bin\javaw.exe");

            if (File.Exists(privateJava))
            {
                // Si existe la JRE privada, ejecutar con ella de forma directa y silenciosa
                ProcessStartInfo psi = new ProcessStartInfo();
                psi.FileName = privateJava;
                psi.Arguments = "-jar \"Clinica_AAUCA.jar\"";
                psi.WorkingDirectory = tempDir;
                psi.CreateNoWindow = true;
                psi.UseShellExecute = false;
                Process.Start(psi);
            }
            else
            {
                // Si no existe, caer en el script por lotes por defecto (sistema)
                string batPath = Path.Combine(tempDir, "Iniciar_Clinica.bat");
                if (File.Exists(batPath))
                {
                    ProcessStartInfo psi = new ProcessStartInfo(batPath);
                    psi.WorkingDirectory = tempDir;
                    psi.WindowStyle = ProcessWindowStyle.Hidden;
                    Process.Start(psi);
                }
            }
        }
    }
}
