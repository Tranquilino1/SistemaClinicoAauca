package com.clinica.aauca.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Gestor central de la conexión a la base de datos SQLite.
 * Implementa el patrón Singleton para asegurar una única conexión activa.
 */
public class DatabaseConnector {
    private static final String URL = obtenerURLPersistente();
    private static Connection connection = null;

    private static String obtenerURLPersistente() {
        String userHome = System.getProperty("user.home");
        File dir = new File(userHome, "ClinicaAAUCA_Datos");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dbFile = new File(dir, "clinica_aauca.db");
        
        // Importación automática: si no existe la base de datos en la ruta persistente,
        // pero existe una local en el directorio de ejecución, la copiamos para conservar los datos
        if (!dbFile.exists()) {
            File localDb = new File("clinica_aauca.db");
            if (localDb.exists()) {
                try {
                    java.nio.file.Files.copy(localDb.toPath(), dbFile.toPath());
                    System.out.println("✅ Base de datos local importada exitosamente a la ruta persistente: " + dbFile.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("Advertencia al importar base de datos local: " + e.getMessage());
                }
            }
        }
        return "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    private static boolean dbInitialized = false;

    private DatabaseConnector() {
        // Constructor privado para evitar instanciación externa
    }

    /**
     * Obtiene una conexión activa a la base de datos.
     * Si no existe, la crea e inicializa las tablas base.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Registro manual del driver para compatibilidad en entornos JAR
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);
                
                // Habilitar almacenamiento ultra seguro y rápido en tiempo real (WAL)
                try (Statement pragmaStmt = connection.createStatement()) {
                    pragmaStmt.execute("PRAGMA journal_mode=WAL;");
                    pragmaStmt.execute("PRAGMA synchronous=NORMAL;");
                } catch (SQLException pragmaEx) {
                    System.err.println("Advertencia al configurar PRAGMAs SQLite: " + pragmaEx.getMessage());
                }

                if (!dbInitialized) {
                    initializeDatabase(); // Crea tablas y siembra datos iniciales una sola vez
                    dbInitialized = true;
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Error: Driver JDBC de SQLite no encontrado.");
                throw new SQLException(e);
            }
        }
        return connection;
    }

    /**
     * Configura la estructura inicial de la base de datos.
     * Lee el archivo 'schema.sql' para crear tablas y aplica migraciones de seguridad.
     */
    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            
            // Carga el archivo SQL desde los recursos del JAR/Proyecto
            InputStream is = DatabaseConnector.class.getResourceAsStream("/com/clinica/aauca/sql/schema.sql");
            if (is == null) {
                System.err.println("CRÍTICO: No se encontró el esquema SQL.");
                return;
            }

            // Lectura línea por línea del script SQL
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    // Ignorar comentarios SQL e instrucciones vacías
                    if (line.trim().isEmpty() || line.startsWith("--")) continue;
                    sql.append(line).append(" ");
                    // Ejecutar el bloque SQL cuando se encuentra un punto y coma
                    if (line.trim().endsWith(";")) {
                        stmt.execute(sql.toString());
                        sql.setLength(0);
                    }
                }
            }

            // Asegura que las columnas añadidas recientemente existan
            ejecutarMigraciones(conn);

            // Inserta datos de demostración para que el sistema no esté vacío
            poblarDatosPrueba(conn);

            // Sincronización automática de credenciales: actualiza SOLO si el password actual está en texto plano
            try (Statement checkStmt = conn.createStatement();
                 ResultSet rs = checkStmt.executeQuery("SELECT id, username, password FROM usuarios")) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String pass = rs.getString("password");
                    
