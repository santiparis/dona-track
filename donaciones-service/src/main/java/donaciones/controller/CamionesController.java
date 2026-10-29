package donaciones.controller;

import donaciones.retrofit_client.LogisticaAPICalls;
import donaciones.web.DatosMock;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CamionesController {

  private final LogisticaAPICalls logisticaAPICalls;

  public CamionesController(LogisticaAPICalls logisticaAPICalls) {
    this.logisticaAPICalls = logisticaAPICalls;
  }

  public Map<String, Object> camiones(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Camiones");
    model.put("rol", "admin");
    model.put("seccion", "camiones");
    model.put("usuario", DatosMock.admin());   // MOCK

    // Los camiones son de logistica-service: se piden por HTTP a GET /api/camiones (falta sumarlo a LogisticaAPICalls)
    // List<CamionDTO> camiones = logisticaAPICalls.getCamiones().execute().body();
    List<Map<String, String>> camiones = DatosMock.camiones();   // MOCK
    model.put("camiones", camiones);

    // ?editar={id} carga ese camión en el formulario; si no, el formulario queda vacío para dar de alta
    String idEditar = ctx.queryParam("editar");
    model.put("camion", camiones.stream()
        .filter(c -> c.get("id").equals(idEditar))
        .findFirst()
        .orElse(Map.of()));
    return model;
  }

  public void crearCamion(Context ctx) {
    // Sin conexión todavía: acá va logisticaAPICalls.crearCamion(...) contra POST /api/camiones
    ctx.redirect("/admin/camiones");
  }

  public void modificarCamion(Context ctx) {
    // Sin conexión todavía: acá va logisticaAPICalls.modificarCamion(id, ...) contra PUT /api/camiones/{id}
    ctx.redirect("/admin/camiones");
  }

  public void eliminarCamion(Context ctx) {
    // Sin conexión todavía: acá va logisticaAPICalls.eliminarCamion(id) contra DELETE /api/camiones/{id}
    ctx.redirect("/admin/camiones");
  }

  public Map<String, Object> seguimientoDonante() {
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

  public Map<String, Object> seguimientoEntidad() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Seguimiento de entregas");
    model.put("rol", "entidad");
    model.put("seccion", "seguimiento");
    Map<String, Object> entidad = DatosMock.entidad();   // MOCK
    model.put("usuario", entidad);
    model.put("subtitulo", "Camiones en camino con donaciones para tu entidad.");
    model.put("textoMapa", "Un marcador por camión y la ubicación de tu entidad");
    // Las ubicaciones vienen de logistica-service por HTTP (endpoint a definir en LogisticaAPICalls):
    // List<UbicacionCamionDTO> camiones = logisticaAPICalls.camionesConDestino(idEntidad).execute().body();
    model.put("camiones", DatosMock.camionesEnRuta().stream()
        .filter(c -> c.get("destino").equals(entidad.get("nombre")))
        .toList());   // MOCK
    return model;
  }
}
