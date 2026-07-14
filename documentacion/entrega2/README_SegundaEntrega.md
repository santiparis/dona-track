
# Justificaciones de Diseño

## Eventos y Notificaciones: Patrón Observer

El sistema necesita notificar a distintos actores (donantes, entidades beneficiarias, administradores) ante eventos relevantes en el ciclo de vida de una donación.

El problema concreto surgió con el evento de **entrega no satisfactoria**: los administradores no forman parte del grafo donante–donación–entidad, por lo que el emisor del evento debía obtener y pasar la lista de administradores por parámetro, generando un alto acoplamiento.

Se aplicó el patrón **Observer** para desacoplar la emisión de eventos de la lógica de notificación:

* **Eventos** (`CambioDeEstadoEnDonacion`): objetos de datos puros que representan hechos del dominio, sin lógica de notificación.
* **Listeners** (`Listener`): cada uno se suscribe a un tipo de evento y encapsula a quién y cómo notificar. El `EntregaNoSatisfactoriaListener` resuelve los administradores por sí mismo a través de un repositorio inyectado.
* **Publicador** (`PublicadorDeEventos`): mantiene las suscripciones y despacha eventos a los listeners registrados. Se instancia en `Main` y se inyecta como dependencia (sin Singleton).

**Principios aplicados:** SRP (cada clase tiene una única responsabilidad), OCP (agregar un evento nuevo no requiere modificar los existentes), DIP (los emisores dependen de abstracciones, no de listeners concretos).

Ver diagrama de clases: `dc_eventos.puml`.
