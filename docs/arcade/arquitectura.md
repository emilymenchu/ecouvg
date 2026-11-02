# POO de Eco Arcade

## Java / Spring Boot

`ControladorArcade` sirve la plantilla `/arcade` y la respuesta JSON de
`/api/arcade/materiales`. Recibe `ServicioArcade` mediante inyección por constructor.

`ServicioArcade` usa por composición `ServicioGuia`, selecciona las cuatro
categorías del contrato del juego y comprueba que tengan ejemplos. Devuelve un
503 si la configuración está incompleta. No duplica el repositorio ni la entidad
de la guía. `MaterialArcade` es un record inmutable que transporta solo el contenido
educativo necesario.

La seguridad existente exige sesión para `/arcade` y `/api/arcade/materiales`;
no se modifica `SecurityConfig`. Las lecturas no escriben en la base de datos.

## JavaScript orientado a objetos

La interacción a 60 FPS vive en el navegador. Toda la lógica está separada del DOM
en `modelo.mjs`; puede ejecutarse en Node.js para probarla.

| Clase | Responsabilidad |
|---|---|
| `Entidad` | Posición común de elementos del tablero. |
| `Tortuga` | Personaje principal y dirección. |
| `Residuo` | Material y ejemplo de la guía. |
| `Monstruo` | Enemigo y tiempo de transformación en árbol. |
| `Bloque` | Material, dimensiones y estado en Breakout. |
| `Pelota` | Velocidad, posición y radio. |
| `Partida` | Puntos no negativos, vidas, nivel y estado. |
| `Juego` | Ciclo común, pausa, reanudación, vidas y victoria. |
| `EcoRun` | Laberinto, recogida, clasificación y enemigos. |
| `EcoSnake` | Fila, crecimiento, objetivos y colisiones. |
| `EcoBreakout` | Plataforma, rebotes, bloques y fases de materiales. |
| `Dibujante` | Representación Canvas, sin modificar las reglas. |
| `EcoArcade` | Eventos de teclado/táctiles, carga de datos y interfaz. |
| `Records` | Lectura y escritura tolerante a fallos en sessionStorage. |

**Herencia:** los tres juegos extienden `Juego`; tortuga, residuos, monstruos,
bloques y pelota extienden `Entidad`.

**Polimorfismo:** el bucle llama `actualizar`, `dirigir` y `objetivo` mediante el
mismo contrato. Cada juego aporta `avanzar` y sus propias reglas.

**Composición:** cada juego contiene una `Partida`, una `Tortuga` y las entidades
que necesita. El coordinador utiliza un juego y un dibujante; el dibujante no
consulta la base de datos ni registra eventos.

**Separación de responsabilidades:** modelo independiente de HTML, representación
Canvas y coordinación DOM en módulos distintos. El estado de la partida tiene un
único dueño y no se guarda en variables globales sueltas. Los objetos de prueba
solo existen en `src/test`.

## Mecánicas y límites

- Estados: lista → jugando ↔ pausa → fin/victoria. Reiniciar crea una partida nueva.
- Las actualizaciones se detienen al perder foco o cambiar de pestaña; no se
  simula de golpe el tiempo pasado en segundo plano.
- Breakout calcula subpasos para las colisiones. Sus 50 bloques son cuadrados: solo colisionan los del material objetivo; el resto es atravesable. Corrige los rebotes centrales y relanza tras 12 segundos sin progreso, apuntando a un objetivo pendiente sin restar vidas ni puntos.
- Snake admite un giro pendiente por paso y bloquea la inversión inmediata.
- Eco Run tiene inmunidad breve al reaparecer y una sola carga a la vez.
- No se usan servicios cartográficos, librerías de juegos ni recursos de terceros.
- Se reutiliza `Eco sentada feliz.png` en el salón; dentro del tablero la tortuga
  se dibuja con Canvas vista desde arriba, para distinguir su dirección.
- Los botones son operables con teclado y los mensajes de estado usan aria-live.
  El tablero sigue siendo un juego visual; no se presenta como alternativa completa
  para personas que no puedan percibir Canvas.
- Los récords son por pestaña, no por cuenta. No son un ranking persistente.

## Identificación visual de residuos

El mismo dibujante genera botellas, latas, hojas y cajas para el tablero, la leyenda y el panel de objetivo. Este panel muestra nombre y color del material; la última recogida muestra el ejemplo real de la guía y el resultado. Eco Snake conserva `ultimoResiduo` y `colorFila` aunque el objetivo cambie de ronda. El color se acompaña de forma, número y texto. El antiguo récord de Rec-man se consulta como respaldo al mostrar Eco Run.