                    // Si el password no empieza con $2 (lo que indica que no es un hash BCrypt válido)
                    if (pass != null && !pass.startsWith("$2")) {
                        String hashed = "";
                        if ("admin".equals(username)) {
                            hashed = "$2a$12$o1yLTe.4SRKOaWLVVmsoueRzGsjiJ3Xp0TxJ3KV0K9zOhmUbqM2r6"; // admin123
                        } else if ("medico".equals(username) || "enfermero".equals(username)) {
                            hashed = "$2a$12$nmd3s0HBqqzA9YR1ZW6vI.8bfSResIOTcgZa81XTgSaqG8KhjC5lS"; // medico123 / enfermero123
                        } else {
                            // Para cualquier otro usuario personalizado, hasheamos su contraseña texto plano
                            hashed = org.mindrot.jbcrypt.BCrypt.hashpw(pass, org.mindrot.jbcrypt.BCrypt.gensalt(12));
                        }
                        
                        try (PreparedStatement psUpdate = conn.prepareStatement("UPDATE usuarios SET password = ? WHERE id = ?")) {
                            psUpdate.setString(1, hashed);
                            psUpdate.setInt(2, id);
                            psUpdate.executeUpdate();
                            System.out.println("🔑 Contraseña en texto plano del usuario " + username + " actualizada a hash BCrypt.");
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Advertencia al sincronizar credenciales: " + e.getMessage());
            }

            System.out.println("✅ Base de datos configurada y lista.");
            
        } catch (Exception e) {
            System.err.println("Fallo al inicializar: " + e.getMessage());
        }
    }

    /**
     * Ejecuta ALTER TABLE de forma segura para añadir columnas si no existen.
     */
    private static void ejecutarMigraciones(Connection conn) {
        String[] columns = {
            "ALTER TABLE hospitalizaciones ADD COLUMN fecha_ingreso TEXT DEFAULT '2024-01-01';",
            "ALTER TABLE hospitalizaciones ADD COLUMN motivo TEXT;",
            "ALTER TABLE consultas ADD COLUMN motivo_consulta TEXT;",
            "ALTER TABLE consultas ADD COLUMN diagnostico TEXT;",
            "ALTER TABLE consultas ADD COLUMN tratamiento TEXT;",
            "ALTER TABLE consultas ADD COLUMN antecedentes TEXT;",
            "ALTER TABLE consultas ADD COLUMN estado TEXT DEFAULT 'guardado';",
            "ALTER TABLE medicamentos ADD COLUMN precio REAL DEFAULT 0;",
            "ALTER TABLE hospitalizaciones ADD COLUMN fecha_alta TEXT;",
            "ALTER TABLE hospitalizaciones ADD COLUMN monto_total REAL DEFAULT 0;",
            "ALTER TABLE pacientes ADD COLUMN tipo TEXT;",
            "ALTER TABLE pacientes ADD COLUMN sexo TEXT;",
            "ALTER TABLE pacientes ADD COLUMN direccion TEXT;",
            "ALTER TABLE pacientes ADD COLUMN telefono TEXT;",
            "ALTER TABLE pacientes ADD COLUMN nacionalidad TEXT;",
            "ALTER TABLE consultas ADD COLUMN historia_actual TEXT;",
            "ALTER TABLE consultas ADD COLUMN antecedentes_familiares TEXT;",
            "ALTER TABLE consultas ADD COLUMN antecedentes_personales TEXT;",
            "ALTER TABLE consultas ADD COLUMN examen_fisico TEXT;",
            "ALTER TABLE consultas ADD COLUMN laboratorio TEXT;",
            "CREATE TABLE IF NOT EXISTS signos_vitales (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "paciente_id INTEGER NOT NULL, " +
            "fecha TEXT NOT NULL, " +
            "peso TEXT, " +
            "temperatura TEXT, " +
            "frecuencia_cardiaca TEXT, " +
            "frecuencia_respiratoria TEXT, " +
            "presion_arterial TEXT, " +
            "talla TEXT, " +
            "FOREIGN KEY(paciente_id) REFERENCES pacientes(id));"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : columns) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    // Ignorar error si la columna ya existe
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en migraciones: " + e.getMessage());
        }
    }

    /**
     * Inserta datos de prueba si la tabla de pacientes tiene pocos registros.
     */
    private static void poblarDatosPrueba(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Verificar si hay menos de 9 pacientes
            ResultSet rsP = stmt.executeQuery("SELECT COUNT(*) FROM pacientes");
            int numPacientes = rsP.next() ? rsP.getInt(1) : 0;
            rsP.close();

            if (numPacientes < 25) {
                System.out.println("Poblando pacientes guineanos de prueba (30 totales)...");
                String[] nombres = {
                    "Pelayo Nvulu (Informática)", "Carlota Raquel (Medicina)", "Mauricio Edu (Economía)",
                    "Ángel Asimi (Ing. Eléctrica)", "Tranquilino Maba (Agro)", "Profe. Diosdado (Docente)",
                    "Evaristo Olo (Civil)", "Natividad Eseng (Mecánica)", "Patricio Mba (ADE)", "Beatriz Nguema (Artes)",
                    "Bonifacio Ondo Mba (Administración)", "Teresa Avomo Nguema (Docente)", "Saturnino Obiang (Decanato)",
                    "María Consolación Bindang (Secretaría)", "Faustino Edu (Administrativo)", "Josefa Mikue (Docente)",
                    "Clemente Nsue (Contabilidad)", "Milagrosa Ayeto (Docente)", "Diosdado Nfa (Mantenimiento)",
                    "Esperanza Okomo (Administración)", "Baltasar Ndong (Docente)", "Rufina Eseng (RRHH)",
                    "Plácido Mba (Administrativo)", "Victoria Ada (Docente)", "Gregorio Nguema (Seguridad)",
                    "Braulio Nguema (Informática)", "Elena Mengue (Medicina)", "Secundino Ondo (Economía)",
                    "Prisca Eyenga (Derecho)", "Modesto Esono (Ingeniería)"
                };

                for (int i = 0; i < nombres.length; i++) {
                    String pSql = "INSERT OR IGNORE INTO pacientes (id, nombre_completo, es_estudiante, fecha_nacimiento) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(pSql)) {
                        ps.setInt(1, i + 1);
                        ps.setString(2, nombres[i]);
                        // i >= 10 && i < 25 son los nuevos administrativos/docentes (15 personas)
                        // los últimos 5 (25-29) son estudiantes
                        boolean esEst = (i < 5) || (i >= 6 && i < 10) || (i >= 25);
                        ps.setInt(3, esEst ? 1 : 0);
                        ps.setString(4, "19" + (70 + (i % 30)) + "-05-10");
                        ps.executeUpdate();
                    }
                }
            }

            // Asegurar que hay consultas (mínimo 2 por cada uno de los primeros 9 pacientes)
            ResultSet rsC = stmt.executeQuery("SELECT COUNT(*) FROM consultas");
            int numConsultas = rsC.next() ? rsC.getInt(1) : 0;
            rsC.close();

            if (numConsultas < 18) {
                System.out.println("Insertando historiales clínicos de prueba...");
                for (int i = 1; i <= 10; i++) {
                    for (int j = 1; j <= 2; j++) {
                        String cSql = "INSERT INTO consultas (paciente_id, fecha, motivo_consulta, diagnostico, tratamiento, antecedentes, receta, factura, estado) " +
                                     "VALUES (?, date('now', '-" + (j * 5) + " days'), 'Chequeo rutinario " + j + "', 'Estable', 'Seguimiento normal', 'Ninguno', 'N/A', '0', 'impreso')";
                        try (PreparedStatement ps = conn.prepareStatement(cSql)) {
                            ps.setInt(1, i);
                            ps.executeUpdate();
                        }
                    }
                }
                System.out.println("✅ Datos de prueba (Pacientes y Consultas) listos.");
            }

            // Asegurar que hay hospitalizaciones ACTIVAS para probar (mínimo 3)
            ResultSet rsH = stmt.executeQuery("SELECT COUNT(*) FROM hospitalizaciones WHERE estado = 'ingresado'");
            int numHosp = rsH.next() ? rsH.getInt(1) : 0;
            rsH.close();

            if (numHosp < 3) {
                System.out.println("Insertando hospitalizaciones de prueba...");
                int[] idsH = {1, 3, 5};
                String[] motivos = {"Paludismo severo", "Observación post-accidente", "Crisis hipertensiva"};
                for (int i = 0; i < idsH.length; i++) {
                    String hSql = "INSERT INTO hospitalizaciones (paciente_id, estado, seguimiento, fecha_ingreso, motivo) " +
                                  "VALUES (?, 'ingresado', 'Evolución estable, requiere monitoreo cada 4h.', date('now', '-2 days'), ?)";
                    try (PreparedStatement ps = conn.prepareStatement(hSql)) {
                        ps.setInt(1, idsH[i]);
                        ps.setString(2, motivos[i]);
                        ps.executeUpdate();
                    }
                }
                System.out.println("✅ Hospitalizaciones de prueba listas.");
            }

            // Asegurar 50 Medicamentos
            ResultSet rsM = stmt.executeQuery("SELECT COUNT(*) FROM medicamentos");
            int numMeds = rsM.next() ? rsM.getInt(1) : 0;
            rsM.close();

            if (numMeds < 5) { // Si hay pocos o ninguno, sembrar 50
                System.out.println("Sembrando inventario de medicamentos...");
                String[] meds = {
                    "Amoxicilina 500mg", "Azitromicina 500mg", "Ciprofloxacino 500mg", "Claritromicina 500mg", "Doxiciclina 100mg",
                    "Metronidazol 500mg", "Penicilina V 250mg", "Cefalexina 500mg", "Levofloxacino 500mg", "Nitrofurantoína 100mg",
                    "Alprazolam 0.5mg", "Diazepam 10mg", "Lorazepam 2mg", "Sertralina 50mg", "Fluoxetina 20mg",
                    "Escitalopram 10mg", "Amitriptilina 25mg", "Quetiapina 100mg", "Risperidona 2mg", "Carbonato de Litio 300mg",
                    "Enalapril 20mg", "Losartán 50mg", "Amlodipino 10mg", "Atenolol 50mg", "Bisoprolol 5mg",
                    "Hidroclorotiazida 25mg", "Furosemida 40mg", "Espironolactona 25mg", "Digoxina 0.25mg", "Warfarina 5mg",
                    "Glibenclamida 5mg", "Metformina 850mg", "Levotiroxina 100mcg", "Prednisona 20mg", "Dexametasona 4mg",
                    "Hidrocortisona Crema", "Fluticasona Spray", "Cetirizina 10mg", "Desloratadina 5mg", "Omeprazol 20mg",
                    "Pantoprazol 40mg", "Sucralfato 1g", "Loperamida 2mg", "Buscapina 10mg", "Simeticona 80mg",
                    "Naproxeno 500mg", "Tramadol 50mg", "Paracetamol 500mg", "Ibuprofeno 600mg", "Vitaminas B Complex",
                    "Artemetér + Lumefantrina (Antipalúdico)", "Quinina 300mg", "Doxiciclina 100mg (Antipalúdico)", "Amoxicilina + Ácido Clavulánico", "Salbutamol Inhalador",
                    "Insulina NPH", "Hidralazina 25mg", "Glibenclamida 5mg", "Albendazol 400mg", "Mebendazol 100mg"
                };
                double[] precios = { 2500, 8500, 4500, 12000, 3500, 2000, 1500, 5500, 15000, 7500, 12000, 5000, 9500, 18000, 14000, 22000, 6000, 45000, 35000, 28000, 3000, 4500, 6500, 4000, 15000, 2500, 1500, 8000, 12000, 25000, 2000, 3500, 11000, 5000, 4000, 6000, 18000, 3000, 12000, 1500, 9500, 14000, 2500, 5000, 4500, 4000, 18000, 500, 1200, 15000, 20000, 15000, 4000, 12000, 8000, 25000, 9000, 2000, 1500, 1500 };
                
                String mSql = "INSERT OR IGNORE INTO medicamentos (nombre, stock, precio, creador_id) VALUES (?, ?, ?, 1)";
                try (PreparedStatement ps = conn.prepareStatement(mSql)) {
                    for (int i = 0; i < meds.length; i++) {
                        ps.setString(1, meds[i]);
                        ps.setInt(2, (int)(Math.random() * 500) + 50);
                        ps.setDouble(3, precios[i]);
                        ps.executeUpdate();
                    }
                }
                System.out.println("✅ Inventario de 50 medicamentos listo.");
            }
        } catch (SQLException e) {
            System.err.println("Error al poblar datos: " + e.getMessage());
        }
    }
}
