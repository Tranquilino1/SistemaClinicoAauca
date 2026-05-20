-- Schema de la Base de Datos Clínica Aauca
-- v2.0 — Roles: Admin, Médico, Enfermero (sin Recepción)

CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    nombre_completo TEXT NOT NULL,
    rol TEXT CHECK(rol IN ('Admin', 'Médico', 'Enfermero')) NOT NULL
);

-- =====================================================================
-- CREDENCIALES DE ACCESO AL SISTEMA
-- =====================================================================
-- admin     / admin123
-- medico    / medico123
-- enfermero / enfermero123
-- =====================================================================

INSERT OR IGNORE INTO usuarios (username, password, nombre_completo, rol)
VALUES ('admin', '$2a$12$o1yLTe.4SRKOaWLVVmsoueRzGsjiJ3Xp0TxJ3KV0K9zOhmUbqM2r6', 'Administrador Principal', 'Admin');

INSERT OR IGNORE INTO usuarios (username, password, nombre_completo, rol)
VALUES ('medico', '$2a$12$nmd3s0HBqqzA9YR1ZW6vI.8bfSResIOTcgZa81XTgSaqG8KhjC5lS', 'Dr. Marcos Nguema Edu', 'Médico');

INSERT OR IGNORE INTO usuarios (username, password, nombre_completo, rol)
VALUES ('enfermero', '$2a$12$nmd3s0HBqqzA9YR1ZW6vI.8bfSResIOTcgZa81XTgSaqG8KhjC5lS', 'Enf. Roberto Díaz Mba', 'Enfermero');

CREATE TABLE IF NOT EXISTS pacientes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_completo TEXT NOT NULL,
    es_estudiante BOOLEAN NOT NULL DEFAULT 0,
    fecha_nacimiento TEXT
);

CREATE TABLE IF NOT EXISTS configuracion (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    nombre_clinica TEXT DEFAULT 'Clínica AAUCA',
    direccion TEXT,
    telefono TEXT,
    email TEXT,
    moneda TEXT DEFAULT 'FCFA',
    precio_consulta REAL DEFAULT 5000,
    precio_hospitalizacion REAL DEFAULT 15000,
    logo_path TEXT
);

INSERT OR IGNORE INTO configuracion (id, nombre_clinica, precio_consulta, precio_hospitalizacion) 
VALUES (1, 'Clínica AAUCA', 5000, 15000);

CREATE TABLE IF NOT EXISTS hospitalizaciones (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    paciente_id INTEGER NOT NULL,
    estado TEXT NOT NULL DEFAULT 'ingresado',
    seguimiento TEXT,
    fecha_ingreso TEXT NOT NULL,
    motivo TEXT,
    FOREIGN KEY(paciente_id) REFERENCES pacientes(id)
);

CREATE TABLE IF NOT EXISTS consultas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    paciente_id INTEGER NOT NULL,
    fecha TEXT NOT NULL,
    motivo_consulta TEXT,
    diagnostico TEXT,
    tratamiento TEXT,
    antecedentes TEXT,
    receta TEXT,
    factura TEXT,
    estado TEXT DEFAULT 'guardado',
    FOREIGN KEY(paciente_id) REFERENCES pacientes(id)
);

CREATE TABLE IF NOT EXISTS medicamentos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    precio REAL DEFAULT 0,
    creador_id INTEGER NOT NULL,
    FOREIGN KEY(creador_id) REFERENCES usuarios(id)
);

-- Datos de Prueba para Pacientes
INSERT OR IGNORE INTO pacientes (id, nombre_completo, es_estudiante, fecha_nacimiento) VALUES 
(1, 'Pelayo Nvulu Nvulu (Informática)', 1, '2001-05-15'),
(2, 'Carlota Raquel (Medicina)', 1, '2002-11-20'),
(3, 'Mauricio Edu (Economía)', 1, '1999-03-10'),
(4, 'Ángel Asimi (Ing. Eléctrica)', 1, '2000-01-30'),
(5, 'Tranquilino Maba (Agroindustrial)', 1, '2003-08-05'),
(6, 'Profe. Diosdado Esono (Docente)', 0, '1975-12-12'),
(7, 'Evaristo Olo (Ing. Civil)', 1, '2004-03-10'),
(8, 'Natividad Eseng (Mecánica)', 1, '2002-07-22'),
(9, 'Patricio Mba (Administración)', 1, '2001-09-14'),
(10, 'Beatriz Nguema (Interpretación)', 1, '2003-02-28');

