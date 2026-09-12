Trabajo Práctico Anual DDS - DonaTrack

## Documentación de entregas

- [_README_PrimeraEntrega_](./documentacion/entrega1/README_PrimeraEntrega.md)
- [_README_SegundaEntrega_](./documentacion/entrega2/README_SegundaEntrega.md)

## Feature agregada: procesamiento de sugerencias por cron

Se incorporó un flujo simple para ejecutar la generación de sugerencias de asignación desde un cron, respetando la estructura actual del proyecto y sin eliminar aún los services.

### Qué se implementó

1. Un controller nuevo: [donaciones-service/src/main/java/donaciones/controller/AsignacionesController.java](donaciones-service/src/main/java/donaciones/controller/AsignacionesController.java)
   - toma las donaciones con estado `EN_DEPOSITO` desde el repositorio de donaciones,
   - toma todas las entidades beneficiarias con necesidades cargadas,
   - ejecuta el `OrganizadorAsignaciones` con `CompatibilidadSemantica`,
   - guarda las sugerencias en un repositorio propio.

2. Un repositorio nuevo para persistir sugerencias: [donaciones-service/src/main/java/donaciones/repository/SugerenciaAsignacionRepository.java](donaciones-service/src/main/java/donaciones/repository/SugerenciaAsignacionRepository.java)

3. Un main programable para crontab: [donaciones-service/src/main/java/planificacion/ProcesarAsignaciones.java](donaciones-service/src/main/java/planificacion/ProcesarAsignaciones.java)
   - ejecuta el flujo completo,
   - imprime un resumen por donación,
   - informa cuántas sugerencias quedaron guardadas.

4. Configuración de packaging en Maven para generar un JAR ejecutable del cron: [donaciones-service/pom.xml](donaciones-service/pom.xml)

### Cómo se ejecuta

Desde la raíz del proyecto:

```bash
mvn package
java -jar donaciones-service/target/procesador-asignaciones-cron-jar-with-dependencies.jar
```

### Qué hace exactamente el cron

El proceso hace lo siguiente:

1. Lee todas las donaciones del repositorio.
2. Filtra únicamente las que están en estado `EN_DEPOSITO`.
3. Lee todas las entidades beneficiarias con necesidades cargadas.
4. Para cada donación, ejecuta el organizador para obtener una `SugerenciaAsignacion`.
5. Guarda esa sugerencia asociada al `id` de la donación.

### Nota de arquitectura

Este cambio sigue la idea que pediste de MVC y deja el foco en controllers, repositorios y dominio. Los services existentes no fueron eliminados todavía, solo se agregó el flujo necesario para que el cron pueda correr con la estructura actual.

### Ejemplo de crontab

```cron
0 2 * * * cd /home/fear/Desktop/facu/DDS/dona-track && java -jar donaciones-service/target/procesador-asignaciones-cron-jar-with-dependencies.jar >> /tmp/dona-track-cron.log 2>&1
```

Este ejemplo corre el job a las 2:00 AM, cuando usualmente hay menos tráfico.

