# Actualización de Eco Arcade: Eco Run y residuos visibles

Este parche se aplica sobre la primera versión de Eco Arcade que ya instalaste.
No crea otra rama y no modifica Java, tu .env ni Supabase.

1. Detén la ejecución de EcoUVG en VS Code.
2. Descomprime ecouvg-arcade-ajustes.zip.
3. Copia el contenido de su carpeta archivos dentro de tu proyecto ecouvg-arcade,
   conservando las rutas y sustituyendo solamente los archivos incluidos.
   Evita reemplazar carpetas completas desde Finder. En la terminal:

```bash
cp -R "/ruta/del/paquete/archivos/." "/Users/danielnajera/Proyecto POO/Proyecto github/ecouvg-arcade/"
cd "/Users/danielnajera/Proyecto POO/Proyecto github/ecouvg-arcade"
node --test src/test/js/arcade.test.mjs
```

4. Las 22 pruebas deben pasar. Arranca de nuevo con “Ejecutar EcoUVG”.
5. Recarga /arcade sin caché (en Safari, ⌘ + ⌥ + R).
6. Comprueba que la tarjeta diga Eco Run y que Breakout muestre 50 bloques.
7. En Snake, recoge un residuo correcto: su dibujo, nombre y color deben aparecer
   encima del tablero y la fila debe adoptar ese color. Al cambiar la ronda,
   el objetivo nuevo y la última recogida deben seguir distinguiéndose.
8. En Breakout, la pelota atraviesa los materiales apagados. Solo el material
   iluminado colisiona. Esto evita barreras de bloques de rondas futuras.
   El impacto central en la plataforma añade desplazamiento horizontal.
   Si pasan 12 segundos sin destruir un bloque, Eco relanza hacia un objetivo
   pendiente y conserva vidas y puntos; perder la pelota normalmente sí cuesta vida.

La figura identifica el material; el texto de última recogida identifica el
residuo específico que proviene de los ejemplos de la guía. Los colores conservan
la paleta de la primera versión y no representan una norma local de reciclaje.

## Guardar los cambios

Desde tu rama feature/daniel-eco-arcade:

```bash
git add src/main/resources/templates/arcade.html src/main/resources/static/css/arcade.css src/main/resources/static/js/arcade src/test/js/arcade.test.mjs docs/arcade
git commit -m "Mejorar identificación de residuos y evitar bloqueos en Eco Breakout"
git push origin feature/daniel-eco-arcade
```

Si aún no has hecho el primer commit del arcade, agrega también sus archivos Java,
menús y pruebas como indica instalacion.md antes de crear el commit inicial.

Validación: 22 pruebas de lógica aprobadas, incluida una simulación física hasta
la victoria con los 50 bloques y una prueba de objetivo rodeado por materiales
futuros. Tableros renderizados y revisados con Canvas. No se pudo comprobar la
pantalla completa en navegador en este entorno; verifica arriba las interacciones
antes de integrar el PR.
