/* ==========================================================================
   INTERACTIVIDAD DE LANDING PAGE - CLINICA AAUCA
   ========================================================================== */

document.addEventListener("DOMContentLoaded", () => {
  inicializarNavegacionMockup();
  inicializarScrollSuave();
  inicializarTimelineInteractivo();
  inicializarContadorSimulado();
  registrarDescargasEventos();
});

// --- 1. NAVEGACIÓN EN EL MOCKUP INTERACTIVO DE LA INTERFAZ ---
function inicializarNavegacionMockup() {
  const menuButtons = document.querySelectorAll(".m-menu-item");
  const tabContents = document.querySelectorAll(".mockup-tab-content");
  
  menuButtons.forEach(button => {
    button.addEventListener("click", () => {
      const targetTab = button.getAttribute("data-tab");
      
      // Desactivar botones anteriores
      menuButtons.forEach(btn => btn.classList.remove("active"));
      button.classList.add("active");
      
      // Ocultar pestañas anteriores con animación suave
      tabContents.forEach(tab => {
        tab.classList.remove("active");
      });
      
      // Mostrar la pestaña seleccionada
      const targetEl = document.getElementById(targetTab);
      if (targetEl) {
        targetEl.classList.add("active");
      }
    });
  });
}

// --- 2. SMOOTH SCROLLING Y RESALTADO DE CABECERA ---
function inicializarScrollSuave() {
  const links = document.querySelectorAll(".nav-links a, .footer-link-group a[href^='#']");
  const sections = document.querySelectorAll("section");
  
  // Desplazamiento suave al hacer clic en enlaces
  links.forEach(link => {
    link.addEventListener("click", (e) => {
      const targetId = link.getAttribute("href");
      if (targetId.startsWith("#")) {
        e.preventDefault();
        const targetSection = document.getElementById(targetId.substring(1));
        if (targetSection) {
          const navbarHeight = 90; // compensar la cabecera fija
          const offsetTop = targetSection.offsetTop - navbarHeight;
          window.scrollTo({
            top: offsetTop,
            behavior: "smooth"
          });
        }
      }
    });
  });
  
  // Resaltar sección activa en la barra de navegación al hacer scroll
  window.addEventListener("scroll", () => {
    let currentSectionId = "inicio";
    const scrollPos = window.scrollY + 120; // margen superior
    
    sections.forEach(section => {
      if (section.offsetTop <= scrollPos && (section.offsetTop + section.offsetHeight) > scrollPos) {
        currentSectionId = section.getAttribute("id");
      }
    });
    
    // Sincronizar clases en cabecera
    const navItems = document.querySelectorAll(".nav-links a");
    navItems.forEach(item => {
      item.classList.remove("active");
      if (item.getAttribute("href") === `#${currentSectionId}`) {
        item.classList.add("active");
      }
    });
  });
}

// --- 3. LÍNEA DE TIEMPO INTERACTIVA AL DESPLAZARSE ---
function inicializarTimelineInteractivo() {
  const timelineSection = document.getElementById("instalacion");
  const fillBar = document.getElementById("timeline-progress-fill");
  const nodes = document.querySelectorAll(".timeline-node");
  
  if (!timelineSection || !fillBar) return;
  
  window.addEventListener("scroll", () => {
    const sectionTop = timelineSection.offsetTop;
    const sectionHeight = timelineSection.offsetHeight;
    const windowScroll = window.scrollY + window.innerHeight / 2; // punto medio de la pantalla
    
    if (windowScroll >= sectionTop && windowScroll <= (sectionTop + sectionHeight)) {
      // Calcular porcentaje completado en la sección
      const scrolledIn = windowScroll - sectionTop;
      let percent = Math.min((scrolledIn / (sectionHeight - 150)) * 100, 100);
      percent = Math.max(percent, 0);
      
      fillBar.style.height = `${percent}%`;
      
      // Activar nodos secuencialmente basado en el porcentaje
      if (percent < 33) {
        nodes[0].classList.add("active");
        nodes[1].classList.remove("active");
        nodes[2].classList.remove("active");
      } else if (percent >= 33 && percent < 66) {
        nodes[0].classList.add("active");
        nodes[1].classList.add("active");
        nodes[2].classList.remove("active");
      } else if (percent >= 66) {
        nodes[0].classList.add("active");
        nodes[1].classList.add("active");
        nodes[2].classList.add("active");
      }
    } else if (windowScroll < sectionTop) {
      fillBar.style.height = "0%";
      nodes[0].classList.add("active");
      nodes[1].classList.remove("active");
      nodes[2].classList.remove("active");
    } else if (windowScroll > (sectionTop + sectionHeight)) {
      fillBar.style.height = "100%";
      nodes.forEach(n => n.classList.add("active"));
    }
  });
}

