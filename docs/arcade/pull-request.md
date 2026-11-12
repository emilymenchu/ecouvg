# Agregar Eco Arcade a EcoUVG

EcoUVG incorpora una sección para practicar la identificación de materiales con
Eco Run, Eco Snake y Eco Breakout, protagonizados por Eco la tortuga. Se accede
desde el menú inferior o lateral y requiere la sesión existente de la aplicación.

Los materiales se consultan mediante ServicioGuia desde PostgreSQL. La lógica de
los juegos usa clases con herencia y composición, separada de Canvas y del DOM.
Incluye controles de teclado y táctiles, pausa automática, reinicio, tres vidas,
mensajes educativos y récords de la pestaña. No modifica EcoPuntos ni premios.

No necesita migraciones ni dependencias adicionales. Requiere las categorías
plasticos, metales, papel y carton con ejemplos en la guía.

Validación:
- 22 pruebas de lógica JavaScript aprobadas.
- Sintaxis de módulos y renderizado de los tres tableros revisados.
- [ ] Ejecutar ServicioArcadeTest y ControladorArcadeTest con Java 25.
- [ ] Comprobar las tres partidas en navegador de escritorio y móvil.
- [ ] Confirmar acceso con sesión y carga de materiales desde Supabase.

Ajustes de jugabilidad: residuos con dibujos y confirmación de recogida; Eco Snake adopta el color del último acierto. Eco Breakout usa 50 bloques cuadrados, deja atravesar materiales de otras rondas y relanza la pelota si no hay progreso. Incluye pruebas de trayectorias, recuperación y victoria completa.
