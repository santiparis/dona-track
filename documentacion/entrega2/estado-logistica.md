# Estado de logistica-service — Entrega 2 (al 16/09/2026)

Contexto para retomar el trabajo. El `CLAUDE.md` de la raíz tiene el enunciado completo,
pero su inventario de "hecho / pendiente" quedó desactualizado: lista como pendientes
varias cosas que ya están implementadas. Usar este archivo para el estado real.

## Proyecto

TP anual de Diseño de Sistemas. Plataforma de trazabilidad de donaciones, dividida en
**dos microservicios** que se integran por HTTP/REST (Javalin entrante, Retrofit saliente,
Jackson, repos en memoria, Java 17, Maven). Sin Spring.

- `donaciones-service` (puerto **8081**): dueño de la donación, su estado y su historial.
  CRUDs de donaciones/donantes/entidades/necesidades, algoritmos de matchmaking y el
  envío real de notificaciones.
- `logistica-service` (puerto **7070**): dueño de la **entrega** y la **ruta**. Flota de
  camiones, planificación, trazabilidad de entregas y monitoreo en tiempo real.

Regla de diseño a respetar: **logística informa hechos, donaciones decide y aplica** el
cambio de estado de la donación. Logística no duplica el estado ni el historial de la
donación, y no se comparten clases de dominio entre servicios.

## Cómo se corre (4 procesos)

1. `donaciones.Main` → 8081
2. `logistica.Main` → 7070
3. `logistica.mock.MockPlanificador` → 9090 (simula el proveedor externo de rutas)
4. `logistica.mock.MockGps` → cliente puro, reporta ubicación cada 5s

Más el jar de `logistica.scheduler.LanzadorPlanificacion`, que lo dispara cron y solo
hace un POST a `/api/planificaciones` del servidor vivo (los repos están en memoria en
ESE proceso, por eso el cron no puede hacer el trabajo por su cuenta).

## Flujo completo

```
donaciones: admin confirma la entidad destino
    │  POST /api/donaciones  ─────────────▶ logistica encola la donacion
cron 3 AM → POST /api/planificaciones
    │  batch de ≤100 donaciones + camiones ─▶ MockPlanificador responde "OK"
    │◀── callback POST /api/rutas ────────── devuelve rutas armadas + no asignadas
chofer: POST /api/rutas/{id}/inicio → ruta EN_CURSO, entregas EN_TRASLADO
    │  PATCH /api/donaciones/{id} ────────▶ donaciones notifica (donante + entidad)
entidad: POST /api/entregas/{id}/confirmacion | /rechazo | /reingreso
    │  PATCH /api/donaciones/{id} ────────▶ donaciones notifica
```

## Lo que ya está hecho en logística

- Máquinas de estado en el dominio: `Entrega` (`PENDIENTE → EN_TRASLADO →
  ENTREGADA / NO_RECIBIDA → PENDIENTE`) y `Ruta` (`PLANIFICADA → EN_CURSO`), con
  validación de transiciones. Los controllers solo traducen a HTTP.
- CRUD de camiones + endpoint de ingesta de localización.
- CRUD/lectura de rutas y entregas, con endpoints de negocio (`/inicio`,
  `/confirmacion`, `/rechazo`, `/reingreso`) en vez de un PATCH de estado crudo.
- Integración con el planificador externo completa y asincrónica: envío en lotes de 100,
  callback que arma todas las rutas antes de guardar ninguna, y re-encolado de las
  donaciones que vuelven sin asignar.
- `NotificadorEntregas` como **único punto** que le habla a donaciones, vía un PATCH
  único por donación (`EN_TRASLADO` / `ENTREGADA` / `ENTREGA_FALLIDA` + `datosAdicionales`).
- IDs `Long` con secuencia en los repositorios.
- Tests de dominio, controllers y del cliente del planificador.

## Persistencia (en curso)

El servicio **compila y corre**. Se empezó a incorporar JPA/Hibernate 5.6.12 siguiendo el
instructivo de la cátedra:

- `src/main/resources/META-INF/persistence.xml` con dos bloques: **HSQLDB en memoria**
  activo (para los tests, arranca limpio en cada corrida) y **PostgreSQL** comentado (para
  correr `logistica.Main` contra la base real). Se cambia comentando/descomentando.
  OJO: con el bloque de Postgres activo, `mvn test` corre contra la base real.
