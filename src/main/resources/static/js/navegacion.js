const menuLateral = document.getElementById("menu-lateral");
const abrirMenu = document.getElementById("abrir-menu");
const cerrarMenu = document.getElementById("cerrar-menu");

abrirMenu.addEventListener("click", () => {
    menuLateral.showModal();
    abrirMenu.setAttribute("aria-expanded", "true");
    document.body.classList.add("menu-abierto");
});

cerrarMenu.addEventListener("click", () => {
    menuLateral.close();
});

// Cerrar al hacer clic fuera del panel.
menuLateral.addEventListener("click", (evento) => {
    const limites = menuLateral.getBoundingClientRect();

    const fueraDelPanel =
        evento.clientX < limites.left ||
        evento.clientX > limites.right ||
        evento.clientY < limites.top ||
        evento.clientY > limites.bottom;

    if (evento.target === menuLateral && fueraDelPanel) {
        menuLateral.close();
    }
});

// Escape también cierra el diálogo.
menuLateral.addEventListener("close", () => {
    abrirMenu.setAttribute("aria-expanded", "false");
    document.body.classList.remove("menu-abierto");
    abrirMenu.focus();
});
