# Guía Rápida: Configuración de SendGrid
---

## 1. Configuración de Variables de Entorno

Para poder enviar correos mediante SendGrid (`NotificacionPorEmail`), es **requerido** que estén configuradas las variables de entorno `SENDGRID_API_KEY` y `SENDGRID_REMITENTE`.

### Cargar las variables en la sesión actual

El archivo `sendgrid.env` debe exportar ambas variables para habilitar el envío de correos:
```bash
export SENDGRID_API_KEY='SG...'
export SENDGRID_REMITENTE='tu-email@dominio.com'
```

Carga tu archivo de variables en la consola:

**En Git Bash / Linux:**
```bash
source sendgrid.env
```

### Borrar / Limpiar las variables

Si deseas remover las variables cargadas en tu consola:

**En Git Bash / Linux:**
```bash
unset SENDGRID_API_KEY
unset SENDGRID_REMITENTE
```

> **Tip Universal:** Cierra la pestaña o ventana actual de la terminal y abre una nueva. Las variables de sesión se borrarán de la memoria automáticamente.

---

## 2. Comportamiento ante Falta de Configuración (`ConfiguracionSendGridException`)

Si las variables `SENDGRID_REMITENTE` o `SENDGRID_API_KEY` no están definidas o se encuentran vacías al invocar el método `enviar()`, la estrategia `NotificacionPorEmail` no opera en un modo simulado por consola, sino que lanza de inmediato la excepción `ConfiguracionSendGridException`:

De este modo se previene el intento silencioso o erróneo de envío de correos sin credenciales válidas.

---

## 3. Validación de Excepciones con Mockito en Tests

Para verificar de forma declarativa y aislada que el sistema propaga correctamente la excepción `EnvioDeEmailException` ante fallos en la comunicación o respuesta HTTP del servicio de correos, utilizamos **Mockito**:

```java
@Test
public void testLanzaEnvioDeEmailExceptionAnteFalloDeEnvio() {
    // 1. Mockeamos la estrategia de notificación instruyendo un fallo
    EstrategiaDeNotificacion estrategiaMock = mock(EstrategiaDeNotificacion.class);
    when(estrategiaMock.enviar(anyString(), anyString()))
            .thenThrow(new EnvioDeEmailException("Error simulado al enviar por SendGrid"));

    // 2. Inyectamos el mock en la entidad Contacto
    Contacto contactoConError = new Contacto(estrategiaMock, "fallo@donatrack.org");
    Notificacion notif = new Notificacion(contactoConError, "Mensaje que fallará");

    // 3. Afirmamos que al intentar enviar la notificación se propague la excepción
    assertThrows(EnvioDeEmailException.class, () -> notif.enviar(),
            "Debe lanzarse EnvioDeEmailException cuando el envío de correo falla.");
}
```
