# Guía de contenedores desde PostgreSQL

## Qué cambia

Las tarjetas, el buscador y el detalle consultan PostgreSQL en cada petición.
`ServicioGuia` ya no contiene categorías, ejemplos, instrucciones ni enlaces fijos.
`CategoriaGuia` es la entidad JPA; `RepositorioGuia` recupera las categorías con sus
 ejemplos y `GuiaContenedor` sigue siendo el objeto inmutable que recibe la vista.
El controlador conserva `/guia` y `/guia/{slug}`. No se necesita instalar npm ni
el cliente JavaScript de Supabase: se utiliza la conexión JDBC existente.

Las tablas nuevas son `public.guia_contenedores` y
`public.guia_contenedor_ejemplos`. No se modifica la tabla `contenedor` ni su
relación con `Residuo`: el catálogo educativo tiene campos y ejemplos distintos.
Los colores siguen siendo ilustrativos, no una norma local de clasificación.

## 1. Aplicar los archivos

Este cambio está preparado contra el ZIP de main con commit
`f641bdbf804a886f70352f8c85ea73944e9060a2`.
Como tu carpeta principal tiene trabajo pendiente, crea una carpeta y rama nuevas:

```bash
cd "/Users/danielnajera/Proyecto POO/Proyecto github/ecouvg"
git fetch origin
git worktree add -b feature/daniel-contenedores-bd ../ecouvg-contenedores-bd origin/main
```

Copia el CONTENIDO de la carpeta `archivos` del paquete a
`ecouvg-contenedores-bd`, conservando las rutas. Solo sustituye los archivos
incluidos en el paquete; no reemplaces carpetas completas desde Finder.
Si main ha cambiado después del commit indicado, revisa las diferencias antes
de sustituir archivos. Configura en esa nueva carpeta las variables de conexión
que ya usas (credenciales renovadas); no las agregues a Git.

## 2. Crear tablas y cargar datos

Antes de arrancar el código nuevo, abre Supabase → SQL Editor y ejecuta completo:

`docs/guia/01_guia_base_datos.sql`

El script crea las dos tablas en una transacción e inserta ocho categorías y
sus ejemplos. Puede repetirse: las categorías existentes y sus ejemplos no se
sobrescriben ni se duplican. No es un script para restaurar ejemplos borrados
manualmente. Las tablas no deben existir con otra estructura incompatible.
No hay carga automática al arrancar, ni copia del catálogo como respaldo en Java.

RLS está activado sin políticas para clientes web: la aplicación usa el usuario
JDBC del servidor. Con el usuario postgres que utiliza el proyecto, las consultas
funcionan como propietario. Si usas un rol JDBC restringido, el administrador debe
configurar permisos SELECT y políticas RLS para ese rol. No desactives RLS para
solucionar un acceso del navegador: el navegador no necesita consultar Supabase.

Comprueba la carga:

```sql
SELECT c.slug, c.nombre, c.color, c.orden, count(e.posicion) AS ejemplos
FROM public.guia_contenedores c
LEFT JOIN public.guia_contenedor_ejemplos e ON e.contenedor_slug = c.slug
GROUP BY c.slug, c.nombre, c.color, c.orden
ORDER BY c.orden, c.slug;
```

En una instalación nueva debe devolver ocho categorías y 19 ejemplos en total.

## 3. Ejecutar y comprobar

En la carpeta nueva, con Java 25 y las variables habituales de la aplicación:

```bash
bash mvnw -Dtest=ServicioGuiaTest,ControladorGuiaTest,RepositorioGuiaTest test
bash mvnw spring-boot:run
```

1. Inicia sesión y abre http://localhost:8080/guia.
2. Comprueba las ocho tarjetas y el detalle de cada una.
3. Busca `CARTON`, `pilas`, `botellas vidrio` y `cafe`: deben ignorarse las tildes
   y mayúsculas y combinarse las palabras de la búsqueda.
4. Busca un texto inexistente y comprueba el mensaje sin resultados.
5. Abre `/guia/inventada`: debe responder 404.
6. En Supabase edita la descripción de una categoría, guarda y recarga su detalle.
   Debe aparecer el texto nuevo sin recompilar ni reiniciar. Restaura el texto
   anterior después de la prueba si era solo una comprobación.
7. Vuelve a ejecutar el SQL de carga y comprueba que conserva esa edición y que
   no duplica filas.

`color` elige una clase de CSS existente: verde, cafe, azul, gris, amarillo,
naranja, celeste o negro. Nuevos diseños requieren añadir CSS. `orden` determina
el orden de tarjetas; empates se resuelven por slug. En ejemplos, `posicion` debe
ser consecutiva desde cero por categoría, ya que corresponde a una lista JPA.
Usa URLs HTTPS de fuentes educativas en `fuente_url`. Los textos se muestran con
`th:text`, no como HTML. El catálogo es pequeño: el filtro se hace en memoria
sobre los datos recién leídos para mantener la búsqueda sin tildes.

Si la tabla está vacía, la guía muestra que no hay categorías cargadas. Si la
conexión falla, no se oculta el error mostrando datos antiguos.

## 4. Commit y pull request

Después de verificar la aplicación:

```bash
git status
git add src/main/java/gt/edu/uvg/ecouvg/modelo/CategoriaGuia.java src/main/java/gt/edu/uvg/ecouvg/repositorio/RepositorioGuia.java src/main/java/gt/edu/uvg/ecouvg/servicio/ServicioGuia.java
git add src/main/resources/templates/guia.html src/main/resources/templates/guia-detalle.html
git add src/test/java/gt/edu/uvg/ecouvg/GuiaPrueba.java src/test/java/gt/edu/uvg/ecouvg/servicio/ServicioGuiaTest.java src/test/java/gt/edu/uvg/ecouvg/controlador/ControladorGuiaTest.java src/test/java/gt/edu/uvg/ecouvg/repositorio/RepositorioGuiaTest.java
git add docs/guia
git diff --cached --stat
git commit -m "Persistir catálogo de contenedores en PostgreSQL"
git push -u origin feature/daniel-contenedores-bd
```

Desde GitHub abre un PR con base `main` y comparación
`feature/daniel-contenedores-bd`. El SQL debe ejecutarse antes de desplegar el
código. Para volver al código anterior basta revertir este commit; las tablas
nuevas pueden permanecer sin uso, sin borrarlas.

## Validación de este paquete

Se compararon los ocho registros y sus 19 ejemplos con el catálogo original;
se revisaron el mapeo de columnas, las rutas y los cambios incluidos.
Se actualizaron las pruebas existentes y se añadieron pruebas JPA con H2 para
persistencia, orden de ejemplos, catálogo vacío y edición sin reiniciar.
No se pudieron ejecutar las pruebas Java en el entorno de preparación: el Java
instalado no arranca y no fue posible descargar Maven. Tampoco se ejecutó el SQL
en tu Supabase. Los comandos de arriba permiten validar ambos en tu equipo.
