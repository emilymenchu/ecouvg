# Mapa de centros: bloque 3 de 4

Base esperada: bloque 2 aplicado; el usuario confirmó el mapa visible, cinco puntos y el commit `e55ede2` subido a `feature/daniel-mapa-centros`.

## Comportamiento implementado

- Cada tarjeta incorpora **Ver en mapa**. Se habilita cuando el mapa terminó de cargar y existe un marcador para ese ID.
- Al pulsarlo, se centra el mapa con zoom 16, se abre la ventana del centro y se resalta su tarjeta.
- Cada marcador abre una ventana con nombre, dirección, horario, materiales y **Ver información completa**, que enlaza a la ficha existente.
- **Ver tarjeta en la lista** cierra la ventana y lleva el foco a la tarjeta correspondiente.
- Al cerrar una ventana se limpia su selección. Una recarga de datos limpia marcadores, botones y selección anteriores.
- Los centros sin coordenadas siguen visibles en el directorio, con el botón del mapa deshabilitado y un título explicativo. Una falla de carga también deshabilita esos botones.
- La búsqueda y el filtro de materiales mantienen el formulario GET existente. Al enviar o limpiar filtros se recargan la lista y el mapa con los mismos parámetros; no se agrega un segundo buscador ni una búsqueda asíncrona independiente.

## Diseño y accesibilidad

La selección se relaciona mediante IDs, no por nombre o posición en la lista. Los botones funcionan con teclado, el foco se mueve al mapa o a la tarjeta y se respeta la preferencia de movimiento reducido al desplazar la página. Las tarjetas tienen `tabindex=-1` para admitir foco programático sin agregar paradas adicionales al tabulador.

Las ventanas usan nodos DOM y `textContent`; los nombres y materiales de la base no se interpretan como HTML. Los enlaces se construyen con una ruta de Thymeleaf que conserva el contexto de la aplicación y un ID entero positivo válido. No se inventan horarios ni materiales cuando faltan.

Las ventanas tienen altura máxima con desplazamiento interno para que la información extensa pueda consultarse en móvil. No se modifican el modelo Java, el endpoint, los datos de Supabase ni las credenciales.

## Prueba manual

1. Reinicia la aplicación desde `ecouvg-mapa`, inicia sesión y abre `/centros`.
2. Pulsa un marcador: comprueba nombre, dirección, horario y materiales.
3. Pulsa **Ver información completa**: debe abrir la ficha de ese centro.
4. Vuelve a `/centros` y pulsa **Ver en mapa** en una tarjeta: el mapa debe centrarse en su centro y abrir la ventana correspondiente.
5. Dentro de la ventana pulsa **Ver tarjeta en la lista**: debes volver a esa tarjeta resaltada.
6. Abre otro marcador y luego cierra su ventana: el resaltado debe actualizarse y finalmente desaparecer.
7. Busca `cayala` (si existe en tus datos) y un material: verifica que tarjeta y punto correspondan al resultado. Limpia filtros para recuperar todos los centros.
8. Busca un texto sin coincidencias: no deben permanecer los puntos de la búsqueda anterior.
9. Prueba con la ventana angosta; revisa que puedas desplazar la información de la ventana emergente y alcanzar sus enlaces.
10. Para centros sin coordenadas, si existen en tu base, comprueba que el botón esté deshabilitado. No hace falta modificar datos reales para provocar este caso: también está cubierto con datos simulados.

La ficha individual aún mantiene el espacio reservado para su mapa; se implementa en el cuarto bloque.

## Pruebas automatizadas

Opcional, con Node.js 18 o posterior:

```bash
node --test src/test/js/mapa-centros.test.cjs
```

Se ejecutaron 24 pruebas: las 15 anteriores más 9 que cubren contenido de ventanas, enlaces, datos tratados como texto, selección por tarjeta, centros sin ubicación, apertura/cierre de ventanas, regreso a tarjeta, limpieza tras recargar y movimiento reducido.

Estas pruebas simulan DOM, Leaflet y peticiones. No sustituyen las comprobaciones de navegador, Spring Boot y Supabase en tu equipo. Se verificaron la sintaxis del JavaScript y la aplicación del parche contra la base del segundo bloque.

## Archivos y commit

Cambian `centros.html`, `mapa-centros.js`, `mapa-centros.css` y `mapa-centros.test.cjs`; se agrega este documento.

Mensaje sugerido: `feat(mapa): conectar marcadores con buscador y fichas`.

Después de verificar y subir este tercer commit, queda el cuarto: mapa individual, revisión final y preparación del pull request hacia `main`.
