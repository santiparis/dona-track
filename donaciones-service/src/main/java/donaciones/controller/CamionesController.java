package donaciones.controller;

import donaciones.domain.Donacion;
import donaciones.domain.EstadoDonacion;
import donaciones.grpc.LogisticaGrpcClient;
import donaciones.repository.DonacionRepository;
import donaciones.web.DatosMock;
import donaciones.web.SesionUsuario;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import logistica.grpc.CamionResponse;
import logistica.grpc.UbicacionCamion;
import logistica.grpc.UbicacionCamionesResponse;

public class CamionesController {

  private final LogisticaGrpcClient logisticaGrpcClient;
  private final DonacionRepository donacionRepository;

  public CamionesController(LogisticaGrpcClient logisticaGrpcClient, DonacionRepository donacionRepository) {
    this.logisticaGrpcClient = logisticaGrpcClient;
    this.donacionRepository = donacionRepository;
  }

  public Map<String, Object> camiones(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Administrar camiones");
    model.put("rol", "admin");
    model.put("seccion", "camiones");

    model.put("camiones", logisticaGrpcClient.obtenerCamiones());

    // ?editar={id} carga ese camión en el formulario; si no, el formulario queda vacío para dar de alta
    String idEditar = ctx.queryParam("editar");
    if (idEditar != null) {
      model.put("camion", logisticaGrpcClient.obtenerCamion(Long.valueOf(idEditar)));
    }
    return model;
  }

  // Los camiones viven en Logística: se crean, modifican y eliminan por gRPC
  public void crearCamion(Context ctx) {
    CamionResponse response = logisticaGrpcClient.crearCamion(ctx.formParam("patente"),
        ctx.formParamAsClass("volumen", Double.class).get(),
        ctx.formParamAsClass("altura", Double.class).get(),
        ctx.formParamAsClass("cargaMax", Double.class).get());
    ctx.sessionAttribute("aviso", response.getMensaje());
    ctx.redirect("/admin/camiones");
  }

  public void modificarCamion(Context ctx) {
    CamionResponse response = logisticaGrpcClient.modificarCamion(Long.valueOf(ctx.pathParam("id")),
        ctx.formParam("patente"),
        ctx.formParamAsClass("volumen", Double.class).get(),
        ctx.formParamAsClass("altura", Double.class).get(),
        ctx.formParamAsClass("cargaMax", Double.class).get());
    ctx.sessionAttribute("aviso", response.getMensaje());
    ctx.redirect("/admin/camiones");
  }

  public void eliminarCamion(Context ctx) {
    CamionResponse response = logisticaGrpcClient.eliminarCamion(Long.valueOf(ctx.pathParam("id")));
    ctx.sessionAttribute("aviso", response.getMensaje());
    ctx.redirect("/admin/camiones");
  }

  // Monitoreo de la flota que pide la entrega 2: el admin ve todos los camiones en ruta, sin filtro
  public Map<String, Object> seguimientoAdmin() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Seguimiento de camiones");
    model.put("rol", "admin");
    model.put("seccion", "seguimiento");
    model.put("subtitulo", "Todos los camiones en ruta y el avance de sus entregas.");
    // Todas las donaciones en traslado: logística devuelve los camiones que las llevan
    List<Donacion> donacionesEnTraslado = donacionRepository.obtenerTodas().stream()
        .filter(d -> d.getEstado() == EstadoDonacion.EN_TRASLADO)
        .toList();

    List<Long> idsDonaciones = donacionesEnTraslado.stream()
        .map(Donacion::getId)
        .toList();

    List<Map<String, Object>> camiones = List.of();
    if (!idsDonaciones.isEmpty()) {
      UbicacionCamionesResponse response = logisticaGrpcClient.obtenerUbicacionCamiones(idsDonaciones);

      camiones = response.getUbicacionesList().stream()
          .map(u -> camionParaVista(u, donacionesEnTraslado))
          .toList();
    }

