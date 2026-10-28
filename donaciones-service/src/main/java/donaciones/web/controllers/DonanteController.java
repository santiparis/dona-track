package donaciones.web.controllers;

import donaciones.web.DatosMock;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DonanteController {

  public Map<String, Object> entidades(Context ctx) {
    // La búsqueda viaja en la URL: ?q=...&subcategoria=...
    String busqueda = Objects.requireNonNullElse(ctx.queryParam("q"), "");
    String subcategoria = Objects.requireNonNullElse(ctx.queryParam("subcategoria"), "");

    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Entidades beneficiarias");
    model.put("rol", "donante");
    model.put("seccion", "entidades");
    model.put("usuario", DatosMock.donante());   // MOCK

    // List<EntidadBeneficiaria> entidades = entidadRepo.buscar(busqueda, subcategoria);
    model.put("entidades", DatosMock.entidadesConNecesidades().stream()
        .filter(e -> ((String) e.get("nombre")).toLowerCase().contains(busqueda.toLowerCase()))
        .filter(e -> subcategoria.isEmpty() || ((List<?>) e.get("necesidades")).stream()
            .anyMatch(n -> subcategoria.equals(((Map<?, ?>) n).get("subcategoria"))))
        .toList());   // MOCK
    model.put("busqueda", busqueda);
    model.put("subcategoriaElegida", subcategoria);
    // Todas las subcategorías, de todas las categorías
    model.put("subcategorias", DatosMock.categorias().stream()
        .flatMap(c -> DatosMock.subcategorias(c.get("valor")).stream())
        .toList());   // MOCK
    return model;
  }

  public Map<String, Object> notificaciones() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Notificaciones");
    model.put("rol", "donante");
    model.put("seccion", "notificaciones");
    model.put("usuario", DatosMock.donante());   // MOCK
    model.put("aviso", "También te llegan por tu medio preferido: correo.");   // MOCK
    // List<Notificacion> notificaciones = notificacionRepository.obtenerTodas().stream().filter(n -> n.getReceptorId().equals(idDonante)).toList();
    model.put("notificaciones", DatosMock.notificaciones().stream()
        .filter(n -> "donante".equals(n.get("destinatario")))
        .toList());   // MOCK
    return model;
  }

  public Map<String, Object> seguimiento() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Seguimiento de entregas");
    model.put("rol", "donante");
    model.put("seccion", "seguimiento");
    model.put("usuario", DatosMock.donante());   // MOCK
    model.put("subtitulo", "Camiones que hoy llevan donaciones tuyas.");
    model.put("textoMapa", "Un marcador por camión y los destinos de tus donaciones");
    // Las ubicaciones vienen de logistica-service por HTTP (endpoint a definir en LogisticaAPICalls):
    // List<UbicacionCamionDTO> camiones = logisticaAPICalls.camionesConDonacionesDe(idDonante).execute().body();
    model.put("camiones", DatosMock.camionesEnRuta());   // MOCK
    return model;
  }
}
