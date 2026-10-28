package donaciones.web.controllers;

import donaciones.web.DatosMock;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class AdminController {

  public Map<String, Object> donanteNuevo(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Registrar donante");
    model.put("rol", "admin");
    model.put("seccion", "donante-nuevo");
    model.put("usuario", DatosMock.admin());   // MOCK
    // La pestaña viaja en la URL: /admin/donantes/nuevo?tipo=HUMANA|JURIDICA
    model.put("tipo", Objects.requireNonNullElse(ctx.queryParam("tipo"), "HUMANA"));
    return model;
  }

  public void registrarDonante(Context ctx) {
    // Sin persistencia todavía: acá se da de alta la persona y se le manda el mail de bienvenida
    // "Registrar y cargar donación" manda siguiente=donacion para seguir directo con la donación
    if ("donacion".equals(ctx.formParam("siguiente"))) {
      ctx.redirect("/admin/donaciones/nueva");
    } else {
      ctx.redirect("/admin/donantes/nuevo");
    }
  }

  public Map<String, Object> donacionNueva(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Registrar donación");
    model.put("rol", "admin");
    model.put("seccion", "donacion-nueva");
    model.put("usuario", DatosMock.admin());   // MOCK

    // Paso 1: el donante se busca con un form GET aparte (?donante=documento o correo)
    String busqueda = Objects.requireNonNullElse(ctx.queryParam("donante"), "");
    model.put("busqueda", busqueda);
    // Persona donante = personasRepository.buscarPorDocumentoOEmail(busqueda);
    DatosMock.donantes().stream()
        .filter(d -> d.get("documento").equals(busqueda) || d.get("email").equals(busqueda))
        .findFirst()
        .ifPresent(d -> model.put("donante", d));   // MOCK

    // Paso 3: una fila por bien; "+ Agregar bien" recarga con ?filas=N+1 (sin JavaScript)
    int cantidadFilas = ctx.queryParamAsClass("filas", Integer.class).getOrDefault(3);
    model.put("filas", IntStream.rangeClosed(1, cantidadFilas)
        .mapToObj(numero -> Map.of("indice", numero - 1, "numero", numero))
        .toList());
    model.put("masFilas", cantidadFilas + 1);
    model.put("categorias", DatosMock.categoriasConSubcategorias());   // MOCK
    return model;
  }

  public void registrarDonacion(Context ctx) {
    // Sin persistencia todavía: acá se arma la donación con sus bienes (bienes[i].subcategoria, cantidad, ...),
    // se segmenta por subcategoría y vencimiento, y las donaciones resultantes quedan En depósito
    ctx.redirect("/admin/donaciones");
  }
}
