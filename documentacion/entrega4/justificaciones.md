# Justificaciones de Diseño — Entrega 4: Cliente liviano

Alcance de este documento por ahora: los mapas de la UI de **Donaciones** (`donaciones-service`), en Donaciones entregadas (pública) y en el Seguimiento de entregas del donante y de la entidad.

## Mapas: MapLibre GL + OpenFreeMap

* **MapLibre GL** es la librería JS que dibuja el mapa en el navegador (zoom, marcadores, popups). **OpenFreeMap** sirve los datos de OpenStreetMap y el estilo `liberty`, que le indica a MapLibre cómo pintar cada cosa. Una es la librería y la otra la fuente de datos: se usan juntas.
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