// --- 4. CONTADOR SIMULADO DE DESCARGAS DIARIAS ---
function inicializarContadorSimulado() {
  const counterEl = document.getElementById("simulated-downloads");
  if (!counterEl) return;
  
  let currentDownloads = 1248;
  
  // Incrementar aleatoriamente cada 5 a 10 segundos
  setInterval(() => {
    const increment = Math.floor(Math.random() * 3) + 1; // suma de 1 a 3 descargas
    currentDownloads += increment;
    
    // Animación suave de cambio numérico
    counterEl.style.opacity = "0.5";
    counterEl.style.transform = "scale(1.05)";
    
    setTimeout(() => {
      counterEl.innerText = currentDownloads.toLocaleString();
      counterEl.style.opacity = "1";
      counterEl.style.transform = "scale(1)";
    }, 200);
    
  }, 7000);
}

// --- 5. LOG DE EVENTOS DE DESCARGA PARA MONITOREO ---
function registrarDescargasEventos() {
  const setupButtons = document.querySelectorAll("a[href*='Setup.exe']");
  const portableButtons = document.querySelectorAll("a[href*='Final.exe']");
  
  setupButtons.forEach(btn => {
    btn.addEventListener("click", () => {
      crearNotificacionToast("Iniciando descarga: ClinicaAauca_Setup.exe (Instalador)", "success");
    });
  });
  
  portableButtons.forEach(btn => {
    btn.addEventListener("click", () => {
      crearNotificacionToast("Iniciando descarga: ClinicaAauca_Portable.exe (Portable)", "info");
    });
  });
  
  // Notificar descarga del manual
  const manualLinks = document.querySelectorAll("a[href*='.pdf']");
  manualLinks.forEach(link => {
    link.addEventListener("click", () => {
      crearNotificacionToast("Abriendo Manual de Usuario Oficial (PDF)", "success");
    });
  });
}

// --- 6. TOAST DE NOTIFICACIONES DINÁMICAS ---
function crearNotificacionToast(mensaje, tipo = "success") {
  // Crear contenedor de notificaciones si no existe
  let container = document.querySelector(".toast-container");
  if (!container) {
    container = document.createElement("div");
    container.className = "toast-container";
    // Copiar estilos de toast container al vuelo para mantener el sitio autocontenido
    container.setAttribute("style", "position:fixed; bottom:24px; right:24px; z-index:9999; display:flex; flex-direction:column; gap:10px;");
    document.body.appendChild(container);
  }
  
  const toast = document.createElement("div");
  toast.className = `toast toast-${tipo}`;
  
  // Estilo inline premium para el toast independiente
  let borderColor = "var(--color-cyan)";
  if (tipo === "info") borderColor = "var(--color-violet)";
  if (tipo === "warning") borderColor = "var(--color-yellow)";
  
  toast.setAttribute("style", `
    padding: 14px 20px;
    background: rgba(11, 15, 25, 0.9);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border-left: 4px solid ${borderColor};
    border-top: 1px solid var(--border-color);
    border-right: 1px solid var(--border-color);
    border-bottom: 1px solid var(--border-color);
    border-radius: 8px;
    color: var(--text-primary);
    font-size: 0.82rem;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 12px;
    box-shadow: 0 10px 30px rgba(0,0,0,0.5);
    transition: all 0.3s ease;
    min-width: 280px;
    animation: slideInRight 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  `);
  
  // Animación del keyframe incrustada
  if (!document.getElementById("toast-animation-styles")) {
    const styleSheet = document.createElement("style");
    styleSheet.id = "toast-animation-styles";
    styleSheet.innerHTML = `
      @keyframes slideInRight {
        from { transform: translateX(120%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
      }
    `;
    document.head.appendChild(styleSheet);
  }
  
  let icon = "check_circle";
  if (tipo === "info") icon = "box";
  
  toast.innerHTML = `
    <span class="material-symbols-outlined" style="color:${borderColor}">${icon}</span>
    <span style="flex-grow:1;">${mensaje}</span>
    <button class="toast-close" style="background:transparent; border:none; color:var(--text-muted); cursor:pointer; display:flex;">
      <span class="material-symbols-outlined" style="font-size:16px;">close</span>
    </button>
  `;
  
  container.appendChild(toast);
  
  // Desvanecimiento automático después de 4 segundos
  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateX(50px)";
    setTimeout(() => {
      toast.remove();
    }, 300);
  }, 4000);
  
  // Cierre manual
  toast.querySelector(".toast-close").addEventListener("click", () => {
    toast.remove();
  });
}
