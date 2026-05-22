const fs = require('fs');
const path = require('path');

// Cargar el token desde variables de entorno o desde el argumento de línea de comandos para seguridad
const TOKEN = process.env.GITHUB_TOKEN || process.argv[2];
const OWNER = 'Tranquilino1';
const REPO = 'SistemaClinicoAauca';
const FILE_PATH = path.join(__dirname, 'ClinicaAauca_Setup.exe');
const FILE_NAME = 'ClinicaAauca_Setup.exe';

async function main() {
    try {
        if (!TOKEN) {
            throw new Error("No se ha proporcionado el token de acceso de GitHub (GITHUB_TOKEN).\nUso: $env:GITHUB_TOKEN='tu_token'; node upload_setup.js o bien node upload_setup.js tu_token");
        }
        console.log(`Buscando la versión de lanzamiento (Release) para ${OWNER}/${REPO}...`);
        
        // 1. Obtener los releases de la API de GitHub
        const releasesRes = await fetch(`https://api.github.com/repos/${OWNER}/${REPO}/releases`, {
            headers: {
                'Authorization': `Bearer ${TOKEN}`,
                'Accept': 'application.vnd.github+json',
                'X-GitHub-Api-Version': '2022-11-28',
                'User-Agent': 'NodeJS-Uploader'
            }
        });
        
        if (!releasesRes.ok) {
            throw new Error(`Error al obtener releases: ${releasesRes.status} ${await releasesRes.text()}`);
        }
        
        const releases = await releasesRes.json();
        if (releases.length === 0) {
            throw new Error("No se encontró ningún Release publicado en el repositorio.");
        }
        
        // Buscar el release v1.0.0 o el último
        const release = releases.find(r => r.tag_name === 'v1.0.0') || releases[0];
        console.log(`Encontrado el Release: ${release.name} (Tag: ${release.tag_name})`);
        
        // 2. Verificar si ya existe un asset con el mismo nombre y eliminarlo si es necesario
        const existingAsset = release.assets.find(a => a.name === FILE_NAME);
        if (existingAsset) {
            console.log(`Se detectó un asset existente llamado "${FILE_NAME}" (ID: ${existingAsset.id}). Eliminándolo...`);
            const deleteRes = await fetch(`https://api.github.com/repos/${OWNER}/${REPO}/releases/assets/${existingAsset.id}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${TOKEN}`,
                    'Accept': 'application.vnd.github+json',
                    'X-GitHub-Api-Version': '2022-11-28',
                    'User-Agent': 'NodeJS-Uploader'
                }
            });
            
            if (deleteRes.status === 204) {
                console.log("Asset anterior eliminado exitosamente.");
            } else {
                throw new Error(`Fallo al eliminar el asset antiguo: ${deleteRes.status} ${await deleteRes.text()}`);
            }
        }
        
        // 3. Subir el nuevo archivo
        console.log(`Subiendo "${FILE_NAME}" al Release (${FILE_PATH})...`);
        const fileStats = fs.statSync(FILE_PATH);
        const fileStream = fs.createReadStream(FILE_PATH);
        
        const uploadUrl = release.upload_url.replace('{?name,label}', `?name=${encodeURIComponent(FILE_NAME)}`);
        
        const uploadRes = await fetch(uploadUrl, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${TOKEN}`,
                'Content-Type': 'application/octet-stream',
                'Content-Length': fileStats.size,
                'User-Agent': 'NodeJS-Uploader'
            },
            body: fileStream,
            duplex: 'half' // Requerido en Node para subir streams vía fetch
        });
        
        if (!uploadRes.ok) {
            throw new Error(`Error en la subida: ${uploadRes.status} ${await uploadRes.text()}`);
        }
        
        const uploadData = await uploadRes.json();
        console.log(`\n========================================================================`);
        console.log(`✅ ¡Éxito! El instalador se ha subido correctamente.`);
        console.log(`========================================================================`);
        console.log(`Nombre del archivo: ${uploadData.name}`);
        console.log(`Tamaño: ${(uploadData.size / (1024 * 1024)).toFixed(2)} MB`);
        console.log(`Descarga pública: ${uploadData.browser_download_url}`);
        console.log(`========================================================================\n`);
        
    } catch (error) {
        console.error("❌ Ocurrió un error crítico durante el proceso:", error.message);
        process.exit(1);
    }
}

main();
