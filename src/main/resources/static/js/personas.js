for (const foto of document.querySelectorAll("[data-foto-persona]")) {
    foto.addEventListener("load", () => { foto.hidden = false; });
    foto.addEventListener("error", () => { foto.hidden = true; });
    if (foto.complete && foto.currentSrc && foto.naturalWidth === 0) foto.hidden = true;
}
