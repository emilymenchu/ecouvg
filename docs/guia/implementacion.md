# Guía de reciclaje

## Uso

Inicia sesión y abre /guia o pulsa Guía en el menú inferior. Hay ocho categorías en dos columnas, inspiradas en el dibujo: orgánicos, cartón, plásticos, metales, papel, envases multicapa, vidrio y manejo especial. Las ilustraciones de contenedores usan CSS y símbolos; no requieren descargas de imágenes.

El buscador GET /guia?q=... acepta nombres, descripciones y ejemplos, sin distinguir tildes o mayúsculas. Combina palabras y trata los símbolos de forma literal. No hay coincidencias: muestra un estado vacío y permite limpiar. Cada tarjeta abre /guia/{slug}, con descripción, ejemplos, preparación y fuente; volver conserva la búsqueda. Las rutas continúan protegidas por la configuración de sesión existente.

Guía es una pestaña propia, separada de Residuos. Se comparte el fragmento fragmentos/navegacion.html en Inicio, Residuos, Retos, Centros, las fichas y la guía. La navegación contiene cinco enlaces y se adapta al ancho disponible.

## Datos

GuiaContenedor es un modelo educativo inmutable; ServicioGuia mantiene un catálogo versionado de contenido. No es una entidad JPA y no representa contenedores físicos. No se modifica la entidad Contenedor existente ni se escriben tablas de Supabase. Esta decisión permite mostrar inmediatamente las ocho categorías sin depender de registros desconocidos de la base.

El catálogo se edita en ServicioGuia.java. Los ejemplos son orientativos; no certifican que los centros del directorio los reciban. Los colores siguen la referencia visual del proyecto, no una norma de Guatemala. La pantalla explica que prevalecen la rotulación y las condiciones del centro. El enlace de la ficha lleva al directorio general, sin inventar una correspondencia entre la categoría y los materiales de Supabase.

Referencias consultadas el 15/10/2026:
- https://espanol.epa.gov/espanol/como-reciclar-los-materiales-reciclables-comunes
- https://www.epa.gov/recycle/composting-home
- https://www.epa.gov/recycle/frequent-questions-recycling

## Verificación

Se ejecutaron siete comprobaciones de lógica Java sobre copias temporales del modelo, normalizador y servicio, retirando únicamente la anotación/importación de Spring de esa copia. Validan catálogo, tildes, varias palabras, ejemplos, búsqueda literal, categoría ausente e inmutabilidad. Esto no comprueba arranque de Spring, persistencia ni renderizado Thymeleaf.

Se incluyen 11 pruebas JUnit (6 de servicio y 5 de contrato MVC). Ejecutarlas en el equipo con Java 25:

```bash
sh ./mvnw -Dtest=ServicioGuiaTest,ControladorGuiaTest test
```

No se ejecutaron aquí: Maven no tiene disponible su distribución/dependencias. Tampoco se verificó con un navegador real. Antes de guardar el commit:

1. Reinicia Spring Boot, inicia sesión y abre /guia. Deben verse ocho tarjetas en dos columnas.
2. Busca PLASTICOS, pilas y botellas vidrio. Confirma los resultados.
3. Busca zzzzinexistente y limpia: recupera las ocho categorías.
4. Abre una tarjeta, verifica descripción y ejemplos y vuelve; conserva la consulta.
5. Visita /guia/inventada: debe responder 404.
6. Recorre Inicio, Residuos, Guía, Retos y Centros: todos deben conservar las cinco pestañas y resaltar solamente la activa.
7. En ventana de 320–390 px, comprueba que el menú no se desborde ni tape el final de la pantalla. Usa Tab y Enter en buscador, tarjetas y menú.
8. Prueba los mapas de lista y ficha para descartar regresiones de estilos.
9. Sin sesión, /guia debe seguir redirigiendo al login.

## Integración

Este parche parte del cuarto bloque del mapa aplicado. Guarda primero los cuatro commits del mapa. La guía es una funcionalidad adicional y debe guardarse en su propio commit; no se debe incluir mediante amend en el cuarto commit del mapa.
