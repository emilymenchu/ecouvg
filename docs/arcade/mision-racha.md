# Misión de racha: cinco aciertos en Arcade

## Regla

Cinco aciertos en UNA partida de cualquiera de los tres juegos completan la misión
semanal. No hace falta ganar ni terminar. No son cinco puntos ni cinco partidas.
Los errores no restan aciertos ya obtenidos; reiniciar o cambiar de juego empieza
el contador en cero. Cambiar de nivel o perder una vida sin terminar conserva el
contador de esa partida.

- Eco Run: cada depósito en el contenedor correcto cuenta uno; recoger no cuenta.
- Eco Snake: cada residuo del material solicitado cuenta uno.
- Eco Breakout: cada bloque correcto destruido cuenta uno.

La misión se registra una vez por usuario y semana (lunes a domingo en Guatemala).
Usa la racha existente en Usuario.registroActividadValida. Si ya hubo una evidencia
aprobada esa semana, la misión se completa pero no aumenta otra vez la racha.
No entrega EcoPuntos. Es una misión de actividad semanal independiente de los retos
con fotos: no crea evidencia ni se suma al contador de retos con evidencia aprobada.
La misión se acredita en la fecha en que el servidor recibe el envío válido;
no se acreditan fechas pasadas desde el navegador.

## Instalar

1. Detén EcoUVG. Desde la carpeta principal del repositorio:

```bash
git fetch origin
git worktree add -b feature/daniel-arcade-racha ../ecouvg-arcade-racha origin/main
```

2. Copia el contenido de `archivos` del paquete a esa carpeta nueva conservando
las rutas, sin reemplazar carpetas enteras. Revisa diferencias si main avanzó
respecto del ZIP enviado. Abre la nueva carpeta en VS Code y coloca tu .env local
con las credenciales vigentes.

3. ANTES de arrancar la aplicación, ejecuta `docs/arcade/03-mision-racha.sql` en el
SQL Editor de Supabase. Crea mision_arcade y agrega version a usuarios. No borra
usuarios, puntos o rachas. Puede volver a ejecutarse sobre el esquema creado por
este mismo script. La tabla queda con RLS; se consulta con el usuario JDBC del
servidor, no desde el cliente Supabase del navegador. Si el rol JDBC es restringido,
el administrador debe conceder los permisos y políticas correspondientes.

4. Ejecuta las pruebas:

```bash
node --test src/test/js/arcade.test.mjs src/test/js/mision-arcade.test.mjs
bash mvnw -Dtest=ServicioMisionArcadeTest,ControladorMisionArcadeTest test
```

5. Arranca con la configuración existente “Ejecutar EcoUVG”. Inicia sesión y abre
/arcade. Recarga sin caché si conservabas abierta la versión anterior.

6. Comprueba: cuatro aciertos no completan; el quinto muestra confirmación sin
necesidad de terminar; volver a Inicio muestra la misión completada y la racha.
Repite en otro juego: no debe sumarse otra semana. Comprueba reinicio, cambio de
juego y un fallo de red seguido de “Reintentar guardar misión”. Sin confirmación
HTTP del servidor, la interfaz NO afirma que esté guardada.

## Persistencia y concurrencia

MisionArcade guarda usuario, juego, fecha y lunes de la semana. La restricción
UNIQUE(usuario_id, semana) impide duplicados. ServicioMisionArcade bloquea el usuario
en una transacción antes de consultar/insertar y actualizar la racha. El carné se
obtiene de Authentication; no se acepta id de usuario ni fecha enviados por el cliente.

La nueva propiedad @Version de Usuario evita sobrescribir silenciosamente cambios
concurrentes de puntos o racha realizados por otros módulos. Un conflicto causa
rollback, y el envío del Arcade puede reintentarse. Es importante desplegar la
nueva versión de la aplicación de forma consistente: no mantener instancias viejas
que escriban usuarios sin usar la columna version. La prueba de concurrencia con
múltiples conexiones reales de PostgreSQL queda pendiente en el entorno destino.

## Alcance de validación

El servidor valida sesión, CSRF, nombre del juego, rango de aciertos y unicidad
semanal. El motor del navegador cuenta los aciertos reales durante el uso normal.
No se ha trasladado la simulación al servidor: una persona que manipule su propio
navegador puede fabricar un reporte de aciertos. Esta implementación NO es un
sistema antitrampas autoritativo; no debe reutilizarse para premios de valor o
ranking competitivo sin validación adicional del juego en el servidor.

El envío fallido se conserva en memoria mientras la página siga abierta, incluso
si se cambia de juego. Cerrar/recargar la página antes de guardarlo pierde ese
hito local. Un reintento de una petición ya confirmada es seguro. Para consultar
el estado después de una respuesta perdida, vuelve a cargar la página.

## POO y clases

- Partida: contador aciertos común a los tres motores.
- MisionRacha (JavaScript): observa el quinto acierto, comunica con el servidor,
  maneja CSRF, reintentos y confirmaciones sin depender de los puntos.
- MisionArcade: entidad persistente de cumplimiento semanal.
- RepositorioMisionArcade: consulta de cumplimiento por usuario/semana.
- ServicioMisionArcade: regla de cinco aciertos y actualización transaccional.
- ControladorMisionArcade: GET y POST /api/arcade/mision con identidad de sesión.
- SolicitudMisionArcade y EstadoMisionArcade: mensajes entre navegador y servidor.
- RelojArcadeConfig: reloj inyectable para probar fechas de Guatemala.

## Verificación del paquete

29 pruebas JavaScript aprobadas. Se incluyen siete pruebas Java de identidad,
umbral, repetición, evidencia de la misma semana y límites de semana Guatemala/UTC.
No se ejecutaron aquí: solo hay Java 21 y falló la descarga de Maven; el proyecto
requiere Java 25. No se aplicó el SQL a Supabase ni se validó el renderizado completo
de Spring Boot. Completa esos pasos antes del merge.

## Commit

En la rama nueva, revisa git status y agrega únicamente los archivos del paquete;
no incluyas .env ni configuraciones personales. Mensaje sugerido:
“Completar misión de racha con cinco aciertos de Arcade”. Sube esa rama y abre
un PR hacia main. Ejecuta la migración antes de desplegar el código nuevo.
