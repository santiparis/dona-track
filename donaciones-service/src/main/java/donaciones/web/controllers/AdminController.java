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

  public Map<String, Object> importacion(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Importar donantes");
    model.put("rol", "admin");
    model.put("seccion", "importacion");
    model.put("usuario", DatosMock.admin());   // MOCK
    // Después de importar se vuelve con ?importado para mostrar el resultado
    if (ctx.queryParam("importado") != null) {
      model.put("resultado", DatosMock.resultadoImportacion());   // MOCK
    }
    return model;
  }

  public void importar(Context ctx) {
    // Sin conexión todavía: acá se guarda ctx.uploadedFile("archivo") en un archivo temporal y se llama al importador ya hecho
    // new ImportadorPersonas(repositorioPersonas).importarPersonasDesdeCSV(pathTemporal);
    ctx.redirect("/admin/importacion?importado");
  }

  public void registrarDonacion(Context ctx) {
    // Sin persistencia todavía: acá se arma la donación con sus bienes (bienes[i].subcategoria, cantidad, ...),
    // se segmenta por subcategoría y vencimiento, y las donaciones resultantes quedan En depósito
    ctx.redirect("/admin/donaciones");
  }
}
