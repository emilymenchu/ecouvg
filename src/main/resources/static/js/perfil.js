const dialogoFoto = document.getElementById("dialogo-foto");
const abrirFoto = document.getElementById("abrir-foto");
const cerrarFoto = document.getElementById("cerrar-foto");
const archivoFoto = document.getElementById("fotografia");
const vistaPreviaFoto = document.getElementById("vista-previa-foto");
let urlVistaPrevia;

abrirFoto?.addEventListener("click", () => dialogoFoto.showModal());
cerrarFoto?.addEventListener("click", () => dialogoFoto.close());
dialogoFoto?.addEventListener("close", () => abrirFoto.focus());

archivoFoto?.addEventListener("change", () => {
    if (urlVistaPrevia) URL.revokeObjectURL(urlVistaPrevia);
    vistaPreviaFoto.hidden = true;
    vistaPreviaFoto.removeAttribute("src");
    archivoFoto.setCustomValidity("");
    const foto = archivoFoto.files[0];
    if (!foto) return;
    if (foto.size > 5 * 1024 * 1024) {
        archivoFoto.setCustomValidity("La fotografía no debe superar los 5 MB.");
        archivoFoto.reportValidity();
        return;
    }
    urlVistaPrevia = URL.createObjectURL(foto);
    vistaPreviaFoto.src = urlVistaPrevia;
    vistaPreviaFoto.hidden = false;
});

const fotoActual = document.getElementById("foto-actual");
function mostrarErrorFoto() {
    fotoActual.hidden = true;
    document.getElementById("avatar-predeterminado").hidden = false;
    document.getElementById("error-carga-foto").hidden = false;
}
if (fotoActual) {
    fotoActual.addEventListener("error", mostrarErrorFoto);
    if (fotoActual.complete && fotoActual.naturalWidth === 0) mostrarErrorFoto();
}