-- Datos iniciales: Medicamentos (50 items)
INSERT OR IGNORE INTO medicamentos (id, nombre, stock, precio, creador_id) VALUES 
(1, 'Amoxicilina 500mg', 500, 2500, 1),
(2, 'Azitromicina 500mg', 200, 8500, 1),
(3, 'Ciprofloxacino 500mg', 150, 4500, 1),
(4, 'Claritromicina 500mg', 100, 12000, 1),
(5, 'Doxiciclina 100mg', 300, 3500, 1),
(6, 'Metronidazol 500mg', 400, 2000, 1),
(7, 'Penicilina V 250mg', 800, 1500, 1),
(8, 'Cefalexina 500mg', 350, 5500, 1),
(9, 'Levofloxacino 500mg', 120, 15000, 1),
(10, 'Nitrofurantoína 100mg', 250, 7500, 1),
(11, 'Alprazolam 0.5mg', 150, 12000, 1),
(12, 'Diazepam 10mg', 200, 5000, 1),
(13, 'Lorazepam 2mg', 180, 9500, 1),
(14, 'Sertralina 50mg', 250, 18000, 1),
(15, 'Fluoxetina 20mg', 300, 14000, 1),
(16, 'Escitalopram 10mg', 120, 22000, 1),
(17, 'Amitriptilina 25mg', 200, 6000, 1),
(18, 'Quetiapina 100mg', 100, 45000, 1),
(19, 'Risperidona 2mg', 150, 35000, 1),
(20, 'Carbonato de Litio 300mg', 90, 28000, 1),
(21, 'Enalapril 20mg', 500, 3000, 1),
(22, 'Losartán 50mg', 600, 4500, 1),
(23, 'Amlodipino 10mg', 400, 6500, 1),
(24, 'Atenolol 50mg', 300, 4000, 1),
(25, 'Bisoprolol 5mg', 200, 15000, 1),
(26, 'Hidroclorotiazida 25mg', 500, 2500, 1),
(27, 'Furosemida 40mg', 500, 1500, 1),
(28, 'Espironolactona 25mg', 150, 8000, 1),
(29, 'Digoxina 0.25mg', 100, 12000, 1),
(30, 'Warfarina 5mg', 80, 25000, 1),
(31, 'Glibenclamida 5mg', 400, 2000, 1),
(32, 'Metformina 850mg', 600, 3500, 1),
(33, 'Levotiroxina 100mcg', 450, 11000, 1),
(34, 'Prednisona 20mg', 300, 5000, 1),
(35, 'Dexametasona 4mg', 400, 4000, 1),
(36, 'Hidrocortisona Crema', 250, 6000, 1),
(37, 'Fluticasona Spray', 100, 18000, 1),
(38, 'Cetirizina 10mg', 500, 3000, 1),
(39, 'Desloratadina 5mg', 200, 12000, 1),
(40, 'Omeprazol 20mg', 1000, 1500, 1),
(41, 'Pantoprazol 40mg', 300, 9500, 1),
(42, 'Sucralfato 1g', 150, 14000, 1),
(43, 'Loperamida 2mg', 400, 2500, 1),
(44, 'Buscapina 10mg', 300, 5000, 1),
(45, 'Simeticona 80mg', 200, 4500, 1),
(46, 'Naproxeno 500mg', 400, 4000, 1),
(47, 'Tramadol 50mg', 150, 18000, 1),
(48, 'Paracetamol 500mg', 2000, 500, 1),
(49, 'Ibuprofeno 600mg', 1200, 1200, 1),
(50, 'Vitaminas B Complex', 500, 15000, 1);
