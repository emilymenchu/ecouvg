# Mapa de centros: bloque 4 de 4

Base requerida: bloques 1, 2 y 3 aplicados, en feature/daniel-mapa-centros.

## Qué completa

La ficha /centros/{id} muestra el mapa del centro consultado, con marcador, información y botón Centrar ubicación. Usa los mismos estilos y JavaScript que el directorio. El endpoint autenticado GET /api/centros/mapa/{id} entrega un objeto CentroMapaDTO; devuelve 404 si no existe. La búsqueda general sigue entregando una lista. La conversión a DTO se comparte dentro del servicio, con la transacción de lectura y el EntityGraph del repositorio existentes.

Si las coordenadas faltan o son inválidas, la ficha oculta el mapa y explica que se debe consultar la dirección. No se inventa una ubicación. Los fallos de red permiten reintentar; una sesión caducada ofrece volver a iniciar sesión. Los datos de los popups se insertan como texto. No se agregan dependencias npm, claves de mapas ni cambios a la base de datos.

## Pruebas antes del commit

Desde la carpeta ecouvg-mapa, con Java 25 y acceso a las dependencias Maven:

```bash
sh ./mvnw -Dtest=CentroMapaDTOTest,ServicioCentroMapaTest,ControladorCentroMapaTest test
```

Hay 19 pruebas Java: 6 de DTO, 6 de servicio y 7 de controlador. Las del controlador usan MockMvc standalone; no verifican el login real. Las pruebas seleccionadas no necesitan Supabase ni correo.

Con Node.js 18 o posterior, si lo tienes instalado:

```bash
node --test src/test/js/mapa-centros.test.cjs
```

Se ejecutaron correctamente las 30 pruebas JavaScript con DOM, Leaflet y fetch simulados. No se ejecutó la aplicación en un navegador real ni se completaron las pruebas Maven en el entorno de preparación. Confirma los resultados Java en tu equipo antes de abrir el PR.

## Recorrido manual de aceptación

1. Reinicia Spring Boot desde ecouvg-mapa, inicia sesión y abre http://localhost:8080/centros.
2. Comprueba el mapa, los puntos y la atribución OpenStreetMap visible.
3. Busca un nombre o zona y filtra por un material. La lista y los puntos deben corresponder a los mismos resultados. Limpia los filtros.
4. Busca un texto sin coincidencias: no deben quedar marcadores anteriores.
5. Pulsa Ver en mapa en una tarjeta y después Ver tarjeta en la lista en la ventana del marcador. Comprueba el centro seleccionado y el foco.
6. Abre Ver información completa: la ficha debe mostrar solo el punto del centro. Arrastra el mapa y pulsa Centrar ubicación para regresar a él.
7. Repite con otro centro. Si hay centros sin coordenadas, comprueba el aviso y que no aparezca un mapa con una ubicación engañosa. No alteres datos reales para probarlo; el caso tiene cobertura simulada.
8. Prueba el ancho de un móvil, teclado y zoom. Las ventanas y controles deben poder usarse sin tapar la navegación.
9. En DevTools, bloquea temporalmente la petición a /api/centros/mapa/*, recarga una ficha y comprueba Reintentar carga. Desbloquea y reintenta: debe aparecer un solo marcador. Si bloqueas teselas, debe aparecer un aviso y conservarse el contenido textual.
10. Cierra sesión e intenta abrir /centros y el API: deben seguir pidiendo acceso. Vuelve a iniciar sesión.

## Alcance y servicios externos

Leaflet 1.9.4 se carga desde unpkg con integridad SRI; las teselas provienen de https://tile.openstreetmap.org/{z}/{x}/{y}.png. Se mantiene atribución, caché normal del navegador y referencia HTTP. No hay descarga masiva, precarga sin conexión ni cálculo de rutas. Se requiere internet para el fondo y la librería. La posición usa las coordenadas ya guardadas; debe contrastarse con los datos del centro antes de la entrega.

Fuentes técnicas: https://leafletjs.com/reference.html y https://operations.osmfoundation.org/policies/tiles/.

## Cierre

Mensaje sugerido: feat(mapa): completar mapa individual y validacion final

Después de pasar las comprobaciones, sube el cuarto commit y abre el pull request desde feature/daniel-mapa-centros hacia main. Usa docs/mapa/pull-request.md como descripción y marca únicamente las pruebas que hayas realizado. No incluyas .env ni credenciales. No mezcles la carpeta ecouvg anterior con ecouvg-mapa.
