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
    model.put("donante", DatosMock.donante());   // MOCK

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
}