    model.put("camiones", camiones);
    return model;
  }

  public Map<String, Object> seguimientoDonante(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Seguimiento de entregas");
    model.put("rol", "donante");
    model.put("seccion", "seguimiento");

    model.put("subtitulo", "Camiones que hoy llevan donaciones tuyas.");
    model.put("textoMapa", "Un marcador por camión y los destinos de tus donaciones");

    // 1. Buscar donaciones en traslado del donante
    List<Donacion> donacionesEnTraslado = donacionRepository.buscarPorUsuarioDonante(ctx.sessionAttribute(SesionUsuario.USUARIO_ID));

    // 2. Extraer los IDs de las donaciones
    List<Long> idsDonaciones = donacionesEnTraslado.stream()
        .filter(d -> d.getEstado() == EstadoDonacion.EN_TRASLADO)
        .map(Donacion::getId)
        .toList();

    // 3. Consultar vía gRPC la ubicación de los camiones
    List<Map<String, Object>> camiones = List.of();
    if (!idsDonaciones.isEmpty()) {
      UbicacionCamionesResponse response = logisticaGrpcClient.obtenerUbicacionCamiones(idsDonaciones);

      // 4. Mapear la respuesta de Protobuf a la estructura que espera la vista
      camiones = response.getUbicacionesList().stream()
          .filter(u -> idsDonaciones.contains(u.getIdDonacion()))
          .map(u -> camionParaVista(u, donacionesEnTraslado))
          .toList();
    }

    model.put("camiones", camiones);
    return model;
  }

  public Map<String, Object> seguimientoEntidad(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Seguimiento de entregas");
    model.put("rol", "entidad");
    model.put("seccion", "seguimiento");

    model.put("subtitulo", "Camiones en camino con donaciones para tu entidad.");
    model.put("textoMapa", "Un marcador por camión y la ubicación de tu entidad");
    model.put("entidad", DatosMock.entidad());   // MOCK: EntidadBeneficiaria todavía no tiene coordenadas

    List<Donacion> donacionesEnTraslado = donacionRepository.buscarPorUsuarioEntidad(ctx.sessionAttribute(SesionUsuario.USUARIO_ID));

    List<Long> idsDonaciones = donacionesEnTraslado.stream()
        .filter(d -> d.getEstado() == EstadoDonacion.EN_TRASLADO)
        .map(Donacion::getId)
        .toList();

    List<Map<String, Object>> camiones = List.of();
    if (!idsDonaciones.isEmpty()) {
      UbicacionCamionesResponse response = logisticaGrpcClient.obtenerUbicacionCamiones(idsDonaciones);

      camiones = response.getUbicacionesList().stream()
          .filter(u -> idsDonaciones.contains(u.getIdDonacion()))
          .map(u -> camionParaVista(u, donacionesEnTraslado))
          .toList();
    }

    model.put("camiones", camiones);
    return model;
  }

  // El camión como lo muestra la tabla del seguimiento, con los datos de la donación que lleva
  private static Map<String, Object> camionParaVista(UbicacionCamion ubicacion, List<Donacion> donaciones) {
    Donacion donacion = donaciones.stream()
        .filter(d -> d.getId() == ubicacion.getIdDonacion())
        .findFirst()
        .orElseThrow();
    return Map.of(
        "patente", ubicacion.getPatente(),
        "donacion", donacion.getBien().getSubcategoria().nombre() + " · " + donacion.getBien().getCantidad() + " " + donacion.getBien().getUnidad(),
        "destino", donacion.getEntidadBeneficiaria().getRazonSocial(),
        "avance", "—",   // logística no informa el avance de la ruta
        // A 4 decimales (unos 10 metros): el GPS manda valores como -34.308000000000675
        "latitud", Math.round(ubicacion.getLatitud() * 10000) / 10000.0,
        "longitud", Math.round(ubicacion.getLongitud() * 10000) / 10000.0,
        "velocidad", (int) ubicacion.getVelocidad() + " km/h",
        // ultimaActualizacion viene en ISO-8601: se muestra la hora (HH:mm)
        "actualizacion", ubicacion.getUltimaActualizacion().isEmpty() ? "—" : ubicacion.getUltimaActualizacion().substring(11, 16));
  }
}
