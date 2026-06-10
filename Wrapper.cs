using System;
using System.IO;
using System.Diagnostics;
using System.Reflection;
using System.Windows.Forms;

namespace ClinicaAauca.App
{
    class Program
    {
        [STAThread]
        static void Main()
        {
            try
            {
                // Obtener el directorio donde se encuentra este ejecutable
                string appDir = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
                
                // Rutas absolutas calculadas dinámicamente en tiempo de ejecución
                string privateJava = Path.Combine(appDir, @"jre\bin\javaw.exe");
                string jarPath = Path.Combine(appDir, "Clinica_AAUCA.jar");

                // Verificar existencia de la JRE privada en la instalación
                if (!File.Exists(privateJava))
                {
                    MessageBox.Show(
                        "Error de inicio: No se encontró el entorno de ejecución privado (JRE) en:\n" + privateJava + 
                        "\n\nPor favor, reinstale la aplicación o asegúrese de que el instalador finalizó correctamente.", 
                        "Sistema Clínico AAUCA", 
                        MessageBoxButtons.OK, 
                        MessageBoxIcon.Error
                    );
                    return;
                }

                // Verificar existencia del archivo base de la aplicación (JAR)
                if (!File.Exists(jarPath))
                {
                    MessageBox.Show(
                        "Error de inicio: No se encontró el archivo base de la aplicación (JAR) en:\n" + jarPath + 
                        "\n\nPor favor, reinstale la aplicación.", 
                        "Sistema Clínico AAUCA", 
                        MessageBoxButtons.OK, 
                        MessageBoxIcon.Error
                    );
                    return;
                }

                // Configurar y arrancar el proceso del JRE privado de forma directa
                ProcessStartInfo psi = new ProcessStartInfo();
                psi.FileName = privateJava;
                psi.Arguments = "-jar \"" + jarPath + "\"";
                psi.WorkingDirectory = appDir; // Carpeta de trabajo es la misma que la de instalación
                psi.CreateNoWindow = true;
                psi.UseShellExecute = false;
                
                Process.Start(psi);
            }
            catch (Exception ex)
            {
                MessageBox.Show(
                    "Error crítico al iniciar la aplicación:\n" + ex.Message, 
                    "Sistema Clínico AAUCA", 
                    MessageBoxButtons.OK, 
                    MessageBoxIcon.Error
                );
            }
        }
    }
}

