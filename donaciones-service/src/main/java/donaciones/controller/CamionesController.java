package donaciones.controller;

import donaciones.domain.Donacion;
import donaciones.domain.EstadoDonacion;
import donaciones.grpc.LogisticaGrpcClient;
import donaciones.repository.DonacionRepository;
import donaciones.web.DatosMock;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    model.put("titulo", "Camiones");
    model.put("rol", "admin");
    model.put("seccion", "camiones");
    model.put("usuario", DatosMock.admin());   // MOCK

    var camiones = DatosMock.camiones();   // MOCK
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

  // Monitoreo de la flota que pide la entrega 2: el admin ve todos los camiones en ruta, sin filtro
  public Map<String, Object> seguimientoAdmin() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Seguimiento de camiones");
    model.put("rol", "admin");
    model.put("seccion", "seguimiento");
    model.put("usuario", DatosMock.admin());   // MOCK
    model.put("subtitulo", "Todos los camiones en ruta y el avance de sus entregas.");
    // List<UbicacionCamionDTO> camiones = logisticaAPICalls.camionesEnRuta().execute().body();
    model.put("camiones", DatosMock.camionesEnRuta());   // MOCK
    return model;
  }

  public Map<String, Object> seguimientoDonante(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Seguimiento de entregas");
    model.put("rol", "donante");
    model.put("seccion", "seguimiento");

    Map<String, Object> donante = DatosMock.donante();
    model.put("usuario", donante);
    model.put("subtitulo", "Camiones que hoy llevan donaciones tuyas.");
    model.put("textoMapa", "Un marcador por camión y los destinos de tus donaciones");

    // 1. Buscar donaciones en traslado del donante (donante fijo del seed hasta que esté el login)
    List<Donacion> donacionesEnTraslado = donacionRepository.obtenerTodas().stream()
        .filter(d -> d.getEstado() == EstadoDonacion.EN_TRASLADO && d.getDonante().getEmail().equals("ana@mail.com"))
        .toList();

    // 2. Extraer los IDs de las donaciones
    List<Long> idsDonaciones = donacionesEnTraslado.stream()
        .map(Donacion::getId)
        .toList();

    // 3. Consultar vía gRPC la ubicación de los camiones
    List<Map<String, Object>> camiones = List.of();
    if (!idsDonaciones.isEmpty()) {
      UbicacionCamionesResponse response = logisticaGrpcClient.obtenerUbicacionCamiones(idsDonaciones);

      // 4. Mapear la respuesta de Protobuf a la estructura que espera la vista
      camiones = response.getUbicacionesList().stream()
          .map(u -> camionParaVista(u, donacionesEnTraslado))
          .toList();
    }
    if (camiones.isEmpty()) {
      camiones = DatosMock.camionesEnRuta();   // MOCK hasta que logística tenga rutas cargadas
    }

    model.put("camiones", camiones);
    return model;
  }

  public Map<String, Object> seguimientoEntidad(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Seguimiento de entregas");
    model.put("rol", "entidad");
    model.put("seccion", "seguimiento");

    Map<String, Object> entidad = DatosMock.entidad();
    model.put("usuario", entidad);
    model.put("entidad", entidad);
    model.put("subtitulo", "Camiones en camino con donaciones para tu entidad.");
    model.put("textoMapa", "Un marcador por camión y la ubicación de tu entidad");

    // Entidad fija del seed hasta que esté el login
    List<Donacion> donacionesEnTraslado = donacionRepository.obtenerTodas().stream()
        .filter(d -> d.getEstado() == EstadoDonacion.EN_TRASLADO
            && d.getEntidadBeneficiaria().getRazonSocial().equals("Escuela Rural N°10"))
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
    if (camiones.isEmpty()) {
      camiones = DatosMock.camionesEnRuta().stream()
          .filter(c -> c.get("destino").equals(entidad.get("nombre")))
          .toList();   // MOCK hasta que logística tenga rutas cargadas
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
        "latitud", ubicacion.getLatitud(),
        "longitud", ubicacion.getLongitud(),
        "velocidad", (int) ubicacion.getVelocidad() + " km/h",
        // ultimaActualizacion viene en ISO-8601: se muestra la hora (HH:mm)
        "actualizacion", ubicacion.getUltimaActualizacion().isEmpty() ? "—" : ubicacion.getUltimaActualizacion().substring(11, 16));
  }
}
