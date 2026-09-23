# Directorio inicial de centros

Consulta documental: 23 de septiembre de 2026. Referencia: campus central UVG, Vista Hermosa III, zona 15. Se eligieron cinco puntos de acopio de zonas 15, 16, 10, 14 y 17, respaldados por el directorio del operador. No constituyen un ranking de cercanía: no se calcularon rutas, distancias ni tiempos y Portales amplía la cobertura hacia zona 17.

| Punto | Zona | Materiales publicados | Recepción dentro de la sede |
|---|---|---|---|
| CEMACO Express Zona 15 | 15 | Electrónicos | Entrada |
| CEMACO Cayalá | 16 | Electrónicos, plástico, metal, papel, cartón, multicapa y vidrio | Servicio al cliente |
| Plaza CEMACO | 10 | Electrónicos, plástico, metal, papel, cartón, multicapa y vidrio | Sótanos, cerca de escaleras |
| CEMACO La Noria | 14 | Electrónicos | Entrada |
| CEMACO Portales | 17 | Electrónicos, plástico, metal, papel, cartón, multicapa y vidrio | Entrada |

La fuente denomina “papel-cartón” y “multicapa” a dos categorías; en la aplicación se separan Papel/Cartón y se usa Tetra Pak, también mencionado en la descripción general del programa. No se infiere que cualquier plástico, aparato o envase sea admisible: deben confirmarse las condiciones específicas.

## Fuentes

- [Programa y tabla de estaciones CEMACO](https://www.cemaco.com/reciclaje): sedes, materiales y ubicación de recepción. Requiere residuos clasificados, limpios, secos y empacados.
- [Directorio oficial de tiendas](https://www.cemaco.com/tiendas): direcciones y enlaces de ubicación. Los enlaces de Maps publicados allí contienen las coordenadas del lugar en los componentes `!3d` y `!4d`; no se usó el centro de la cámara `@`.
- [Zona 15](https://goo.gl/maps/8PEKzLpigs8Zsu2p7), [Cayalá](https://maps.app.goo.gl/RQ5VX13rjeJSKb5NA), [Plaza CEMACO](https://goo.gl/maps/mTEvnVwunkSJk5Ni6), [Portales](https://goo.gl/maps/wQ5NMiHUbL4odjru7): enlaces publicados por CEMACO. La redirección devolvió la referencia del lugar, aunque no se pudo renderizar el mapa. Para La Noria se conservó la referencia del centro comercial; todos los enlaces exactos están en `centros-investigados.json`.

Las direcciones, coordenadas, fuentes y materiales por registro se encuentran en `centros-investigados.json`. La fecha de consulta registra la revisión de la fuente, no una visita ni una llamada de confirmación.

## Datos pendientes y exclusiones

Los horarios concretos de recepción no quedaron disponibles en las fuentes consultadas: el valor almacenado indica que deben confirmarse. No se interpretó el indicador dinámico “Cerrado” de la tienda como horario del acopio. Las coordenadas sitúan el establecimiento, no el punto interior de depósito.

El PDF proporcionado con veinte centros es un directorio preliminar. Sus cuatro puntos marcados con † no tienen actividad actual confirmada y la jornada Reciclatú no es un centro permanente. No se cargaron esos veinte como si todos estuvieran activos. Tampoco se incorporó Resikla de PriceSmart sin completar la verificación de recepción y datos de esa sede.

## Referencias de implementación

- [Supabase: conexión PostgreSQL](https://supabase.com/docs/guides/database/connecting-to-postgres).
- [Spring Data JPA: consultas de repositorio](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html).
- Fase 2, páginas 8 y 12–13: atributos, constructor y `aceptaResiduo`.
- Entrega 1, página 21: prototipo final de pantalla.