- Drivers en el pom: `postgresql 42.7.3` y `hsqldb 2.4.0`. También `commons-cli 1.5.0`,
  que hace falta para el exportador de schema y no viene transitivo con `jpa-extras`.
- Entidades mapeadas hasta ahora: **solo `Camion`**. Faltan `Ruta`, `Entrega` y
  `DonacionEncolada`. Los repositorios siguen siendo listas en memoria.
- Para ver el modelo relacional generado hay una run configuration **JpaSchemaExport**
  (main class `io.github.flbulgarelli.jpa.extras.export.JpaSchemaExport`, args
  `-o schema.sql -f`, módulo `logistica-service`). Escribe `schema.sql` en la raíz del
  servicio y se puede re-correr cada vez que se anota una entidad nueva.

Detalle a revisar del schema actual: `latitud` y `longitud` salen `not null`, porque
`Coordenadas` usa `double` primitivos. Un `Camion` recién creado tiene `localizacion` en
null hasta el primer reporte del GPS.

## Bugs abiertos

1. **El GPS nunca actualiza la posición.** `MockGps` reporta a
   `PATCH api/camiones/{patente}/localizacion` mandando `"AB123CD"`, pero
   `CamionesController.actualizarLocalizacion` hace `Long.parseLong(ctx.pathParam("id"))`.
   Cada reporte tira `NumberFormatException` → 400. Todo el monitoreo en tiempo real está
   desconectado en la práctica. Arreglo elegido: que el mock reporte por **id**
   (`List.of(1L, 2L)`), porque cambiar el endpoint a patente rompe 3 tests de
   `CamionesControllerTest` que pasan ids numéricos.

2. **Las donaciones nunca llegan a logística por el flujo vivo.** Es del otro módulo, pero
   corta el circuito entero: `AsignacionesController.asignarDonacion` (donaciones) todavía
   tiene `// TODO: Pegarle al endpoint de logistica`, y `AsignacionService.confirmarAsignacion`
   —que sí le pega y además dispara las notificaciones de "donación asignada"— se instancia
   en el `Main` de donaciones pero no se cablea a ningún endpoint: es código muerto. Hoy la
   cola de logística solo se llena posteando a mano.

3. **`EstadoRuta.COMPLETADA` no lo setea nadie** y `Ruta.entregasPendientes()` no lo llama
   nadie: una ruta queda `EN_CURSO` para siempre.

Menor: `IntegracionLogisticaController` (donaciones) quedó huérfano, no está registrado en
su `Main`. Es el resto del contrato viejo que reemplazó el PATCH único.

## Trabajo diseñado pero SIN APLICAR: link de seguimiento en vivo

El enunciado pide que la notificación de inicio de ruta incluya un enlace al mapa para
seguir la entrega **en tiempo real**. Hoy `NotificadorEntregas.avisarEnTraslado` manda una
URL hardcodeada e igual para todas las rutas.

Criterio: **en la URL va el id de la ruta, no las coordenadas**. Detrás hay un endpoint que
lee la posición del camión en el momento en que se lo abre, así el mismo link sirve 40
minutos después y muestra otra cosa. De paso cubre el dashboard de monitoreo del
requerimiento 7, porque devuelve posición **y** avance sobre la ruta.

Falta aplicar estos 4 cambios:

**a) `logistica/dto/SeguimientoDTO.java` (nuevo)**

```java
package logistica.dto;

import java.util.List;

public record SeguimientoDTO(
    Long rutaId,
    String patente,
    String estadoRuta,
    Double latitud,
    Double longitud,
    double velocidad,
    List<ParadaSeguimientoDTO> paradas
) {
  public record ParadaSeguimientoDTO(
      Long entregaId,
      String destino,
      String entidadNombre,
      String estado
  ) {}
}
```

**b) `RutasController`: nuevo endpoint de lectura**

