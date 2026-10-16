# Mapa de centros: bloque 1 de 4

## Resultado

Se prepara el contrato de datos para conectar Leaflet con los centros de Supabase. Las pantallas y el buscador existentes siguen disponibles; el mapa visual se incorpora en el bloque 2.

Archivos de producción:

- `dto/CentroMapaDTO.java`: copia los datos necesarios del centro; valida y protege el par de coordenadas.
- `servicio/ServicioCentro.java`: agrega `obtenerCentrosMapa`, reutilizando `buscarCentros`; produce nombres de materiales únicos y ordenados dentro de la transacción de lectura existente.
- `controlador/ControladorCentro.java`: agrega la respuesta JSON y comparte la validación de longitud de parámetros con la vista HTML.

No se requiere una migración SQL: se utilizan las columnas y asociaciones existentes. No se cambian las credenciales ni la configuración de Spring Security. Esta última mantiene la ruta protegida por inicio de sesión.

## Contrato para los siguientes commits

`GET /api/centros/mapa?q=&material=`

Ambos parámetros son opcionales, con máximo 120 caracteres. La consulta utiliza las mismas reglas del buscador: normalización de tildes, mayúsculas y espacios, y filtro por material. Sin coincidencias devuelve `[]` y HTTP 200. Un parámetro demasiado largo devuelve HTTP 400. Se debe iniciar sesión antes de llamar la API; el login existente puede redirigir las peticiones sin sesión a `/login`.

Ejemplo ilustrativo, no un registro nuevo ni un ID que deba insertarse:

```json
[
  {
    "id": 7,
    "nombre": "Cayalá",
    "ubicacion": "Zona 16",
    "latitud": 14.6,
    "longitud": -90.4,
    "horario": "Por confirmar",
    "materiales": ["Cartón", "Vidrio"],
    "ubicable": true
  }
]
```

Una coordenada es válida si ambas componentes existen, son finitas y están dentro de latitud [-90,90] y longitud [-180,180]. Si no se cumple, el DTO devuelve ambas como `null` y `ubicable: false`, sin alterar la entidad o la base de datos. El cliente deberá conservar la tarjeta y omitir el marcador. No se sustituyen valores faltantes por cero; (0,0) sí es una coordenada geográficamente válida si fue registrada explícitamente. Validar rango no acredita que un centro esté realmente en ese lugar.

El contrato solo contiene datos del centro. Los próximos bloques deberán crear el contenido de las ventanas del mapa mediante nodos de texto, no concatenando nombres o direcciones en HTML sin escapar.

## Comprobación manual

1. Arrancar la aplicación con su configuración PostgreSQL habitual.
2. Iniciar sesión desde `/login`.
3. Abrir `http://localhost:8080/api/centros/mapa` en el mismo navegador: debe mostrar JSON con los centros que ya existen en la base. No se garantiza un número fijo si el equipo agregó o modificó registros.
4. Abrir `/api/centros/mapa?q=cayala`, `/api/centros/mapa?material=Cart%C3%B3n` y `/api/centros/mapa?q=xyz123`. Comparar con los mismos filtros en `/centros`.
5. Comprobar que `/centros` y una ficha `/centros/{id}` siguen funcionando.
6. Abrir la API en una ventana privada sin iniciar sesión: debe solicitar login.

## Pruebas

Desde la carpeta que contiene `pom.xml`, con JDK 25 configurado:

```bash
sh ./mvnw -Dtest=CentroMapaDTOTest,ServicioCentroMapaTest,ControladorCentroMapaTest test
```

Se incluyen 14 métodos de prueba: 6 del DTO, 4 del servicio y 4 del controlador. Usan objetos de prueba y un repositorio simulado, sin conexión a Supabase ni envío de correo. Cubren coordenadas incompletas, rangos, NaN/infinito, copia de materiales, conservación de entidades, filtros, contrato JSON y parámetros largos. Las pruebas MVC standalone no ejecutan los filtros de Spring Security ni acreditan integración JPA; comprobar autenticación y base real con los pasos manuales anteriores.

Verificación realizada al preparar este bloque: el DTO real compiló con Java 17 y pasó 29 comprobaciones ejecutables. No se ejecutaron las 14 pruebas Maven ni la compilación completa: el entorno bloqueó la descarga de Maven y no dispone de JDK 25. El parche se comprobó contra una copia exacta del ZIP proporcionado.

## Historial previsto

1. `feat(mapa): preparar datos de centros para el mapa` (este bloque).
2. `feat(mapa): integrar Leaflet y OpenStreetMap`.
3. `feat(mapa): conectar marcadores con buscador y fichas`.
4. `feat(mapa): completar detalle y manejo de errores`.

El cuarto bloque incluirá las verificaciones finales y la preparación del pull request hacia `main`. Este ZIP de trabajo no contiene historial Git; el commit y el push se hacen desde la rama del estudiante.
