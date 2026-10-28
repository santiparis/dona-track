package donaciones.web.controllers;

import donaciones.web.DatosMock;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
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

  public Map<String, Object> deposito(Context ctx) {
    // Los filtros viajan en la URL: ?categoria=...&vence=VENCIDA|PROXIMA
    String categoria = Objects.requireNonNullElse(ctx.queryParam("categoria"), "");
    String vence = Objects.requireNonNullElse(ctx.queryParam("vence"), "");

    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Donaciones en depósito");
    model.put("rol", "admin");
    model.put("seccion", "deposito");
    model.put("usuario", DatosMock.admin());   // MOCK

    // List<Donacion> enDeposito = donacionRepository.obtenerTodas().stream().filter(d -> d.getEstado() == EstadoDonacion.EN_DEPOSITO).toList();
    List<Map<String, String>> enDeposito = DatosMock.donacionesEnDeposito();   // MOCK
    model.put("donaciones", enDeposito.stream()
        .filter(d -> categoria.isEmpty() || categoria.equals(d.get("categoria")))
        .filter(d -> vence.isEmpty() || vence.equals(d.get("vencimiento")))
        .toList());   // MOCK
    model.put("categorias", DatosMock.categorias());   // MOCK
    model.put("categoriaElegida", categoria);
    model.put("venceElegido", vence);

    // Después de marcar una como vencida se vuelve con ?vencida={id} para mostrar el aviso
    String idVencida = ctx.queryParam("vencida");
    enDeposito.stream()
        .filter(d -> d.get("id").equals(idVencida))
        .findFirst()
        .ifPresent(d -> model.put("marcadaVencida", d));   // MOCK
    return model;
  }

  public void marcarVencida(Context ctx) {
    // Sin persistencia todavía: acá la donación ctx.pathParam("id") pasa a Vencida (cambio manual, queda en el historial)
    ctx.redirect("/admin/donaciones?vencida=" + ctx.pathParam("id"));
  }

  public Map<String, Object> asignaciones(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Asignaciones pendientes");
    model.put("rol", "admin");
    model.put("seccion", "asignaciones");
    model.put("usuario", DatosMock.admin());   // MOCK
    model.put("ultimaEjecucion", "hoy 03:00");   // MOCK
    // List<SugerenciaAsignacion> sugerencias = sugerenciaRepository.obtenerTodas();
    model.put("sugerencias", DatosMock.sugerencias());   // MOCK
    // Después de ejecutar los algoritmos se vuelve con ?ejecutado para mostrar el aviso
    model.put("ejecutado", ctx.queryParam("ejecutado") != null);
    return model;
  }

  public void ejecutarAlgoritmos(Context ctx) {
    // Sin conexión todavía: acá se corre lo mismo que POST /api/sugerencias (limpiar sugerencias y procesar las donaciones en depósito)
    ctx.redirect("/admin/asignaciones?ejecutado");
  }

  public void registrarDonacion(Context ctx) {
    // Sin persistencia todavía: acá se arma la donación con sus bienes (bienes[i].subcategoria, cantidad, ...),
    // se segmenta por subcategoría y vencimiento, y las donaciones resultantes quedan En depósito
    ctx.redirect("/admin/donaciones");
  }
}
