const formulario = document.getElementById("form-chat");
const pregunta = document.getElementById("pregunta");
const mensajes = document.getElementById("mensajes");
const boton = document.getElementById("btn-enviar");
const estado = document.getElementById("estado-chat");
const sugerencias = document.getElementById("sugerencias");
const contador = document.getElementById("contador-chat");
const token = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
let esperando = false;
let conversacionId = formulario.dataset.conversacionId || null;
const listaConversaciones = document.getElementById("lista-conversaciones");

async function asegurarConversacion(signal) {
    if (conversacionId) return;
    const respuesta = await fetch(formulario.dataset.crearUrl, {
        method: "POST", headers: {[csrfHeader]: token}, signal
    });
    if (!respuesta.ok || respuesta.redirected) throw new Error("No se pudo crear la conversación");
    const datos = await respuesta.json();
    if (typeof datos.id !== "string") throw new Error("Conversación inválida");
    conversacionId = datos.id;
    const url = new URL(listaConversaciones.dataset.chatUrl, window.location.origin);
    url.searchParams.set("conversacion", conversacionId);
    window.history.replaceState(null, "", url);
}

function actualizarHistorial(texto) {
    let enlace = listaConversaciones.querySelector(`[data-chat-id="${conversacionId}"]`);
    if (!enlace) {
        enlace = document.createElement("a");
        enlace.dataset.chatId = conversacionId;
        const url = new URL(listaConversaciones.dataset.chatUrl, window.location.origin);
        url.searchParams.set("conversacion", conversacionId);
        enlace.href = url;
        listaConversaciones.prepend(enlace);
        document.getElementById("historial-vacio")?.remove();
    }
    if (!enlace.textContent || enlace.textContent === "Nueva conversación") {
        enlace.textContent = Array.from(texto.replace(/\s+/g, " ")).slice(0, 80).join("");
    }
    for (const otro of listaConversaciones.querySelectorAll("a")) otro.removeAttribute("aria-current");
    enlace.setAttribute("aria-current", "page");
}

function prepararFoto(foto) {
    foto.addEventListener("load", () => { foto.hidden = false; });
    foto.addEventListener("error", () => { foto.hidden = true; });
}


function bajarConversacion() {
    mensajes.scrollTop = mensajes.scrollHeight;
}

function actualizarEntrada() {
    contador.textContent = `${pregunta.value.length} / 1000`;
    pregunta.style.height = "auto";
    pregunta.style.height = `${Math.min(pregunta.scrollHeight, 130)}px`;
}

function agregarMensaje(texto, tipo) {
    const fila = document.createElement("article");
    fila.classList.add("fila-mensaje", tipo);
    const avatar = document.getElementById(tipo === "bot" ? "avatar-eco" : "avatar-usuario").content.cloneNode(true);
    const fotoUsuario = avatar.querySelector(".avatar-usuario img");
    if (fotoUsuario) {
        // El contenido de <template> aún no ha cargado la imagen al clonarlo.
        // Esperar sus eventos evita ocultar una foto válida antes de insertarla.
        prepararFoto(fotoUsuario);
    }
    fila.appendChild(avatar);
    const contenido = document.createElement("div");
    contenido.className = "contenido-mensaje";
    const autor = document.createElement("span");
    autor.className = "autor-chat";
    autor.textContent = tipo === "bot" ? "Eco" : "Tú";
    const burbuja = document.createElement("div");
    burbuja.className = "burbuja-chat";
    // Los mensajes del usuario se muestran literalmente.
    burbuja.textContent = texto;
    contenido.append(autor, burbuja);
    fila.appendChild(contenido);
    mensajes.appendChild(fila);
    if (tipo === "usuario") agregarHora(contenido);
    bajarConversacion();
    return contenido;
}

