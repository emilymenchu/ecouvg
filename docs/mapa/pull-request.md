# Mapa de centros de reciclaje con OpenStreetMap

El directorio de centros tenía un espacio reservado para el mapa. Esta implementación permite ubicar los resultados de búsqueda y consultar cada centro desde su marcador o su ficha.

## Cambios

- API de mapas con DTO y validación de coordenadas, manteniendo el acceso autenticado.
- Mapa Leaflet con teselas OpenStreetMap y atribución visible.
- Búsqueda por nombre, zona o material sincronizada con los puntos del mapa.
- Selección entre tarjetas y marcadores, ventanas con información y enlace a la ficha.
- Mapa individual, avisos para coordenadas ausentes, errores de red y sesión caducada.
- Pruebas de DTO, servicio, contrato HTTP e interacción JavaScript.

## Validación

- [x] 30 pruebas JavaScript con dependencias simuladas ejecutadas en el entorno de preparación.
- [ ] 19 pruebas Java seleccionadas ejecutadas en el equipo de desarrollo.
- [ ] Recorrido manual de docs/mapa/commit-4.md completado.
- [ ] Verificación en móvil y con teclado.
- [ ] Acceso con sesión y redirección sin sesión comprobados.
- [ ] Revisado el diff del PR para excluir credenciales y cambios ajenos.

## Consideraciones

Leaflet y las teselas necesitan internet. Los puntos usan las coordenadas existentes, sin cálculo de distancia ni rutas. Los centros sin ubicación válida conservan su información textual. No se modifica el esquema ni se cargan nuevos datos en Supabase.
