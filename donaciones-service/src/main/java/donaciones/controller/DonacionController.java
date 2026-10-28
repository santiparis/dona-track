package donaciones.controller;

import donaciones.domain.Bien;
import donaciones.domain.Categoria;
import donaciones.domain.Donacion;
import donaciones.domain.EstadoBien;
import donaciones.domain.EstadoDonacion;
import donaciones.domain.RegistroCambioEstado;
import donaciones.domain.Subcategoria;
import donaciones.domain.notificacion.Contacto;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.PersonaJuridica;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.dto.BienDTO;
import donaciones.dto.BienResponseDTO;
import donaciones.dto.ContactoResponseDTO;
import donaciones.dto.DonacionPatchDTO;
import donaciones.dto.DonacionRequestDTO;
import donaciones.dto.DonacionResponseDTO;
import donaciones.dto.DonanteResponseDTO;
import donaciones.dto.RegistroCambioEstadoDTO;
import donaciones.repository.DonacionRepository;
import donaciones.repository.PersonasAdministradorasRepository;
import donaciones.web.DatosMock;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public class DonacionController {
  private static final Logger logger = LoggerFactory.getLogger(DonacionController.class);

  private final DonacionRepository donacionesRepository;
  private final RepositorioPersonas personasRepository;
  private final PersonasAdministradorasRepository administradorasRepository;
  private final Notificador notificador;

  public DonacionController(
      DonacionRepository donacionesRepository,
      RepositorioPersonas personasRepository,
      PersonasAdministradorasRepository administradorasRepository,
      Notificador notificador
  ) {
    this.donacionesRepository = donacionesRepository;
    this.personasRepository = personasRepository;
    this.administradorasRepository = administradorasRepository;
    this.notificador = notificador;
  }

  public void listar(Context ctx) {
    try {
      List<DonacionResponseDTO> respuesta = this.listarDonaciones()
          .stream()
          .map(DonacionController::toResponseDTO)
          .toList();
      ctx.json(respuesta);
    } catch (RuntimeException e) {
      logger.error("Error al listar donaciones", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al listar donaciones: " + e.getMessage());
    }
  }

  public void obtener(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      Donacion donacion = donacionesRepository.buscarPorId(id)
          .orElseThrow(() -> new IllegalArgumentException("No se encontró la donación"));
      ctx.json(toResponseDTO(donacion));
    } catch (IllegalArgumentException e) {
      logger.warn("Error al obtener donacion: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al obtener donacion", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al obtener donacion: " + e.getMessage());
    }
  }

  public void crear(Context ctx) {
    try {
      DonacionRequestDTO dto = ctx.bodyAsClass(DonacionRequestDTO.class);
      this.crearDonacion(dto);
      ctx.status(HttpStatus.CREATED).result("Donación recibida y guardada");
    } catch (IllegalArgumentException e) {
      logger.warn("Error de validacion al crear donacion: {}", e.getMessage());
      ctx.status(HttpStatus.BAD_REQUEST).result("Error: " + e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al crear donacion", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al crear donacion: " + e.getMessage());
    }
  }

  public void actualizar(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      DonacionRequestDTO dto = ctx.bodyAsClass(DonacionRequestDTO.class);
      this.actualizarDonacion(id, dto);
      ctx.result("Donación actualizada");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al actualizar donacion: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al actualizar donacion", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al actualizar donacion: " + e.getMessage());
    }
  }

  public void actualizarParcial(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      DonacionPatchDTO dto = ctx.bodyAsClass(DonacionPatchDTO.class);

      if (dto.estado() != null) {
        Donacion donacion = donacionesRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró la donación"));
        this.cambiarEstado(donacion, dto);
        ctx.result("Estado de la donación actualizado");
      } else {
        this.actualizarDonacionParcial(id, dto);
        ctx.result("Donación actualizada parcialmente");
      }
    } catch (IllegalArgumentException e) {
      logger.warn("Error al actualizar parcialmente donacion: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al actualizar parcialmente donacion", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al actualizar donacion: " + e.getMessage());
    }
  }

  // Logistica informa el nuevo estado; datosAdicionales depende del estado (url del mapa, patente o motivo)
  private void cambiarEstado(Donacion donacion, DonacionPatchDTO dto) {
    switch (EstadoDonacion.valueOf(dto.estado())) {
      case EN_TRASLADO -> this.marcarEnTraslado(donacion, dto.datosAdicionales());
      case ENTREGADA -> this.confirmarEntrega(donacion, dto.datosAdicionales());
      case ENTREGA_FALLIDA -> this.registrarEntregaFallida(donacion, dto.datosAdicionales());
      default -> throw new IllegalArgumentException("Estado no soportado: " + dto.estado());
    }
  }

  private void marcarEnTraslado(Donacion donacion, String urlMapa) {
    donacion.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    notificador.inicioDeTraslado(donacion, urlMapa);
  }

  private void confirmarEntrega(Donacion donacion, String patenteCamion) {
    donacion.cambiarEstado(EstadoDonacion.ENTREGADA, null);
    notificador.entregaConfirmada(donacion, patenteCamion);
  }

  private void registrarEntregaFallida(Donacion donacion, String motivo) {
    donacion.cambiarEstado(EstadoDonacion.ENTREGA_FALLIDA, motivo);
    notificador.entregaFallida(donacion, motivo, administradorasRepository.obtenerTodos());
  }

  public void eliminar(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      this.eliminarDonacion(id);
      ctx.result("Donacion eliminada");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al eliminar donacion: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al eliminar donacion", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al eliminar donacion: " + e.getMessage());
    }
  }

  private static DonacionResponseDTO toResponseDTO(Donacion donacion) {
    return new DonacionResponseDTO(
        donacion.getId(),
        toDonanteResponseDTO(donacion.getDonante()),
        toBienResponseDTO(donacion.getBien()),
        donacion.getEstado() == null ? null : donacion.getEstado().name(),
        donacion.getFecha() == null ? null : donacion.getFecha().toString(),
        donacion.getHistorialEstados().stream()
            .map(DonacionController::toRegistroDTO)
            .toList()
    );
  }

  private static DonanteResponseDTO toDonanteResponseDTO(Persona persona) {
    if (persona instanceof PersonaHumana humana) {
      return new DonanteResponseDTO(
          "HUMANA",
          humana.getDocumento(),
          humana.getNombre(),
          humana.getApellido(),
          humana.getEdad(),
          humana.getDireccion(),
          null,
          null,
          toContactosDTO(humana.getContactos())
      );
    }

    if (persona instanceof PersonaJuridica juridica) {
      return new DonanteResponseDTO(
          "JURIDICA",
          juridica.getDocumento(),
          juridica.getNombre(),
          null,
          null,
          null,
          juridica.getRazonSocial() == null ? null : juridica.getRazonSocial().name(),
          juridica.getRubro(),
          toContactosDTO(juridica.getContactos())
      );
    }

    return new DonanteResponseDTO(
        "DESCONOCIDA",
        persona.getDocumento(),
        persona.getNombre(),
        null,
        null,
        null,
        null,
        null,
        toContactosDTO(persona.getContactos())
    );
  }

  private static BienResponseDTO toBienResponseDTO(Bien bien) {
    if (bien == null) {
      return null;
    }

    return new BienResponseDTO(
        bien.getSubcategoria() == null ? null : bien.getSubcategoria().name(),
        bien.getCantidad(),
        bien.getUnidad(),
        bien.getEstado() == null ? null : bien.getEstado().name(),
        bien.getVencimiento() == null ? null : bien.getVencimiento().toString(),
        bien.getDescripcion(),
        bien.getFoto()
    );
  }

  private static RegistroCambioEstadoDTO toRegistroDTO(RegistroCambioEstado<?> registro) {
    return new RegistroCambioEstadoDTO(
        registro.estadoAnterior() == null ? null : registro.estadoAnterior().toString(),
        registro.estadoNuevo() == null ? null : registro.estadoNuevo().toString(),
        registro.fecha() == null ? null : registro.fecha().toInstant().toString(),
        registro.justificacion()
    );
  }

  private static List<ContactoResponseDTO> toContactosDTO(List<Contacto> contactos) {
    return contactos.stream()
        .map(contacto -> new ContactoResponseDTO(contacto.getEstrategia(), contacto.getValor()))
        .toList();
  }

  // ---------------------------------------------------------------
  // Operaciones sobre donaciones (venian de DonacionService)
  // ---------------------------------------------------------------

  public void crearDonacion(DonacionRequestDTO dto) {
    List<Bien> bienesDelDominio = this.crearListaBienes(dto.bienes());

    for (Bien bien : bienesDelDominio) {
      Optional<Persona> persona = this.personasRepository.buscarPorId(dto.idDonante());

      if (persona.isPresent()) {
        this.donacionesRepository.guardar(new Donacion(persona.get(), bien));
      } else {
        throw new IllegalArgumentException("No se encontró el donante con id: " + dto.idDonante());
      }
    }
  }

  public List<Bien> crearListaBienes(List<BienDTO> bienesDto) {
    return bienesDto.stream()
        .map(bienDto -> {
          Subcategoria subcat = this.parsearSubcategoria(bienDto.nombreSubcategoria());
          EstadoBien estado;

          if (bienDto.estado() != null) {
            estado = this.parsearEstado(bienDto.estado());
          } else {
            estado = null;
          }

          LocalDate vencimiento;
          if (bienDto.vencimiento() != null) {
            vencimiento = LocalDate.parse(bienDto.vencimiento());
          } else {
            vencimiento = null;
          }

          return new Bien(
              subcat,
              bienDto.cantidad(),
              bienDto.unidad(),
              bienDto.descripcion(),
              bienDto.foto(),
              estado,
              vencimiento
          );
        })
        .toList();
  }

  public Subcategoria parsearSubcategoria(String subcategoria) {
    String valor = subcategoria == null ? "" : subcategoria.trim().toUpperCase();

    if (valor.isEmpty()) {
      throw new IllegalArgumentException("Categoría inválida: " + subcategoria);
    }

    try {
      return Subcategoria.valueOf(valor);
    } catch (IllegalArgumentException e) {
      try {
        Categoria categoria = Categoria.valueOf(valor);
        return Arrays.stream(Subcategoria.values())
            .filter(sub -> sub.getCategoria() == categoria)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Categoría inválida: " + subcategoria));
      } catch (IllegalArgumentException ignored) {
        throw new IllegalArgumentException("Categoría inválida: " + subcategoria, e);
      }
    }
  }

  public EstadoBien parsearEstado(String nombreEstado) {
    try {
      return EstadoBien.valueOf(nombreEstado.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Estado de bien inválido: " + nombreEstado, e);
    }
  }

  public List<Donacion> listarDonaciones() {
    return donacionesRepository.obtenerTodas();
  }

  public void actualizarDonacion(Long id, DonacionRequestDTO dto) {
    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorId(id);
    if (donacionOpt.isEmpty()) {
      throw new IllegalArgumentException("No se encontró la donación");
    }

    Donacion donacionExistente = donacionOpt.get();
    Bien bienActualizado = null;

    if (dto.bienes() != null && !dto.bienes().isEmpty()) {
      List<Bien> bienes = this.crearListaBienes(dto.bienes());
      bienActualizado = bienes.get(0);
    }

    if (dto.idDonante() != null) {
      Optional<Persona> persona = this.personasRepository.buscarPorId(dto.idDonante());
      if (persona.isPresent()) {
        donacionExistente.actualizarDatos(persona.get(), bienActualizado);
      } else {
        throw new IllegalArgumentException("No se encontró el donante con id: " + dto.idDonante());
      }
    } else {
      donacionExistente.actualizarDatos(null, bienActualizado);
    }
  }

  public void actualizarDonacionParcial(Long id, DonacionPatchDTO dto) {
    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorId(id);
    if (donacionOpt.isEmpty()) {
      throw new IllegalArgumentException("No se encontró la donación");
    }

    Donacion donacionExistente = donacionOpt.get();
    Persona personaActualizada = null;
    Bien bienActualizado = null;

    if (dto.idDonante() != null) {
      Optional<Persona> persona = this.personasRepository.buscarPorId(dto.idDonante());
      if (persona.isPresent()) {
        personaActualizada = persona.get();
      } else {
        throw new IllegalArgumentException("No se encontró el donante con id: " + dto.idDonante());
      }
    }

    if (dto.bienes() != null && !dto.bienes().isEmpty()) {
      List<Bien> bienes = this.crearListaBienes(dto.bienes());
      bienActualizado = bienes.get(0);
    }

    donacionExistente.actualizarDatos(personaActualizada, bienActualizado);
  }

  public void eliminarDonacion(Long id) {
    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorId(id);
    if (donacionOpt.isPresent()) {
      donacionesRepository.borrarPorId(id);
    } else {
      throw new IllegalArgumentException("No se encontró la donación");
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

  public Map<String, Object> deposito(Context ctx) {
    // Los filtros viajan en la URL: ?categoria=...&vence=VENCIDA|PROXIMA
    String categoria = Objects.requireNonNullElse(ctx.queryParam("categoria"), "");
    String vence = Objects.requireNonNullElse(ctx.queryParam("vence"), "");

    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Donaciones en depósito");
    model.put("rol", "admin");
    model.put("seccion", "deposito");
    model.put("usuario", DatosMock.admin());   // MOCK

    // List<Donacion> enDeposito = donacionesRepository.obtenerTodas().stream().filter(d -> d.getEstado() == EstadoDonacion.EN_DEPOSITO).toList();
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

  public Map<String, Object> misDonaciones(Context ctx) {
    // Los filtros viajan en la URL: ?estado=...&categoria=...&subcategoria=...&subcategoria=...
    String estado = Objects.requireNonNullElse(ctx.queryParam("estado"), "");
    String categoria = Objects.requireNonNullElse(ctx.queryParam("categoria"), "");
    List<String> subcategorias = ctx.queryParams("subcategoria");

    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Mis donaciones");
    model.put("rol", "donante");
    model.put("seccion", "donaciones");
    model.put("usuario", DatosMock.donante());   // MOCK

    // List<Donacion> donaciones = donacionesRepository.buscarPorDonante(idDonante, estado, categoria, subcategorias);
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

  public Map<String, Object> asignadasAEntidad(Context ctx) {
    // El filtro viaja en la URL: ?estado=...
    String estado = Objects.requireNonNullElse(ctx.queryParam("estado"), "");

    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Donaciones asignadas");
    model.put("rol", "entidad");
    model.put("seccion", "donaciones");
    model.put("usuario", DatosMock.entidad());   // MOCK

    // List<Donacion> donaciones = donacionesRepository.obtenerTodas().stream().filter(d -> d.getEntidadBeneficiaria() == entidad).toList();
    model.put("donaciones", DatosMock.donacionesAsignadas().stream()
        .filter(d -> estado.isEmpty() || estado.equals(d.get("estado")))
        .toList());   // MOCK
    model.put("estadoElegido", estado);
    // A la entidad solo le llegan donaciones ya asignadas: no tiene sentido filtrar por En depósito ni Vencida
    model.put("estados", DatosMock.estados().stream()
        .filter(e -> !List.of("EN_DEPOSITO", "VENCIDA").contains(e.get("valor")))
        .toList());   // MOCK
    return model;
  }

  public Map<String, Object> recepcion(Context ctx) {
    String id = ctx.pathParam("id");

    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Confirmar recepción");
    model.put("rol", "entidad");
    model.put("seccion", "donaciones");
    model.put("usuario", DatosMock.entidad());   // MOCK

    // Donacion donacion = donacionesRepository.buscarPorId(Long.valueOf(id)).orElseThrow(NotFoundResponse::new);
    model.put("donacion", DatosMock.donacionesAsignadas().stream()
        .filter(d -> d.get("id").equals(id))
        .findFirst()
        .orElseThrow(NotFoundResponse::new));   // MOCK
    return model;
  }

  public void confirmarRecepcion(Context ctx) {
    // Sin persistencia todavía: acá la donación pasa a Entregada y se guardan las fotos de ctx.uploadedFiles("fotos")
    ctx.redirect("/entidad/donaciones");
  }

  public void informarNoRecibida(Context ctx) {
    // Sin persistencia todavía: acá la entrega se marca No recibida con el motivo, para que la revise la administración
    ctx.redirect("/entidad/donaciones");
  }

  public Map<String, Object> entregadas() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Donaciones entregadas");
    model.put("seccion", "entregadas");
    // List<Donacion> entregadas = donacionesRepository.obtenerTodas().stream().filter(d -> d.getEstado() == EstadoDonacion.ENTREGADA).toList();
    model.put("entregas", DatosMock.donacionesEntregadas());   // MOCK
    return model;
  }
}
