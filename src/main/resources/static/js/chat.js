
const formulario = document.getElementById("form-chat");
const pregunta = document.getElementById("pregunta");
const mensajes = document.getElementById("mensajes");
const boton = document.getElementById("btn-enviar");

const token = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

function agregarMensaje(texto, tipo) {
    const mensaje = document.createElement("div");

    mensaje.classList.add("mensaje", tipo);
    mensaje.textContent = texto;

    mensajes.appendChild(mensaje);
    mensajes.scrollTop = mensajes.scrollHeight;

    return mensaje;
}

formulario.addEventListener("submit", async function(evento) {
    evento.preventDefault();

    const texto = pregunta.value.trim();

    if (texto === "") {
        return;
    }

    agregarMensaje(texto, "usuario");

    pregunta.value = "";
    boton.disabled = true;

    const mensajeEspera = agregarMensaje("EcoBot está escribiendo...", "bot");

    try {
        const respuesta = await fetch("/chat/preguntar", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: token
            },
            body: JSON.stringify({
                pregunta: texto
            })
        });

        if (!respuesta.ok) {
            throw new Error("Error al consultar EcoBot");
        }

        const datos = await respuesta.json();

        mensajeEspera.textContent = datos.respuesta;

    } catch (error) {
        mensajeEspera.textContent = "No pude conectarme con EcoBot. Intenta nuevamente :().";
        console.error(error);

    } finally {
        boton.disabled = false;
        pregunta.focus();
        mensajes.scrollTop = mensajes.scrollHeight;
    }
});
