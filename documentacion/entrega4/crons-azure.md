# Crons en las VMs de Azure

Los dos procesos se lanzan como contenedores efímeros de Docker Compose cada diez minutos. Así reutilizan la red interna y la configuración de las APIs; las bases de datos no necesitan publicar sus puertos en la VM.

## VM Donaciones

El proceso de asignaciones se ejecuta dentro de la imagen `donaciones-api`, que recibe `DB_URL` desde `compose.yaml` (`jdbc:mysql://mysql:3306/donaciones`). El comando reemplaza el arranque normal de la API y ejecuta la clase `ProcesarAsignaciones`.

Desde `/home/azureuser/dona-track`, reconstruir la imagen después de actualizar el código:

```bash
docker compose -f compose.yaml -f docker-compose.azure.yaml up -d --build donaciones-api
```

Probar una ejecución manual:

```bash
docker compose -f compose.yaml -f docker-compose.azure.yaml run --rm --no-deps --entrypoint java donaciones-api -cp app.jar planificacion.ProcesarAsignaciones
```

Esta tarea elimina las sugerencias guardadas y las vuelve a calcular para donaciones en depósito. Hacer la prueba con los datos de demostración previstos.

Instalar el crontab:

```bash
crontab documentacion/entrega2/crontab-dona-track
crontab -l
```

## VM Logística

El servicio `planificador` ya es un contenedor de una sola ejecución. Comprueba la conectividad con la API:

```bash
docker compose -f compose.yaml -f docker-compose.azure.yaml run --rm --no-deps planificador
```

Instalar el crontab:

```bash
crontab documentacion/entrega2/crontab-logistica
crontab -l
```

## Comprobar las ejecuciones

En cada VM, confirmar que el daemon está activo y consultar el log correspondiente después de esperar hasta diez minutos:

```bash
systemctl is-active cron
tail -n 80 logs/procesador-asignaciones.log
```

En la VM Logística, usar `tail -n 80 logs/planificacion-rutas.log`.

Cada VM tiene su propio archivo de log; consultar solo el que corresponda. `flock` evita que se superpongan dos ejecuciones si una tarda más de diez minutos.

## Notificaciones de inactividad

`NotificarPersonasInactivas` no se instala todavía en cron. Su método `main` actual agrega personas de prueba a la base y no debe ejecutarse sobre la base de la VM. Para habilitarlo hace falta reemplazar esos datos ficticios por un proceso basado en personas reales y registrar interacciones para que la inactividad se calcule correctamente.