function formatearRespuesta(destino, texto) {
    destino.textContent = "";
    // Formato inline de Markdown. Se crean nodos; nunca se interpreta HTML de la IA.
    const patron = /\\([\\*`])|`([^`\n]+)`|\*\*\*(\S(?:[^\n]*?\S)?)\*\*\*|\*\*(\S(?:[^\n]*?\S)?)\*\*|\*([^*\s](?:[^*\n]*?[^*\s])?)\*/g;
    let posicion = 0;
    for (const coincidencia of texto.matchAll(patron)) {
        destino.appendChild(document.createTextNode(texto.slice(posicion, coincidencia.index)));
        if (coincidencia[1]) {
            destino.appendChild(document.createTextNode(coincidencia[1]));
        } else {
            const elemento = document.createElement(coincidencia[2] ? "code" : coincidencia[5] ? "em" : "strong");
            if (coincidencia[3]) {
                const cursiva = document.createElement("em");
                cursiva.textContent = coincidencia[3];
                elemento.appendChild(cursiva);
            } else {
                elemento.textContent = coincidencia[2] || coincidencia[4] || coincidencia[5];
            }
            destino.appendChild(elemento);
        }
        posicion = coincidencia.index + coincidencia[0].length;
    }
    destino.appendChild(document.createTextNode(texto.slice(posicion)));
}

function agregarHora(contenido) {
    const hora = document.createElement("time");
    const ahora = new Date();
    hora.className = "hora-chat";
    hora.dateTime = ahora.toISOString();
    hora.textContent = ahora.toLocaleTimeString("es", {hour: "2-digit", minute: "2-digit"});
    contenido.appendChild(hora);
}

async function enviar(texto, respuestaAnterior) {
    texto = texto.trim();
    if (esperando || !texto || texto.length > 1000) return;
    esperando = true;
    boton.disabled = true;
    sugerencias.hidden = true;
    if (!respuestaAnterior) {
        document.getElementById("bienvenida-chat")?.remove();
        agregarMensaje(texto, "usuario");
        pregunta.value = "";
        actualizarEntrada();
    }
    const contenido = respuestaAnterior || agregarMensaje("", "bot");
    const solicitudId = contenido.dataset.solicitudId || crypto.randomUUID();
    contenido.dataset.solicitudId = solicitudId;
    contenido.querySelector(".reintentar-chat")?.remove();
    contenido.querySelector(".hora-chat")?.remove();
    const burbuja = contenido.querySelector(".burbuja-chat");
    burbuja.textContent = "";
    const puntos = document.createElement("span");
    puntos.className = "escribiendo";
    puntos.setAttribute("aria-hidden", "true");
    for (let i = 0; i < 3; i++) puntos.appendChild(document.createElement("span"));
    burbuja.appendChild(puntos);
    estado.textContent = "Eco está escribiendo…";
    bajarConversacion();
    const controlador = new AbortController();
    const limite = setTimeout(() => controlador.abort(), 45000);
    try {
        if (!token || !csrfHeader) throw new Error("Sesión no disponible");
        await asegurarConversacion(controlador.signal);
        const respuesta = await fetch(formulario.dataset.url, {
            method: "POST",
            headers: {"Content-Type": "application/json", [csrfHeader]: token},
            body: JSON.stringify({pregunta: texto, conversacionId, solicitudId}),
            signal: controlador.signal
        });
        if (!respuesta.ok || respuesta.redirected) throw new Error("No se recibió una respuesta");
        const datos = await respuesta.json();
        if (typeof datos.respuesta !== "string" || !datos.respuesta.trim()) throw new Error("Respuesta vacía");
        formatearRespuesta(burbuja, datos.respuesta);
        agregarHora(contenido);
        actualizarHistorial(texto);
        estado.textContent = "Guardado";
    } catch (error) {
        burbuja.textContent = "No se pudo obtener la respuesta. Puedes reintentar o recargar la página si tu sesión terminó.";
        const reintentar = document.createElement("button");
        reintentar.type = "button";
        reintentar.className = "reintentar-chat";
        reintentar.textContent = "Reintentar";
        reintentar.addEventListener("click", () => enviar(texto, contenido));
        contenido.appendChild(reintentar);
        estado.textContent = "No se recibió la respuesta de Eco.";
    } finally {
        clearTimeout(limite);
        esperando = false;
        boton.disabled = false;
        pregunta.focus();
        bajarConversacion();
    }
}

formulario.addEventListener("submit", evento => {
    evento.preventDefault();
    enviar(pregunta.value);
});
pregunta.addEventListener("input", actualizarEntrada);
pregunta.addEventListener("keydown", evento => {
    if (evento.key === "Enter" && !evento.shiftKey && !evento.isComposing) {
        evento.preventDefault();
        formulario.requestSubmit();
    }
});
for (const sugerencia of sugerencias.querySelectorAll("button[data-pregunta]")) {
    sugerencia.addEventListener("click", () => enviar(sugerencia.dataset.pregunta));
}

// Recuperar el formato y las fotos de mensajes guardados sin interpretar HTML del modelo.
for (const respuesta of document.querySelectorAll("[data-respuesta-guardada]")) {
    formatearRespuesta(respuesta, respuesta.textContent);
}
for (const fecha of document.querySelectorAll("[data-fecha-guardada]")) {
    fecha.textContent = new Date(fecha.dateTime).toLocaleTimeString("es", {hour: "2-digit", minute: "2-digit"});
}
for (const foto of document.querySelectorAll(".mensajes .avatar-usuario img")) {
    prepararFoto(foto);
    if (foto.complete && foto.naturalWidth === 0) foto.hidden = true;
}
bajarConversacion();
