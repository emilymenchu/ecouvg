const fotografia = document.getElementById("fotografia");
fotografia?.addEventListener("change", () => {
    const archivo = fotografia.files[0];
    fotografia.setCustomValidity(archivo && archivo.size > 4 * 1024 * 1024
        ? "La fotografía no debe superar los 4 MB." : "");
    if (!fotografia.checkValidity()) fotografia.reportValidity();
});