```java
  public void seguimiento(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      Ruta ruta = rutasRepository.buscarPorId(id)
          .orElseThrow(() -> new NoSuchElementException("Ruta no encontrada: " + id));

      ctx.json(this.armarSeguimiento(ruta));
    } catch (NumberFormatException e) {
      ctx.status(400).json(new ErrorResponse("ID inválido"));
    } catch (NoSuchElementException e) {
      ctx.status(404).json(new ErrorResponse(e.getMessage()));
    }
  }

  // se lee en cada request: devuelve donde esta el camion AHORA, no donde estaba al iniciar
  private SeguimientoDTO armarSeguimiento(Ruta ruta) {
    Camion camion = ruta.getCamion();
    Coordenadas ubicacion = camion.getLocalizacion();

    List<ParadaSeguimientoDTO> paradas = ruta.getEntregas().stream()
        .map(entrega -> new ParadaSeguimientoDTO(
            entrega.getId(),
            entrega.getDestino(),
            entrega.getEntidadNombre(),
            entrega.getEstado().name()))
        .toList();

    return new SeguimientoDTO(
        ruta.getId(),
        camion.getPatente(),
        ruta.getEstado().name(),
        ubicacion == null ? null : ubicacion.getLatitud(),
        ubicacion == null ? null : ubicacion.getLongitud(),
        camion.getVelocidad(),
        paradas
    );
  }
```

**c) `Main`: registrar la ruta**

```java
      config.routes.get("/api/rutas/{id}/seguimiento", rutasController::seguimiento);
```

**d) `NotificadorEntregas`: armar la URL con el id**

```java
  private static final String URL_SEGUIMIENTO = "http://localhost:7070/api/rutas/%d/seguimiento";

  public void avisarEnTraslado(Ruta ruta) {
    String urlSeguimiento = String.format(URL_SEGUIMIENTO, ruta.getId());
    ruta.getEntregas().forEach(entrega -> this.notificarEstado(entrega, EN_TRASLADO, urlSeguimiento));
  }
```

Cambia la firma (antes recibía `List<Entrega>`), así que en `RutasController.iniciarRuta`
pasa a ser `notificadorEntregas.avisarEnTraslado(ruta);` y en `RutasControllerTest` la
verificación pasa a `verify(notificadorEntregas).avisarEnTraslado(ruta);`.

## Pendiente sin resolver: comprobante de entrega

El enunciado pide que la notificación de entrega exitosa incluya un **comprobante con
fecha, hora y camión responsable**. Hoy `avisarEntregada` manda solo la patente en
`datosAdicionales`, y del lado de donaciones `Notificador.entregaConfirmada` usa
`LocalDate.now()` — que no tiene hora, aunque el mensaje diga "Fecha/Hora".

Decisiones abiertas, en orden de importancia:

1. **Dónde se guarda el número de comprobante.** Si solo se concatena en el mensaje, no
   queda en ningún campo de `Entrega`, `Ruta`, `Donacion` ni `RegistroCambioEstado`, y no
   hay forma de verificarlo después. Un comprobante que el sistema no puede buscar no es
   un comprobante.
2. **Uno por entrega o uno por donación.** `NotificadorEntregas.notificarEstado` itera las
   donaciones de la entrega y manda un PATCH por cada una. Si el número se genera dentro
   del loop, una sola descarga física en una sola puerta genera N comprobantes distintos.
3. **Quién pone el timestamp.** Hoy lo genera donaciones al procesar el PATCH, no logística
   cuando la entidad confirmó. Si el PATCH se reintenta o se demora, el comprobante dice
   una hora que no es la de la entrega.

Relacionado: `avisarFallida` manda `null` como motivo y el endpoint
`POST /api/entregas/{id}/rechazo` no lee body, así que a las personas administradoras les
llega "Motivo: null". El enunciado pide la justificación del incidente.

## Otros huecos vs. el enunciado

- **Fotos** que carga la entidad al confirmar la recepción: no está modelado en `Entrega`.
- **Camión responsable**: se resuelve vía `buscarRutaPorEntregaId`, pero no queda
  registrado en la entrega.
- **Documentación**: `README_SegundaEntrega.md` está vacío. Faltan las justificaciones de
  diseño (con foco en la estrategia de división en microservicios, que el enunciado
  remarca) y los diagramas de clases y despliegue actualizados.

## Convenciones del repo

- IDs `Long` asignados por el repositorio con secuencia. No usar contadores en el dominio.
- Endpoints con significado de negocio por sobre setters de estado genéricos.
- Validar las transiciones en el dominio; el controller traduce la excepción a HTTP
  (404 `NoSuchElementException`, 409 `IllegalStateException`, 400 `NumberFormatException`).
- Jackson serializa por getters públicos: toda entidad que viaje por HTTP tiene que
  exponerlos.
- Camiones sin CRUD REST era la decisión vieja; hoy el CRUD existe y está bien, porque el
  enunciado pide "gestión de flota".
