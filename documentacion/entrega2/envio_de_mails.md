# Guía Rápida: Configuracion de Sendgrind
---

## 1. Ejecución de Pruebas (`mvn test`)

Para ejecutar la suite de pruebas desde el directorio del servicio (`donaciones-service`), ejecuta:

```bash
mvn test
```

## 2. Configuración de SendGrid (Modo Simulado vs. Real)

El sistema opera por defecto en **Modo Simulado (*Dry-Run*)** si no detecta la API Key o el correo remitente (`SENDGRID_REMITENTE`). En este modo, los correos se imprimen por consola sin usar servicios externos.

### Cargar las variables en la sesión actual

Para activar el **Modo Real**, el archivo `sendgrid.env` debe exportar ambas variables:
```bash
export SENDGRID_API_KEY='SG...'
export SENDGRID_REMITENTE='tu-email@dominio.com'
```

Carga tu archivo de variables en la consola:

**En Git Bash / Linux:**
```bash
source sendgrid.env
```

### Borrar / Limpiar las variables (Volver al modo simulado)

Si deseas remover las variables cargadas en tu consola para volver a correr los tests en modo simulado:

**En Git Bash / Linux:**
```bash
unset SENDGRID_API_KEY
unset SENDGRID_REMITENTE
```

> **Tip Universal:** Cierra la pestaña o ventana actual de la terminal y abre una nueva. Las variables de sesión se borrarán de la memoria automáticamente.

---

## 3. Validación de Excepciones con Mockito en Tests

Para verificar de forma declarativa y aislada que el sistema propaga correctamente la excepción `EnvioDeEmailException` ante fallos en el servicio de correos, utilizamos **Mockito**:

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
