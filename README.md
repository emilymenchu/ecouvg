# EcoUVG ♻️

EcoUVG es una aplicación web desarrollada para facilitar la correcta clasificación de residuos y brindar información relacionada con el reciclaje.

## Inicio rápido


### 1. Clonar el repositorio

```bash
git clone https://github.com/emilymenchu/ecouvg.git
```

Ingresar a la carpeta del proyecto:

```bash
cd ecouvg
```

### 2. Configurar las variables de entorno para VSCode

EcoUVG utiliza una base de datos PostgreSQL alojada en Supabase.

Por seguridad, las credenciales de la base de datos no se encuentran almacenadas en el repositorio y serán proporcionadas de forma privada.

El proyecto incluye un archivo `.env.example` que muestra las variables necesarias:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Nosotros proporcionaremos las credenciales por privado para más seguridad.

#### Configuración en IntelliJ IDEA

1. Abrir el proyecto en IntelliJ IDEA.
2. Ir a **Run > Edit Configurations**.
3. Seleccionar la configuración utilizada para ejecutar EcoUVG.
4. Buscar **Environment variables**.
5. Agregar las variables proporcionadas:

```text
DB_URL=valor_proporcionado
DB_USERNAME=valor_proporcionado
DB_PASSWORD=valor_proporcionado
```

6. Guardar los cambios.


### 3. Ejecutar la aplicación

En macOS o Linux:

```bash
./mvnw spring-boot:run
```

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Cuando la aplicación termine de iniciar, ingresar en el navegador a:

```text
http://localhost:8080
```

### 4. Iniciar sesión

La base de datos ya contiene usuarios de prueba para ingresar a la aplicación.

| Usuario | Carné | Contraseña |
|---|---:|---:|
| Admin | 1234 | 1234 |
| Marco Díaz | 24229 | 1234 |
| Sarah Estrada | 24347 | 1234 |

Todos los usuarios de prueba utilizan la contraseña `1234`.

Estas son únicamente credenciales para iniciar sesión en EcoUVG y no proporcionan acceso directo a la base de datos.

---

## Sobre EcoUVG

EcoUVG busca facilitar el acceso a información sobre la correcta clasificación de residuos y los centros de reciclaje.

El proyecto está dirigido principalmente a estudiantes de la Universidad del Valle de Guatemala y surge como una propuesta para facilitar el aprendizaje sobre reciclaje y promover mejores hábitos de clasificación de residuos.

## Funcionalidades

### Fotos y edición del perfil

En **Perfil**, el botón de lápiz junto al avatar permite seleccionar una foto, revisar su vista previa y guardarla. Los datos editables son la foto y la contraseña. Para cambiar la contraseña se solicita la actual y la confirmación de la nueva; la clave se guarda usando el `PasswordEncoder` de la aplicación. La aplicación guarda la ruta de Storage en `usuarios.foto_perfil` y comprime la foto mediante `ServicioImagen` antes de subirla.

Para usarlo en Supabase, configurar `SUPABASE_URL` y `SUPABASE_SECRET_KEY` en el entorno del servidor y disponer de un bucket llamado `perfiles`, con permisos para que la clave del servidor suba, lea y elimine objetos. La imagen se sirve mediante `GET /perfil/foto`, que usa la cuenta de la sesión y obtiene el archivo con `ServicioStorage.obtenerImagen`; la vista ya no depende de una URL pública del bucket. El formulario admite JPG o PNG de hasta 5 MB. Las fotos se almacenan bajo `usuario_ID/UUID.jpg`.

La foto anterior se elimina después de confirmar la transacción de base de datos. Si el guardado se revierte, se intenta eliminar la nueva. Los errores de limpieza se registran en el log para poder retirar archivos pendientes posteriormente. Las pruebas locales simulan Storage; no comprueban credenciales ni permisos del proyecto de Supabase.

Actualmente el prototipo incluye:

- Inicio y cierre de sesión.
- Pantalla principal.
- Consulta de residuos.
- Búsqueda de residuos.
- Visualización de información de residuos.
- Consulta de centros de reciclaje.

El proyecto continúa en desarrollo y se agregarán nuevas funcionalidades en las siguientes etapas.

### Historial de Eco

En **Eco → Nuevo chat** se inicia otra conversación. El historial muestra las conversaciones de la cuenta actual; al abrir Eco se recupera la última, y se puede seleccionar otra para continuarla. Se guardan automáticamente la pregunta y la respuesta cuando Eco responde correctamente. Los reintentos usan un identificador para no duplicar intercambios.

La persistencia usa las tablas `conversaciones_chat` y `turnos_chat`. Con la configuración actual (`spring.jpa.hibernate.ddl-auto=update`), Hibernate las crea al reiniciar la aplicación, siempre que el usuario de base de datos tenga permisos de creación. Las conversaciones anteriores a esta funcionalidad no se pueden recuperar porque solo existían en la página del navegador.

El historial completo se conserva para consultarlo. Para responder, Eco recibe los últimos 10 intercambios de la conversación seleccionada y la pregunta nueva, usando el formato de contexto de [Gemini generateContent](https://ai.google.dev/api/generate-content). Cada lectura y escritura comprueba la cuenta propietaria. Las pruebas de persistencia usan H2 y simulan Gemini; no requieren credenciales remotas ni modifican Supabase.

## Tecnologías utilizadas

- Java
- Spring Boot
- Spring Data JPA
- Thymeleaf
- HTML
- CSS
- PostgreSQL
- Supabase
- Maven

## Estructura del proyecto

El proyecto utiliza una organización basada en Modelo-Vista-Controlador y separa el acceso a los datos mediante servicios y repositorios.

```text
src/main/java
├── controller
├── model
├── repository
└── service

src/main/resources
├── static
├── templates
└── application.properties
```

## Base de datos

La información del prototipo se almacena en PostgreSQL mediante Supabase.

La aplicación utiliza Spring Data JPA para acceder a los datos y las credenciales de conexión se obtienen mediante variables de entorno.

Las credenciales de la base de datos no deben agregarse directamente al código ni subirse al repositorio.

## Notas

- Es necesario tener conexión a Internet para utilizar la base de datos.
- Las credenciales de conexión a Supabase se proporcionan de forma privada.
- El archivo `.env.example` sirve como referencia de las variables necesarias y no contiene credenciales reales.
- Si el puerto `8080` está ocupado, será necesario cerrar la aplicación que lo esté utilizando o configurar otro puerto.

## Integrantes

- Emily Menchú
- Pilar Ordóñez
- Daniel Nájera
- Edith Pérez
