# Notificaciones de DonaTrack

Los avisos salen por mail (SendGrid) o por SMS (Twilio), según el medio predeterminado de quien los recibe. Cada aviso se guarda en la tabla `notificacion` como `COMPLETADA` o `FALLIDA` y se ve en la bandeja del donante y de la entidad.

## Para seguir el código

Todo en `donaciones-service`:

1. `domain/notificacion/Contacto.java` y sus subclases: cada medio sabe enviar con `enviar(mensaje)`. `ContactoPorEmail` usa el SDK de SendGrid y `ContactoPorSMS` el de Twilio (Adapter). `ContactoPorWhatsApp` no se implemento porque se bloquea la cuenta del emisor.
2. `domain/notificacion/Notificable.java`: lo implementan `Persona`, `EntidadBeneficiaria` y `PersonaAdministradora`. Tienen contactos y un medio predeterminado; si no hay, se usa el primero.
3. `controller/Notificador.java`: arma el texto de cada aviso y decide a quién va. Manda y guarda recién después del commit del request: si el request falla, no sale nada.
4. Quién llama al `Notificador`:
   - `DonanteController.registrarDonante`: bienvenida.
   - `AsignacionesController.confirmarAsignacion`: donación asignada, al donante y a la entidad.
   - `DonacionController` (PATCH de estado): inicio de ruta, entrega exitosa y entrega fallida.
5. `planificacion/NotificarPersonasInactivas.java`: cron de ausencia (20 días sin actividad).
6. `repository/NotificacionRepository.java`, `controller/NotificacionesController.java` y `templates/notificaciones.hbs`: guardado y bandeja.

## Claves

Van en dos archivos en la raíz del repo. No se suben a git (`.gitignore` los excluye).

`sendgrid.env`:

```bash
export SENDGRID_API_KEY='SG...'
export SENDGRID_REMITENTE='remitente-verificado@dominio'
```

`twilio.env`:

```bash
export TWILIO_ACCOUNT_SID=AC...
export TWILIO_AUTH_TOKEN=...
export TWILIO_PHONE_NUMBER=+1...
```

Si falta alguna variable, el envío se simula: el log muestra `[SIMULADO]` y la notificación queda `COMPLETADA`. Así andan los tests y quien no tenga los archivos.

## Probar con Docker

`compose.yaml` le pasa los dos archivos al contenedor `donaciones-api` con `env_file` (opcionales). Después de crearlos o cambiarlos, recrear el contenedor:

```powershell
docker compose up -d donaciones-api
```

## Probar desde la terminal

Las clases `EnvioNotificacionEmailReal` y `EnvioNotificacionSmsReal` (en `donaciones-service/src/test/java/notificaciones`) mandan un mensaje real sin tocar la base. Desde Git Bash, en la raíz del repo:

```bash
source sendgrid.env
mvn test-compile exec:java -pl donaciones-service -Dexec.mainClass="notificaciones.EnvioNotificacionEmailReal" -Dexec.classpathScope="test" -Dexec.args="destino@mail.com"
```

```bash
source twilio.env
mvn test-compile exec:java -pl donaciones-service -Dexec.mainClass="notificaciones.EnvioNotificacionSmsReal" -Dexec.classpathScope="test" -Dexec.args="+549..."
```

Para saber si las variables se cargaron: `echo ${#SENDGRID_API_KEY}` tiene que dar 69. Si sale bien, se ve `-> Envío Email finalizado con éxito.` o `-> Envío SMS finalizado`. Si se ve `[SIMULADO]`, repetir el `source` en la misma terminal.

Desde IntelliJ: en la Run Configuration de la clase, cargar el archivo `.env` como variables de entorno.

## Prueba rápida con las clases de prueba

Las clases `EnvioNotificacionEmailReal` y `EnvioNotificacionSmsReal` (en `donaciones-service/src/test/java/notificaciones`) mandan un mensaje real sin levantar la app ni tocar la base. Sirven para comprobar que las claves andan.

1. En IntelliJ, abrir la pestaña Terminal (Git Bash) y pararse en la raíz del repo (`dona-track`).
2. Cargar las claves en esa terminal. No muestra nada:

   ```bash
   source sendgrid.env
   source twilio.env
   ```

3. Comprobar que se cargaron, sin ver los valores. Tiene que dar 69 y 34; si da 0, repetir el paso 2 desde la raíz:

   ```bash
   echo ${#SENDGRID_API_KEY}
   echo ${#TWILIO_ACCOUNT_SID}
   ```

4. Mandar un mail, poniendo el destino en `exec.args`:

   ```bash
   mvn test-compile exec:java -pl donaciones-service -Dexec.mainClass="notificaciones.EnvioNotificacionEmailReal" -Dexec.classpathScope="test" -Dexec.args="destino@mail.com"
   ```

   Tiene que aparecer `-> Envío Email finalizado con éxito.` Revisar la bandeja de entrada y spam.

5. Mandar un SMS a un número verificado en Twilio:

   ```bash
   mvn test-compile exec:java -pl donaciones-service -Dexec.mainClass="notificaciones.EnvioNotificacionSmsReal" -Dexec.classpathScope="test" -Dexec.args="+549..."
   ```

   Tiene que aparecer `-> Envío SMS finalizado`.

Si se ve `[SIMULADO]`, las variables no están cargadas en esa terminal: volver al paso 2. Si no aparece la línea de éxito, el log muestra el error de SendGrid (con su código HTTP) o de Twilio.

Las variables duran mientras la terminal esté abierta. Desde IntelliJ también se puede correr la clase con Run, cargando el `.env` en las variables de entorno de la Run Configuration.

## En el servidor

Los `.env` no llegan con `git pull`: hay que copiarlos a mano a la carpeta de `compose.yaml` y recrear el contenedor:

```bash
scp sendgrid.env twilio.env usuario@IP-del-servidor:/ruta/a/dona-track/
docker compose -f compose.yaml -f docker-compose.azure.yaml up -d donaciones-api
```

## Cuentas

- SendGrid: cuenta de prueba de 60 días con 100 mails por día, creada el 23/11/2026. El remitente tiene que estar verificado (Settings → Sender Authentication).
- Twilio: cuenta de prueba con saldo. Solo manda a números verificados (Phone Numbers → Verified Caller IDs), en formato `+549...`, y antepone «Sent from your Twilio trial account». Argentina tiene que estar habilitada en Messaging → Settings → Geo permissions.
