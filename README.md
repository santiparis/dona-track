# DonaTrack

**Trabajo Práctico Anual de Diseño de Sistemas · UTN**

DonaTrack permite seguir una donación desde que llega al depósito hasta que la recibe una entidad beneficiaria. El equipo del depósito registra los bienes y les asigna un destino. El donante puede consultar el recorrido y la entidad puede confirmar la recepción.

## Primeros pasos

Con Docker y Docker Compose instalados, ejecutá este comando desde la raíz del repositorio:

```sh
docker compose up -d --build
```

Abrí [DonaTrack](http://localhost:8081) y elegí **Quiero formar parte**. Si vas a donar, podés registrarte como persona humana o jurídica. Si representás a una entidad, elegí **Quiero recibir donaciones**.

Para detener el proyecto y conservar los datos, ejecutá `docker compose down`.

## Cómo está organizado

| Servicio | Qué hace | Base de datos |
| --- | --- | --- |
| **Donaciones** · puerto `8081` | Muestra la web y gestiona cuentas, bienes, asignaciones y recepciones. | MySQL |
| **Logística** · puerto `7070` | Gestiona camiones, rutas y estados de entrega. | PostgreSQL |

Los dos servicios están escritos en Java 17. El entorno local incluye simuladores de GPS y planificación para probar el recorrido de una entrega.

## Documentación

- **Entregas:** [Primera entrega](documentacion/entrega1/README_PrimeraEntrega.md) · [Segunda entrega](documentacion/entrega2/README_SegundaEntrega.md)
- **Cuentas y avisos:** [Registro, acceso y roles](documentacion/entrega4/login.md) · [Notificaciones por correo y SMS](documentacion/entrega4/notificaciones.md)
- **Despliegue:** [Decisiones de diseño y Azure](documentacion/entrega4/justificaciones.md) · [Tareas programadas en las máquinas virtuales](documentacion/entrega4/crons-azure.md)
