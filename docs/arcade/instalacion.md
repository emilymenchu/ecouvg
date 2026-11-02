# Eco Arcade — instalación y pruebas

Implementación sobre main `9ea1fad949a93e40065025b23b13f5212dfbc8fd`.
Incluye únicamente Eco Run, Eco Snake y Eco Breakout, con Eco la tortuga.

## 1. Crear una rama en otra carpeta

En la terminal, ejecuta uno por uno:

```bash
cd "/Users/danielnajera/Proyecto POO/Proyecto github/ecouvg"
git fetch origin
git worktree add -b feature/daniel-eco-arcade ../ecouvg-arcade origin/main
```

Si la rama o la carpeta ya existen, no las borres: revisa `git worktree list` y
continúa en la carpeta correcta. Este procedimiento conserva el trabajo de las
otras carpetas. Si main avanzó después del commit base, revisa especialmente los
cambios del menú antes de sustituirlo.

## 2. Copiar los archivos

Descomprime `ecouvg-arcade.zip`. Dentro están `LEEME.md` y `archivos`.
Copia el contenido de `archivos` dentro de la carpeta nueva del proyecto.
No reemplaces carpetas completas con Finder. Puedes escribir `cp -R `, arrastrar
la carpeta `archivos` a la terminal y completar el origen con `/.`:

```bash
cp -R "/ruta/del/paquete/archivos/." "/Users/danielnajera/Proyecto POO/Proyecto github/ecouvg-arcade/"
```

Abre `ecouvg-arcade` en VS Code mediante Archivo → Abrir carpeta.
En una terminal nueva:

```bash
git branch --show-current
git status
```

La rama debe ser `feature/daniel-eco-arcade`. Configura el `.env` local con tus
credenciales vigentes, como en la aplicación actual. No agregues ese archivo a Git.
La configuración existente “Ejecutar EcoUVG” carga el `.env` de esta carpeta.

## 3. Base de datos

No hay tablas nuevas ni SQL nuevo para Eco Arcade. Utiliza `ServicioGuia`, que
consulta las categorías integradas en PostgreSQL. Deben existir estas categorías
con al menos un ejemplo cada una: `plasticos`, `metales`, `papel` y `carton`.
Los nombres, ejemplos e instrucciones provienen de la guía; no se incluye un
catálogo educativo alternativo en JavaScript. La paleta y los números son recursos
visuales del juego y no una norma de reciclaje.

## 4. Pruebas y arranque

Con Node.js 20 o superior (solo para las pruebas de JavaScript, sin npm install):

```bash
node --test src/test/js/arcade.test.mjs
```

Con Java 25, ejecuta las pruebas Java:

```bash
bash mvnw -Dtest=ServicioArcadeTest,ControladorArcadeTest test
```

Detén otras instancias que ocupen el puerto 8080. En VS Code selecciona Ejecutar y
depurar → Ejecutar EcoUVG. Inicia sesión y abre:

http://localhost:8080/arcade

También aparece “Arcade” en el menú inferior y “Eco Arcade” en el menú lateral.
En pantallas pequeñas el menú inferior puede desplazarse horizontalmente.
La aplicación no necesita nuevos paquetes npm, servicios externos ni claves API.

## 5. Comprobaciones manuales antes del PR

- Abrir las tres tarjetas y comprobar la tortuga y la leyenda.
- Eco Run: recoger un residuo, probar un contenedor incorrecto y después el correcto.
  El primero conserva la carga y resta cinco puntos; el segundo convierte los
  monstruos en árboles durante tres segundos. Son cuatro niveles, con 2, 4, 6 y 8
  residuos respectivamente.
- Eco Snake: recoger el material indicado y otro incorrecto, comprobar crecimiento,
  colisión con borde/cuerpo y cambio de objetivo cada tres aciertos. Se gana con 12.
- Eco Breakout: comprobar los 50 bloques cuadrados y mover la plataforma con teclado y ratón; comprobar pérdida de vida
  al caer la pelota, bloques de otras rondas que se pueden atravesar y cambio
  de objetivo al terminar un material. La victoria muestra un bosque. Tras 12 segundos sin romper un bloque, se relanza la pelota sin perder vidas ni puntos. El rebote central ya no produce una trayectoria perfectamente vertical.
- En los tres: pausar con P o el botón, cambiar de pestaña, continuar, reiniciar y
  volver al salón. Al reiniciar deben restaurarse 3 vidas y 0 puntos.
- En móvil: usar botones direccionales; en Breakout también se puede arrastrar el
  dedo sobre el tablero. Confirmar que la pantalla no corta el tablero o controles.
- Interrumpir la conexión y recargar: debe aparecer un mensaje y “Volver a cargar”.
  El juego no comienza si faltan datos. Una sesión expirada solicita iniciar sesión.
- El récord de cada juego se conserva al volver al salón dentro de esta pestaña.
  Si el navegador bloquea sessionStorage, se puede seguir jugando sin guardar récord.

Los puntos de Arcade NO modifican EcoPuntos, niveles, premios ni ranking de usuarios.
Son puntuaciones recreativas locales, editables por el navegador, sin valor como
validación de actividades ambientales. No se envían al servidor ni a Supabase.

## 6. Commit y PR

Agrega únicamente los archivos del paquete:

```bash
git add src/main/java/gt/edu/uvg/ecouvg/dto/MaterialArcade.java
git add src/main/java/gt/edu/uvg/ecouvg/servicio/ServicioArcade.java
git add src/main/java/gt/edu/uvg/ecouvg/controlador/ControladorArcade.java
git add src/main/resources/templates/arcade.html src/main/resources/templates/fragmentos/navegacion.html
git add src/main/resources/static/css/arcade.css src/main/resources/static/css/navegacion.css
git add src/main/resources/static/js/arcade
git add src/test/js/arcade.test.mjs
git add src/test/java/gt/edu/uvg/ecouvg/servicio/ServicioArcadeTest.java
git add src/test/java/gt/edu/uvg/ecouvg/controlador/ControladorArcadeTest.java
git add docs/arcade
git diff --cached --stat
git commit -m "Agregar Eco Arcade con Eco Run, Eco Snake y Eco Breakout"
git push -u origin feature/daniel-eco-arcade
```

En GitHub abre un pull request desde `feature/daniel-eco-arcade` hacia `main`.
Puedes usar `pull-request.md` como descripción y completar los resultados reales
de las pruebas Java y de navegador.

## Validación realizada al preparar el paquete

- 22 pruebas automatizadas de la lógica JavaScript: aprobadas con Node.js.
- Sintaxis de los módulos: revisada con `node --check`.
- Los tres tableros renderizados mediante Canvas y revisados visualmente.
- Se incluyen 5 pruebas Java de servicio y controlador, sin ejecutar aquí.
- No se ejecutó Spring Boot ni se conectó con Supabase en este entorno.
- La comprobación con navegador quedó pendiente: no había Chromium instalado y
  la descarga fue bloqueada. Las revisiones de escritorio/móvil deben completarse
  en la aplicación antes de integrar el PR.
