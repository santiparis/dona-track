// Mapa de las pantallas con marcadores. Los datos salen del HTML que renderiza el server:
// cada elemento con data-marcador (una tarjeta, una fila de tabla) pone un marcador en data-lat / data-lng,
// y lo que tiene adentro su <template> es lo que se ve en el popup al tocarlo.
// Los elementos que caen en el mismo punto (ej: dos entregas a la misma entidad) comparten marcador.
document.addEventListener("DOMContentLoaded", () => {
  const mapa = new maplibregl.Map({
    container: document.querySelector(".mapa"),
    style: "https://tiles.openfreemap.org/styles/liberty",
    center: [-58.38, -34.60], // Buenos Aires, hasta que se ajuste a los marcadores
    zoom: 9
  });

  const elementosPorPunto = new Map();
  document.querySelectorAll("[data-marcador]").forEach(elemento => {
    const punto = elemento.dataset.lng + "," + elemento.dataset.lat;
    if (!elementosPorPunto.has(punto)) elementosPorPunto.set(punto, []);
    elementosPorPunto.get(punto).push(elemento);
  });

  const limites = new maplibregl.LngLatBounds();
  const ventanas = [];
  elementosPorPunto.forEach(elementos => {
    const posicion = [Number(elementos[0].dataset.lng), Number(elementos[0].dataset.lat)];

    const marcador = document.createElement("div");
    marcador.className = "marcador " + elementos[0].dataset.marcador;

    const popup = document.createElement("div");
    popup.className = "popup";
    elementos.forEach(elemento => {
      const bloque = document.createElement("div");
      bloque.append(elemento.querySelector("template").content.cloneNode(true));
      popup.append(bloque);
    });

    // El popup sale siempre arriba del marcador, y al abrirlo el marcador baja a la parte de abajo del mapa
    // (120px bajo el centro) para que el popup entre
    const ventana = new maplibregl.Popup({ offset: 14, anchor: "bottom" }).setDOMContent(popup);
    ventana.on("open", () => mapa.easeTo({ center: posicion, offset: [0, 120] }));

    const marker = new maplibregl.Marker({ element: marcador })
      .setLngLat(posicion)
      .setPopup(ventana)
      .addTo(mapa);
    limites.extend(posicion);

    // Tocar el elemento (la tarjeta, la fila) lleva al mapa y, al llegar, abre el popup de su marcador
    // cerrando el que estuviera abierto
    ventanas.push(ventana);
    elementos.forEach(elemento => elemento.addEventListener("click", () => subirAlMapa(() => {
      ventanas.forEach(otra => otra.remove());
      marker.togglePopup();
    })));
  });

  if (!limites.isEmpty()) mapa.fitBounds(limites, { padding: 60, maxZoom: 13, duration: 0 });

  // Scroll animado hasta dejar el mapa centrado en la pantalla. Es más lento que el smooth del navegador
  // y arranca y frena de a poco (curva ease-in-out)
  function subirAlMapa(alLlegar) {
    const desde = window.scrollY;
    const recuadro = mapa.getContainer().getBoundingClientRect();
    const hasta = Math.max(0, desde + recuadro.top - (window.innerHeight - recuadro.height) / 2);
    const inicio = performance.now();
    const duracion = 700;
    requestAnimationFrame(function paso(ahora) {
      const t = Math.min(1, (ahora - inicio) / duracion);
      const avance = t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
      window.scrollTo(0, desde + (hasta - desde) * avance);
      if (t < 1) requestAnimationFrame(paso);
      else alLlegar();
    });
  }
});
