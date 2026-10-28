package donaciones.web.controllers;

import donaciones.web.DatosMock;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DonanteController {

  public Map<String, Object> donaciones(Context ctx) {
    // Los filtros viajan en la URL: ?estado=...&categoria=...&subcategoria=...&subcategoria=...
    String estado = Objects.requireNonNullElse(ctx.queryParam("estado"), "");
    String categoria = Objects.requireNonNullElse(ctx.queryParam("categoria"), "");
    List<String> subcategorias = ctx.queryParams("subcategoria");

    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Mis donaciones");
    model.put("rol", "donante");
    model.put("seccion", "donaciones");
    model.put("usuario", DatosMock.donante());   // MOCK

    // List<Donacion> donaciones = repositorioDonaciones.buscarPorDonante(idDonante, estado, categoria, subcategorias);
    List<Map<String, String>> donaciones = DatosMock.donacionesDelDonante().stream()
        .filter(d -> estado.isEmpty() || estado.equals(d.get("estado")))
        .filter(d -> categoria.isEmpty() || categoria.equals(d.get("categoria")))
        .filter(d -> subcategorias.isEmpty() || subcategorias.contains(d.get("subcategoria")))
        .toList();   // MOCK
    model.put("donaciones", donaciones);
    model.put("total", donaciones.size());

    model.put("estados", DatosMock.estados());   // MOCK
    model.put("categorias", DatosMock.categorias());   // MOCK
    model.put("estadoElegido", estado);
    model.put("categoriaElegida", categoria);
    // Las subcategorías se muestran recién cuando se eligió una categoría
    model.put("subcategorias", DatosMock.subcategorias(categoria).stream()
        .map(s -> Map.of("valor", s.get("valor"), "texto", s.get("texto"), "elegida", subcategorias.contains(s.get("valor"))))
        .toList());   // MOCK

    // ?historial={id} muestra los cambios de estado de esa donación
    String idHistorial = ctx.queryParam("historial");
    DatosMock.donacionesDelDonante().stream()
        .filter(d -> d.get("id").equals(idHistorial))
        .findFirst()
        .ifPresent(d -> model.put("historial", Map.of(
            "bien", d.get("bien"),
            "cantidad", d.get("cantidad"),
            // donacion.getHistorialEstados()
            "cambios", DatosMock.historialDeEstados())));   // MOCK

    return model;
  }

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

  public Map<String, Object> incentivos() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Incentivos");
    model.put("rol", "donante");
    model.put("seccion", "incentivos");
    model.put("usuario", DatosMock.donante());   // MOCK
    // Sin dominio de incentivos todavía: en esta entrega es solo la vista
    model.put("incentivos", DatosMock.incentivos());   // MOCK
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
    // Las ubicaciones vienen de logistica-service por HTTP (endpoint a definir en LogisticaAPICalls):
    // List<UbicacionCamionDTO> camiones = logisticaAPICalls.camionesConDonacionesDe(idDonante).execute().body();
    model.put("camiones", DatosMock.camionesEnRuta());   // MOCK
    return model;
  }
}
