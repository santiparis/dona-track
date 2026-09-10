# Justificaciones de Diseño — Correcciones Entrega 2

## Notificaciones: se elimina el Observer

**Por qué se saca el esquema anterior** (`domain/eventos`, 11 clases):

* No era un Observer: cada evento tenía **un solo** listener, suscripto una única vez en `Main`. Nunca hubo múltiples interesados ni suscripción dinámica.
* `CambioDeEstadoEnDonacion` era una interfaz marcadora vacía: cada listener arrancaba con un _downcast_. El polimorfismo no hacía nada.
* Los listeners eran cadenas de getters (`evento.getDonacion().getDonante().notificar(...)`): el comportamiento vivía afuera del dueño de los datos.
* 11 clases para 4 notificaciones.

**Decisión:** un método de intención por evento de negocio en `Donacion` (`asignarA`, `iniciarTraslado`, `confirmarEntrega`, `registrarEntregaFallida`). Cada uno **solo cambia el estado y registra el historial**; el aviso lo manda el **controller**, que resuelve asignación y notificación juntas. `cambiarEstado` queda privado y `EstadoDonacion` es un enum de datos, sin comportamiento.

**Criterios:**

* SRP: notificar no es responsabilidad de `Donacion`, que solo gobierna su estado y su historial. Las llamadas a `notificar` quedan en el controller, que es capa de aplicación.
* YAGNI: son cuatro notificaciones y el enunciado no pide agregar destinatarios en runtime, que es lo único que justificaría un Observer.
* Desaparece el `if/else if` por estado: la rama la resuelve el llamador al elegir el método.

**Alternativas descartadas:**

* _Enum con un método por constante:_ mete comportamiento en un enum que el resto del sistema usa como dato (`valueOf`, `RegistroCambioEstado<EstadoDonacion>`).
* _Enum con la plantilla del mensaje como `String`:_ obliga a `null` en los estados que no notifican y a `String.format` con parámetros distintos por estado.
* _Clases de estado (State):_ único que cumple literal "polimorfismo en cambio de estado", pero **ese polimorfismo no se usaría**: solo 4 de 7 estados notifican y cada uno a destinatarios y con datos distintos. Cuesta ~7 clases y rompe `valueOf()`.
* _Un objeto `Aviso`:_ son los mismos eventos con otro nombre.

**Administradoras:** la justificación anterior eligió Observer porque no están en el grafo donante–donación–entidad. Se resuelve sin Observer: el **controller** conoce el repositorio y hace `admin.notificar(mensaje)`. El dominio no recibe la lista ni conoce el repo, cumpliendo _"el repositorio no va en el DC"_.

**Clases eliminadas** (las 11 de `domain/eventos`, más su test):

* `CambioDeEstadoEnDonacion`: interfaz marcadora vacía, solo forzaba el _downcast_.
* `DonacionAsignadaEvent`, `InicioRutaEvent`, `EntregaRealizadaEvent`, `EntregaNoSatisfactoriaEvent`: datos que hoy son parámetros del método.
* `Listener` y sus 4 implementaciones: su contenido es hoy una llamada directa a `notificar`.
* `PublicadorDeEventos`: sin múltiples suscriptores no despachaba nada.
* `EventosLogisticaYAsignacionTest`: probaba los listeners; lo reemplaza `DonacionCambioDeEstadoTest`.

**Impacto:** el camión, el motivo del fallo y la URL del mapa entran **por parámetro** en el método correspondiente, no como atributos de `Donacion`; el motivo se guarda como `justificacion` del `RegistroCambioEstado`.

**Estado actual:** las llamadas a `notificar` están comentadas dentro de los métodos de `Donacion` hasta que se implemente el paso que las mueve al controller.

Ver diagrama: [dc_notificaciones_donacion.puml](./dc_notificaciones_donacion.puml)
