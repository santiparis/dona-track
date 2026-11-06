# Justificaciones de Diseño — Entrega 4: Cliente liviano

Este documento explica las decisiones sobre los mapas de la UI de **Donaciones** y el despliegue de DonaTrack en **Azure**.

## Mapas: MapLibre GL + OpenFreeMap

* **[MapLibre GL](https://maplibre.org/maplibre-gl-js/docs/)** es la librería JS que dibuja el mapa en el navegador (zoom, marcadores, popups). **[OpenFreeMap](https://openfreemap.org/)** sirve los datos de OpenStreetMap y el estilo `liberty`, que le indica a MapLibre cómo pintar cada cosa. Una es la librería y la otra la fuente de datos: se usan juntas.
* **Por qué:** las dos son gratuitas y no piden API key, así que no hay credenciales que guardar ni cuota de un plan gratis. MapLibre se carga por CDN y no suma dependencias al `pom`.
* **Alternativas descartadas:**
  * Estilo de demo de MapLibre: solo trae países y fronteras, sin calles ni ciudades; no sirve para seguir un camión.
  * MapTiler o Stadia: piden API key y tienen límites en el plan gratis.
  * Servir los tiles nosotros: infraestructura que el enunciado no pide.

## Los datos del mapa salen del HTML que arma el server

* Cada elemento que va al mapa (una tarjeta de entrega, una fila de camión) lleva `data-marcador`, `data-lat` y `data-lng`, y adentro un `<template>` con el contenido de su popup. `mapa.js` recorre esos elementos y arma los marcadores.
* **Por qué:** la vista la sigue armando solo el server con Handlebars. El mapa y la lista no pueden mostrar datos distintos, porque salen de los mismos elementos. `mapa.js` no conoce los campos de una entrega ni de un camión, así que el mismo archivo sirve para las tres pantallas y cambiar los datos no obliga a tocar el JS.
* **Alternativas descartadas:**
  * Un endpoint JSON que el JS consulte aparte: deja dos fuentes para la misma pantalla y suma JS a un cliente que tiene que ser liviano.
  * Pasar cada objeto serializado en un atributo con un helper `json`: obliga a armar el popup en JS con los nombres de los campos, duplicando lo que ya hace el template.

## Comportamiento del mapa

* Los elementos que caen en el mismo punto (ej: dos entregas a la misma entidad) comparten marcador y su popup los lista a todos.
* El valor de `data-marcador` es también la clase del marcador: en el seguimiento de la entidad, su ubicación (`entidad`) se pinta de otro color que los camiones (`camion`).
* El popup sale siempre arriba del marcador y el mapa se desplaza para que entre; si el contenido no entra, scrollea.
* Tocar una tarjeta o una fila sube hasta el mapa con un scroll animado y, al llegar, abre el popup de su marcador.
* Los scripts del mapa se cargan con `defer`, para que el mapa se arme con todos los CSS ya aplicados. Sin eso, a veces MapLibre medía el recuadro sin alto y el mapa quedaba cortado.

## Un solo seguimiento para donante, entidad y admin

* Las pantallas 7 y 12 del enunciado de la E4 piden lo mismo para donante y entidad: los camiones de sus entregas activas, con ubicación y última actualización. Por eso usan la misma plantilla (`seguimiento.hbs`) con las mismas columnas; cambian los textos y qué camiones entran, que filtra el controller.
* **Monitoreo del admin (requerimiento de la Entrega 2, no de la E4):** la E2 pide un dashboard administrativo con la posición y el avance de los camiones en tiempo real. Las pantallas mínimas de la E4 no lo incluyen, pero para cubrir ese requerimiento el admin tiene `/admin/seguimiento`, con la misma plantilla y todos los camiones en ruta, sin filtro. La columna "Avance" (entregas hechas de la ruta) sale de ese requerimiento y la ven los tres perfiles.
* **Por qué:** una pantalla que muestra lo mismo se resuelve con una plantilla; tres copias se desincronizan al primer cambio.

## JavaScript solo donde hace falta

* El único JS de estas pantallas es el del mapa. "Ver más entregas" usa `<details>`, sin JS, igual que los otros desplegables de la app.

## Fotos de las entregas

* `foto` es una URL, igual que `Bien.foto` en el dominio, así que conectar la base no cambia la vista. En el mock son fotos libres de Wikimedia Commons referenciadas por URL.
* **Alternativa descartada:** guardar las fotos en `assets/img`: suma peso al repositorio para datos que son de prueba.

## Pendientes y simplificaciones a propósito

* Los datos de las tres pantallas siguen saliendo de `DatosMock`.
* `EntidadBeneficiaria` no tiene coordenadas en el dominio, solo dirección. Falta decidir cómo se obtienen para el mapa público.
* La ubicación real de los camiones vive en Logística; falta definir cómo la pide Donaciones y cómo se sabe qué camiones llevan donaciones de un donante o de una entidad.
* El seguimiento se actualiza recargando la página (botón Actualizar).

## Despliegue en Azure

Preparamos los dos microservicios para correr con Docker Compose. La configuración de Azure contempla una máquina virtual para **Donaciones**, con su base MySQL, y otra para **Logística**, con su base PostgreSQL. El navegador entra a la interfaz de Donaciones por el puerto `8081`. Logística atiende su API en el puerto `7070` y conserva los simuladores de GPS y del planificador como contenedores separados.

* **Imágenes reproducibles.** Los Dockerfiles compilan los servicios con Maven y Java 17. La imagen que arranca en cada VM lleva el JAR y el entorno de Java necesario para ejecutarlo. El mismo proceso de construcción sirve en local y en Azure.
* **Un Compose base y un archivo para Azure.** `compose.yaml` define los servicios, sus dependencias y sus volúmenes. En local, Donaciones y Logística se encuentran por el nombre del contenedor. `docker-compose.azure.yaml` se aplica encima y cambia esas direcciones para que los servicios se comuniquen entre VMs. Donaciones usa `LOGISTICA_IP` y Logística usa `DONACIONES_IP`. Así se puede cambiar la dirección de una VM sin recompilar ni editar el código Java.
* **Una base por servicio.** Compose guarda los datos en volúmenes de Docker para que sobrevivan al reemplazo del contenedor. También espera a que cada base esté lista antes de iniciar su API. En Azure se quita la publicación de los puertos `3306` y `5432`, porque solo los contenedores de cada servicio necesitan conectarse a su base.

La separación en dos VMs mantiene la división del TP también en el despliegue. Cada servicio puede arrancarse y actualizarse por separado, y la comunicación entre ellos sigue siendo por HTTP.
