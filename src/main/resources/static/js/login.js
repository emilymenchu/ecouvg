const botonVer = document.getElementById("verContrasena");

const campoContrasena = document.getElementById("contrasena");

botonVer.addEventListener("click", function () {

    const estaOculta = campoContrasena.type === "password";

    campoContrasena.type = estaOculta ? "text" : "password";

    botonVer.textContent = estaOculta ? "Ocultar" : "Mostrar";

});