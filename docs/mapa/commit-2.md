# Mapa de centros: bloque 2 de 4

## Resultado

`/centros` (y su alias `/mapa`) muestra ahora un mapa Leaflet 1.9.4 con el fondo de OpenStreetMap y los centros recibidos desde `/api/centros/mapa`.

- Sustituye el espacio reservado del directorio, manteniendo el buscador y las tarjetas.
- Dibuja marcadores solo para coordenadas válidas y muestra el nombre al pasar sobre un punto.
- Ajusta el encuadre para todos los puntos; un único punto se muestra con zoom 15. El botón "Ver todos los puntos" restaura el encuadre.
- Conserva la lista para centros sin coordenadas válidas y explica cuántos no se ubican.
- Transmite los filtros de la página mediante la URL generada por Thymeleaf. El formulario todavía recarga la página, como antes; la interacción tarjeta-marcador y las fichas emergentes se desarrollan en el bloque 3.
- Muestra avisos si falla Leaflet, el fondo de OpenStreetMap, la API, la sesión o una consulta tarda más de 12 segundos.
- Ajusta la altura a móvil y evita que el mapa se superponga a la navegación inferior.

La ficha individual `/centros/{id}` conserva por ahora su espacio reservado: su mapa se completa en el bloque 4. No se calculan rutas o tiempos de viaje y no se solicita geolocalización.

## Archivos

| Archivo | Función |
|---|---|
| `src/main/resources/templates/centros.html` | Contenedor del mapa, dependencias y URL de datos con los filtros actuales. |
| `src/main/resources/static/js/mapa-centros.js` | Consulta la API autenticada, crea el mapa y los marcadores, controla errores y reintentos. |
| `src/main/resources/static/css/mapa-centros.css` | Altura, controles, avisos y ajustes para móvil. |
| `src/test/js/mapa-centros.test.cjs` | Pruebas del comportamiento con dependencias simuladas. |

No cambia Java, el esquema PostgreSQL ni las credenciales. El JavaScript está en `static/js`, desde donde Spring lo sirve como `/js/mapa-centros.js`.

## Dependencias y datos

Leaflet 1.9.4 se carga desde `unpkg.com` con versión fija e integridad SRI. Es la versión estable indicada en https://leafletjs.com/download.html; se utiliza su API `L`, no la API de la versión 2 alfa.

El fondo solicita las teselas visibles de `https://tile.openstreetmap.org/{z}/{x}/{y}.png`. Se mantiene atribución visible y el comportamiento normal de caché del navegador; no hay descarga masiva ni funcionalidad de mapas sin conexión. Política oficial: https://operations.osmfoundation.org/policies/tiles/.

Se necesita conexión desde el navegador a `unpkg.com` y `tile.openstreetmap.org`. Si una red institucional bloquea alguno, el aviso permite identificarlo; no implica que Supabase haya fallado. No hace falta una cuenta ni una clave de API para esta configuración. La URL del fondo se declara en el atributo `data-tiles-url` para facilitar un cambio de proveedor, que requeriría revisar también su atribución y límites.

La vista inicial [14.60, -90.50] es una referencia general de Ciudad de Guatemala, no una coordenada atribuida a la UVG. Al cargar los centros se ajusta a sus ubicaciones reales. Los nombres se insertan con `textContent` para que no se interpreten como HTML.

## Comprobar en tu computadora

1. Confirma que el commit 1 está aplicado y que `/api/centros/mapa` responde tras iniciar sesión.
2. Aplica el parche del bloque 2 a la rama `feature/daniel-mapa-centros`.
3. Reinicia la aplicación desde **ecouvg-mapa** y abre `http://localhost:8080/centros`.
4. Recarga sin caché si aún ves el espacio reservado. Deben verse el mapa, los botones +/−, la atribución y los puntos de los centros con coordenadas válidas.
5. Pasa sobre un marcador para ver el nombre. Desplaza el mapa y pulsa **Ver todos los puntos**.
6. Busca `cayala` o usa otro nombre existente: al recargar deben coincidir los puntos y resultados. Limpia los filtros y comprueba que vuelven todos.
7. Busca `xyz123`: la lista y el mapa deben indicar que no hay resultados, sin marcadores anteriores.
8. Reduce el ancho de la ventana o prueba en móvil: el mapa y la atribución deben ser visibles sin desplazamiento horizontal.
9. La ficha del centro sigue funcionando aunque en este bloque su mapa individual permanezca pendiente.

Si el fondo no aparece, revisa el aviso. Si aparece un 404, comprueba en las herramientas de desarrollador las rutas `/js/mapa-centros.js`, `/css/mapa-centros.css` y `/api/centros/mapa`. Un recurso externo bloqueado se revisa como problema de conexión; no se cambia la base de datos.

## Pruebas y límites de la verificación

Con Node.js 18 o posterior, desde la carpeta del proyecto:

```bash
node --test src/test/js/mapa-centros.test.cjs
```

Node solo es una herramienta opcional de pruebas; no se necesita para iniciar la aplicación ni para ver el mapa. No hay que ejecutar `npm install`.

Verificado aquí: sintaxis de JavaScript y 15 pruebas aprobadas con Leaflet, DOM y peticiones simuladas; integridad y aplicación del parche sobre la base del bloque 1. Las pruebas cubren coordenadas, encuadre solicitado, datos filtrados, reintentos, datos vacíos, sesión expirada, errores de API, nombres tratados como texto y fallos de teselas.

No se ejecutó aquí un navegador con Leaflet real ni Spring Boot con Supabase. La revisión visual y de conectividad real se realiza con los pasos manuales. Las pruebas simuladas no acreditan la apariencia, la descarga del CDN ni la respuesta del proveedor de mapas.

## Commit

`feat(mapa): integrar Leaflet y OpenStreetMap`

Abrir el pull request después del bloque 4, según el plan. El bloque 3 añadirá interacción entre tarjetas y marcadores, información emergente y mejoras del buscador.
