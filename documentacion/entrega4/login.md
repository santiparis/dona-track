# Login de DonaTrack

El acceso usa formularios HTML, una cookie de sesión y los repositorios JPA que ya tenía el TP. No hay JWT ni un framework de seguridad adicional.

## Para seguir el código mañana

Leelo en este orden:

1. `templates/sesion/login.hbs`: el formulario manda `usuario` y `password` a `POST /login`.
2. `web/Router.java`: conecta esa URL con `SesionController.ingresar`.
3. `controller/SesionController.java`: busca la cuenta, verifica la contraseña e inicia la sesión. Si algo no coincide, vuelve al formulario con un error genérico.
4. `repository/UsuarioRepository.java` y `domain/donante/Usuario.java`: buscan por nombre y verifican el hash. El nombre se guarda sin espacios en los extremos y en minúsculas.
5. `web/SesionUsuario.java`: crea una sesión nueva y guarda el ID y el rol. El navegador recibe una cookie `JSESSIONID`; la contraseña no viaja dentro de ella.
6. En la siguiente petición, `SesionUsuario.actual` recupera el usuario por su ID. `verificarAcceso` compara su rol actual con la sección solicitada, antes de ejecutar el controller.
7. `web/templates/JavalinHandlebars.java` agrega la cuenta al encabezado de todas las páginas. `POST /logout` invalida la sesión y borra la cookie.

La consulta del usuario se reutiliza durante esa petición, pero se hace de nuevo en la próxima. Así, borrar una cuenta o cambiar su rol también afecta las sesiones que ya estaban abiertas.

| Cuenta | Inicio y sección permitida |
| --- | --- |
| Donante | `/donante/donaciones` y `/donante/*` |
| Entidad beneficiaria | `/entidad/necesidades` y `/entidad/*` |
| Administradora | `/admin/donaciones` y `/admin/*` |

Sin sesión, una página privada redirige a `/login`. Con otro rol, devuelve 403. Las páginas públicas siguen disponibles. Volver a `/login` con una sesión activa lleva al inicio de esa cuenta.

La cookie es HttpOnly, SameSite=Lax, segura cuando se usa HTTPS y vence después de 30 minutos sin actividad. Al entrar se reemplaza cualquier sesión anterior. Las respuestas privadas llevan `Cache-Control: no-store`.

## Registro

`GET /usuarios/nuevo` muestra tres tipos: persona humana, persona jurídica y entidad. `POST /usuarios` valida los datos, las dos contraseñas y la aceptación de términos; después guarda el perfil y su usuario juntos, mediante la cascada JPA existente.

El correo electrónico es el usuario de acceso. Las cuentas humanas y jurídicas reciben `DONANTE`; las entidades reciben `ENTIDAD`. El servidor decide el rol: enviar un campo `rol=ADMINISTRADORA` no da permisos extra.

La contraseña debe tener entre 8 y 128 caracteres. Se almacena con PBKDF2 y una sal aleatoria por cuenta, usando `HashContrasenia`, que ya estaba implementado. Ante un error se conservan los campos del formulario, pero nunca las contraseñas. Un registro exitoso lleva al login con un aviso.

Los nombres de usuario son únicos. En una base existente, revisar `donaciones-service/db/migracion_usuario_unico.sql`; si hay duplicados, resolverlos antes de agregar la restricción. Para una base previa a los roles, también está `migracion_usuario_rol.sql`. No ejecutar otra vez una migración que ya está aplicada.

## API y comunicación entre servicios

`web/AccesoApi.java` exige una sesión administradora para `/api/*`: sin sesión devuelve JSON con 401; con un rol distinto, 403. Para usar Postman, primero enviar el formulario a `POST /login` y conservar la cookie en las siguientes peticiones.

Logística necesita avisar cambios de estado sin iniciar una sesión de persona. Esa única llamada, `PATCH /api/donaciones/{id}`, admite el encabezado `X-Token-Interno`. Ambos servicios leen la misma clave de `DONACIONES_TOKEN_INTERNO`. El cliente Retrofit la adjunta solo a las llamadas a Donaciones, nunca al planificador. La clave no habilita el resto del CRUD.

Si la variable no está configurada, el acceso con clave interna queda deshabilitado. Los errores HTTP al avisar un estado se informan como fallos, en lugar de dar el aviso por exitoso. La autenticación de las otras APIs de Logística queda fuera de este cambio.

## Probar con Docker

En PowerShell, desde la raíz del repositorio, generar una clave local y levantar los servicios en esa misma terminal:

```powershell
$env:DONACIONES_TOKEN_INTERNO = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')
docker compose up --build -d
```

Para conservar la clave entre terminales, se puede definir en un `.env` local, que está ignorado por Git. En Azure, configurar exactamente el mismo valor en los dos hosts. No compartir ni subir ese archivo.

Entrar a `http://localhost:8081/usuarios/nuevo`, crear una cuenta e ingresar con su correo. Probar una contraseña incorrecta, visitar una sección de otro rol y cerrar sesión. Después de salir, volver a una URL privada debe llevar al login.

El rol de administración se crea por un comando explícito; no aparece en el registro público. Definir estas variables localmente con los valores elegidos:

```powershell
$env:ADMIN_USUARIO = 'admin-local'
$env:ADMIN_NOMBRE = 'Administración local'
$env:ADMIN_PASSWORD = 'reemplazar-por-una-clave-propia'
docker compose exec -e ADMIN_USUARIO -e ADMIN_NOMBRE -e ADMIN_PASSWORD donaciones-api java -cp /app/app.jar donaciones.CrearAdministradora
Remove-Item Env:ADMIN_PASSWORD
```

El comando guarda la cuenta y la persona administradora en una transacción. Si el usuario ya existe, falla sin modificar su rol ni su contraseña. No crea cuentas predeterminadas al arrancar.

## Pruebas y alcance

```powershell
mvn test
```

Las pruebas de sesión usan HTTP real y una base HSQLDB en memoria. Cubren credenciales, registro de los tres tipos, validaciones, permisos, cierre de sesión, cookies anteriores, cambio de rol y acceso a la API. Las pruebas de integración con Logística verifican el encabezado interno y las respuestas de error.

Las pantallas privadas de donaciones, necesidades y notificaciones muestran datos de la cuenta autenticada. El historial del donante y la recepción de una entidad también comprueban la pertenencia del registro. Una cuenta recién creada muestra listas vacías hasta que tenga datos en la base.

Para probar el aislamiento sin cargar datos a mano, ejecutar desde la raíz del repo:

```powershell
mvn -pl donaciones-service "-Dtest=RegistroHttpTest#cadaCuentaVeSusDatosYSoloAbreLaRecepcionQueLePertenece" test
```

La prueba registra dos donantes y dos entidades, carga datos distintos en una base en memoria y verifica que los listados no se mezclen. También intenta abrir la recepción y la edición de una necesidad de la otra entidad.

El seguimiento privado queda pendiente de una consulta a Logística por titular. El listado general de entidades y otras pantallas de la entrega todavía usan datos de ejemplo. Los formularios web de necesidades y recepción siguen pendientes de persistencia. Las cuentas antiguas que tuvieran contraseñas en texto plano necesitan una contraseña almacenada con el formato actual para ingresar.

Referencia del filtro de acceso: [documentación de Javalin 6](https://javalin.io/archive/docs/v6.X.html#access-management).
