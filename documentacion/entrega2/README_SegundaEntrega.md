
# Justificaciones de Diseño

## Eventos y Notificaciones: Patrón Observer

* El sistema necesita notificar a distintos actores (donantes, entidades beneficiarias, administradores) ante eventos relevantes en el ciclo de vida de una donación.


* El problema concreto surgió con el evento de _entrega no satisfactoria_: los administradores no forman parte del grafo donante-donación-entidad, por lo que el emisor del evento debía obtener y pasar la lista de administradores por parámetro, generando un alto acoplamiento.


* Se aplicó el patrón **Observer** para desacoplar la emisión de eventos de la lógica de notificación:

   * **Eventos** (`CambioDeEstadoEnDonacion`): objetos de datos puros que representan hechos del dominio, sin lógica de notificación.

   * **Listeners** (`Listener`): cada uno se suscribe a un tipo de evento y encapsula a quién y cómo notificar. El `EntregaNoSatisfactoriaListener` resuelve los administradores por sí mismo a través de un repositorio inyectado.

   * **Publicador** (`PublicadorDeEventos`): mantiene las suscripciones y despacha eventos a los listeners registrados. Se instancia en `Main` y se inyecta como dependencia (sin Singleton).


* Principios aplicados: SRP (cada clase tiene una única responsabilidad), OCP (agregar un evento nuevo no requiere modificar los existentes), DIP (los emisores dependen de abstracciones, no de listeners concretos).

Ver diagrama de clases: [dc_eventos.puml](./diagrama_eventos_notificaciones.puml)

## Integración GPS

* El sistema requiere seguir en tiempo real las entregas, mostrando en un dashboard administrativo la posición actual y el avance de los camiones sobre la ruta asignada.


* Ante las dos alternativas propuestas, se optó por la utilización del **dispositivo GPS configurable**. Para integrarlo, se tomo la decision de modelar el dispositivo como un cliente externo que interactua con el sistema.


* Se expone un **endpoint especifico en el microservicio de logistica** cuya unica responsabilidad es recibir y procesar coordenadas geograficas y la velocidad del camion.

## Algoritmos de asignación

* Se implementa un _Patrón Strategy_ para poder cumplir con el requerimiento de poder desarrollar mas algoritmos a futuro además de los requeridos por la consigna.


* El `OrganizadorAsignaciones` permite instanciar ambas estrategias y generar el filtro requerido de la lista de `EntidadBeneficiaria`


* `SugerenciaAsignacion` es un **public record** utilizado para mostrar ambas ejecuciones de los algoritmos y las coincidentes, como lo requiere la consigna.

