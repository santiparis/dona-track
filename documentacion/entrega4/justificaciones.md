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

## Fotos de recepción

La entidad destinataria adjunta de una a tres fotos JPG o PNG al confirmar una donación en traslado. El servidor revisa que cada archivo sea una imagen legible de hasta 5 MB y 16 megapíxeles. Azure guarda los archivos en un contenedor privado. MySQL guarda la relación entre el ID de la donación, el identificador de cada archivo y su tipo en `foto_recepcion`. La tabla se crea con el esquema de Hibernate.

El donante puede abrir las fotos desde su historial y la entidad desde «Ver fotos» en Donaciones asignadas. Donaciones comprueba el titular antes de pedir el archivo a Azure. El navegador recibe la imagen a través de DonaTrack y la credencial de Azure queda en el servidor.

Si falla la carga o Logística rechaza la confirmación, Donaciones revierte la transacción y pide borrar los archivos que pudo subir. Una falla de red también puede impedir esa limpieza. Logística acepta que se reintente una confirmación que ya recibió.

### Configuración y prueba

1. En Azure, crear un contenedor de blobs con acceso anónimo deshabilitado. Generar un SAS para ese contenedor con permisos de lectura, creación, escritura y eliminación, vigencia limitada y protocolo HTTPS.
2. Configurar `AZURE_STORAGE_CONTAINER_URL` con la URL del contenedor, sin el signo de interrogación ni el SAS. Configurar `AZURE_STORAGE_SAS` con el token en el entorno de Donaciones. El Compose base ya pasa ambas variables al contenedor. Guardar el token fuera del repo.
3. Reconstruir y levantar los servicios con `docker compose up -d --build` o con los dos Compose en el entorno de Azure. Para confirmar, la donación tiene que estar en traslado y asociada a una entrega real en Logística.
4. Iniciar sesión como la entidad destinataria, abrir «Confirmar recepción», adjuntar una foto y confirmar. Abrir «Ver fotos» y repetir desde el historial del donante. La misma URL debe quedar bloqueada para otra cuenta. Reiniciar Donaciones y comprobar que la foto siga disponible.

La prueba automatizada usa HTTP y MySQL se sustituye por HSQLDB en memoria. Las respuestas de Blob Storage y Logística se simulan localmente. Se ejecuta con `mvn -pl donaciones-service "-Dtest=RegistroHttpTest" test`.

## Foto opcional del bien al registrar una donación

El formulario administrativo permite adjuntar una foto JPG o PNG de hasta 5 MB a cada bien. El servidor valida los bytes de la imagen y los sube al mismo contenedor privado de Azure Blob Storage que usa para las fotos de recepción. MySQL guarda en `bien.foto` una referencia interna (`blob:<id>:<tipo>`), nunca la credencial del contenedor.

La administradora, el donante y la entidad pueden ver la foto desde sus pantallas de donaciones mediante DonaTrack. Los endpoints comprueban el rol y la pertenencia antes de leer una foto privada. Existe además una ruta pública que solo entrega fotos de donaciones ya marcadas como entregadas. Las fotos se borran de Blob Storage después de eliminar una donación; si falla el guardado de la donación, se intenta retirar el archivo subido.

El mapa público de entregas todavía se alimenta de `DatosMock`; la foto registrada ya se ve en el depósito, en las donaciones del donante y en las de la entidad. Integrar las donaciones reales en ese mapa requiere resolver las coordenadas de las entidades, que siguen pendientes.

## Menú del celular: cuenta arriba, Cerrar sesión abajo

En el celular, el menú hamburguesa junta el menú del rol, los links públicos y la sesión. El nombre de la cuenta va arriba de todo, para que se vea enseguida con qué cuenta se está operando: es el encabezado de cuenta que propone Material Design para el menú lateral. «Cerrar sesión» va abajo de todo y separado del resto, porque se usa poco y no se puede deshacer: lejos de los links que se tocan seguido, se evita apretarlo sin querer. Es el mismo orden que usan apps conocidas como Gmail o Mercado Libre.

Referencias: guía del *Navigation drawer* de Material Design 3 (m3.material.io), Human Interface Guidelines de Apple (navegación) y los artículos de Nielsen Norman Group sobre menús hamburguesa.

## Registrar donación: un bien por vez, un solo envío

La administradora carga los bienes de a uno: «+ Agregar bien» abre un recuadro con los datos de un bien y «Agregar a la lista» lo suma a una tabla, donde se puede editar o quitar. Así no hace falta mostrar filas vacías de entrada, que antes rompían el envío si quedaban sin completar, y la pantalla sirve igual para 1 bien o para 80.

La donación se sigue registrando en una única operación, como pide el enunciado: la tabla no guarda nada en el servidor. Cada fila lleva sus datos en inputs ocultos (`bienes[0].subcategoria`, `bienes[0].cantidad`, ...) y «Registrar donación» es el submit normal del formulario, que manda todos los bienes juntos para que el sistema arme una donación por subcategoría. Por eso no hay rutas nuevas y el controller lee los bienes igual que antes.

Es el segundo lugar con JavaScript propio (`donacion-nueva.js`), porque agregar filas sin recargar la página no se puede hacer solo con HTML: sin JS, cada bien nuevo sería un pedido al servidor y se perdería lo ya escrito. El archivo maneja la tabla y conserva los archivos de foto seleccionados al editar una fila. Mostrar estado o vencimiento según lo que pida la subcategoría elegida lo resuelve el CSS (`:has`), sin JS.
