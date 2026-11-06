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
import donaciones.grpc.LogisticaGrpcClient;
import donaciones.repository.DonacionRepository;
import donaciones.repository.PersonasAdministradorasRepository;
import donaciones.web.DatosMock;
import donaciones.web.SesionUsuario;
import donaciones.db.TransaccionPorRequest;
import donaciones.storage.FotosRecepcion;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import logistica.grpc.ReportarEntregaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
  private final LogisticaGrpcClient logisticaGrpcClient;
  private final FotosRecepcion fotosRecepcion;

  public DonacionController(
      DonacionRepository donacionesRepository,
      RepositorioPersonas personasRepository,
      PersonasAdministradorasRepository administradorasRepository,
      Notificador notificador,
      LogisticaGrpcClient logisticaGrpcClient
  ) {
    this(donacionesRepository, personasRepository, administradorasRepository, notificador,
        logisticaGrpcClient, new FotosRecepcion(null, null));
  }

  public DonacionController(DonacionRepository donacionesRepository, RepositorioPersonas personasRepository,
      PersonasAdministradorasRepository administradorasRepository, Notificador notificador,
      LogisticaGrpcClient logisticaGrpcClient, FotosRecepcion fotosRecepcion) {
    this.donacionesRepository = donacionesRepository;
    this.personasRepository = personasRepository;
    this.administradorasRepository = administradorasRepository;
    this.notificador = notificador;
    this.logisticaGrpcClient = logisticaGrpcClient;
    this.fotosRecepcion = fotosRecepcion;
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
        Donacion donacion = donacionesRepository.buscarPorIdParaActualizar(id)
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
    Persona persona = this.personasRepository.buscarPorIdParaReferencia(dto.idDonante())
        .orElseThrow(() -> new IllegalArgumentException("No se encontró el donante con id: " + dto.idDonante()));

    for (Bien bien : bienesDelDominio) {
      this.donacionesRepository.guardar(new Donacion(persona, bien));
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
    Bien bienActualizado = null;
    if (dto.bienes() != null && !dto.bienes().isEmpty()) {
      bienActualizado = this.crearListaBienes(dto.bienes()).get(0);
    }

    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorIdParaActualizar(id);
    if (donacionOpt.isEmpty()) {
      throw new IllegalArgumentException("No se encontró la donación");
    }

    Donacion donacionExistente = donacionOpt.get();

    if (dto.idDonante() != null) {
      Optional<Persona> persona = this.personasRepository.buscarPorIdParaReferencia(dto.idDonante());
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
    Bien bienActualizado = null;
    if (dto.bienes() != null && !dto.bienes().isEmpty()) {
      bienActualizado = this.crearListaBienes(dto.bienes()).get(0);
    }

    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorIdParaActualizar(id);
    if (donacionOpt.isEmpty()) {
      throw new IllegalArgumentException("No se encontró la donación");
    }

    Donacion donacionExistente = donacionOpt.get();
    Persona personaActualizada = null;

    if (dto.idDonante() != null) {
      Optional<Persona> persona = this.personasRepository.buscarPorIdParaReferencia(dto.idDonante());
      if (persona.isPresent()) {
        personaActualizada = persona.get();
      } else {
        throw new IllegalArgumentException("No se encontró el donante con id: " + dto.idDonante());
      }
    }

    donacionExistente.actualizarDatos(personaActualizada, bienActualizado);
  }

  public void eliminarDonacion(Long id) {
    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorIdParaActualizar(id);
    if (donacionOpt.isPresent()) {
      Donacion donacion = donacionOpt.get();
      var idsFotos = donacion.getFotosRecepcion().keySet();
      donacionesRepository.eliminar(donacion);
      if (!idsFotos.isEmpty()) {
        TransaccionPorRequest.despuesDelCommit(() -> idsFotos.forEach(this::retirarFoto));
      }
    } else {
      throw new IllegalArgumentException("No se encontró la donación");
    }
  }

  public Map<String, Object> donacionNueva(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Registrar donación");
    model.put("rol", "admin");
    model.put("seccion", "donacion-nueva");

    // Paso 1: el donante se busca con un form GET aparte (?donante=documento o correo)
    String busqueda = Objects.requireNonNullElse(ctx.queryParam("donante"), "");
    model.put("busqueda", busqueda);
    if (!busqueda.isEmpty()) {
      personasRepository.buscarPorDocumento(busqueda)
          .or(() -> personasRepository.buscarPorEmail(busqueda))
          .ifPresent(d -> model.put("donante", Map.of(
              "id", d.getId().toString(),
              "nombre", d.getNombre(),
              "documento", d.getDocumento(),
              "email", d.getEmail())));
    }

    // Paso 3: una fila por bien; "+ Agregar bien" recarga con ?filas=N+1 (sin JavaScript)
    int cantidadFilas = ctx.queryParamAsClass("filas", Integer.class).getOrDefault(3);
    model.put("filas", IntStream.rangeClosed(1, cantidadFilas)
        .mapToObj(numero -> Map.of("indice", numero - 1, "numero", numero))
        .toList());
    model.put("masFilas", cantidadFilas + 1);
    model.put("categorias", Arrays.stream(Categoria.values())
        .map(c -> Map.of(
            "texto", c.nombre(),
            "subcategorias", Arrays.stream(Subcategoria.values())
                .filter(s -> s.getCategoria() == c)
                .map(s -> Map.of("valor", s.name(), "texto", s.nombre()))
                .toList()))
        .toList());
    return model;
  }

  public void registrarDonacion(Context ctx) {
    // Una fila del form por bien; las filas sin subcategoría quedaron vacías
    List<BienDTO> bienes = new ArrayList<>();
    for (int i = 0; ctx.formParam("bienes[" + i + "].subcategoria") != null; i++) {
      String fila = "bienes[" + i + "].";
      if (ctx.formParam(fila + "subcategoria").isBlank()) {
        continue;
      }
      bienes.add(new BienDTO(false, false,
          ctx.formParam(fila + "subcategoria"),
          Integer.valueOf(ctx.formParam(fila + "cantidad")),
          ctx.formParam(fila + "unidad"),
          ctx.formParam(fila + "descripcion"),
          vacioANull(ctx.formParam(fila + "estado")),
          vacioANull(ctx.formParam(fila + "vencimiento")),
          null));
    }
    crearDonacion(new DonacionRequestDTO(Long.valueOf(ctx.formParam("donante")), ctx.formParam("descripcion"), bienes));
    ctx.redirect("/admin/donaciones");
  }

  private static String vacioANull(String valor) {
    return valor == null || valor.isBlank() ? null : valor;
  }

  public Map<String, Object> deposito(Context ctx) {
    // Los filtros viajan en la URL: ?categoria=...&vence=VENCIDA
    String categoria = Objects.requireNonNullElse(ctx.queryParam("categoria"), "");
    String vence = Objects.requireNonNullElse(ctx.queryParam("vence"), "");

    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Donaciones en depósito");
    model.put("rol", "admin");
    model.put("seccion", "deposito");

    DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    model.put("donaciones", donacionesRepository.obtenerTodas().stream()
        // Las vencidas siguen en el depósito, marcadas: las marca a mano la administradora
        .filter(d -> d.getEstado() == EstadoDonacion.EN_DEPOSITO || d.getEstado() == EstadoDonacion.VENCIDA)
        .filter(d -> categoria.isEmpty() || categoria.equals(d.getBien().getSubcategoria().getCategoria().name()))
        .filter(d -> vence.isEmpty() || d.getEstado() == EstadoDonacion.VENCIDA)
        .map(d -> Map.of(
            "id", d.getId().toString(),
            "bien", d.getBien().getSubcategoria().nombre(),
            "cantidad", d.getBien().getCantidad() + " " + d.getBien().getUnidad(),
            "donante", d.getDonante().getNombre(),
            "ingreso", d.getFecha().format(formatoFecha),
            "vence", d.getBien().getVencimiento() == null ? "—" : d.getBien().getVencimiento().format(formatoFecha),
            // VENCIDA: ya marcada; NO_APLICA: no es perecedero; vacío: se puede marcar
            "vencimiento", d.getEstado() == EstadoDonacion.VENCIDA ? "VENCIDA"
                : d.getBien().getVencimiento() == null ? "NO_APLICA" : ""))
        .toList());
    model.put("categorias", Arrays.stream(Categoria.values())
        .map(c -> Map.of("valor", c.name(), "texto", c.nombre()))
        .toList());
    model.put("categoriaElegida", categoria);
    model.put("venceElegido", vence);

    // Después de marcar una como vencida se vuelve con ?vencida={id} para mostrar el aviso
    String idVencida = ctx.queryParam("vencida");
    if (idVencida != null) {
      donacionesRepository.buscarPorId(Long.parseLong(idVencida))
          .ifPresent(d -> model.put("marcadaVencida", Map.of(
              "id", idVencida,
              "bien", d.getBien().getSubcategoria().nombre(),
              "cantidad", d.getBien().getCantidad() + " " + d.getBien().getUnidad())));
    }
    return model;
  }

  public void marcarVencida(Context ctx) {
    Donacion donacion = donacionesRepository.buscarPorId(Long.parseLong(ctx.pathParam("id")))
        .orElseThrow(NotFoundResponse::new);
    donacion.cambiarEstado(EstadoDonacion.VENCIDA, null);
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

    List<Donacion> propias = donacionesRepository.buscarPorUsuarioDonante(ctx.sessionAttribute(SesionUsuario.USUARIO_ID));
    List<Map<String, String>> donaciones = propias.stream()
        .map(this::donacionParaVista)
        .filter(d -> estado.isEmpty() || estado.equals(d.get("estado")))
        .filter(d -> categoria.isEmpty() || categoria.equals(d.get("categoria")))
        .filter(d -> subcategorias.isEmpty() || subcategorias.contains(d.get("subcategoria")))
        .toList();
    model.put("donaciones", donaciones);
    model.put("total", donaciones.size());

    model.put("estados", Arrays.stream(EstadoDonacion.values())
        .map(e -> Map.of("valor", e.name(), "texto", e.nombre()))
        .toList());
    model.put("categorias", Arrays.stream(Categoria.values())
        .map(c -> Map.of("valor", c.name(), "texto", c.nombre()))
        .toList());
    model.put("estadoElegido", estado);
    model.put("categoriaElegida", categoria);
    // Las subcategorías se muestran recién cuando se eligió una categoría
    model.put("subcategorias", Arrays.stream(Subcategoria.values())
        .filter(s -> s.getCategoria().name().equals(categoria))
        .map(s -> Map.of("valor", s.name(), "texto", s.nombre(), "elegida", subcategorias.contains(s.name())))
        .toList());

    // ?historial={id} muestra los cambios de estado de esa donación
    String idHistorial = ctx.queryParam("historial");
    propias.stream()
        .filter(d -> d.getId().toString().equals(idHistorial))
        .findFirst()
        .ifPresent(d -> model.put("historial", Map.of(
            "bien", d.getBien().getSubcategoria().nombre(),
            "cantidad", d.getBien().getCantidad(),
            "cambios", d.getHistorialEstados().stream()
                .map(cambio -> Map.of(
                    "fecha", Objects.toString(cambio.fecha(), ""),
                    "descripcion", textoEstado(cambio.estadoNuevo()) + ": " + Objects.toString(cambio.justificacion(), "")))
                .toList(),
            "fotos", enlacesFotos(d, "donante"))));

    return model;
  }

  public Map<String, Object> asignadasAEntidad(Context ctx) {
    // El filtro viaja en la URL: ?estado=...
    String estado = Objects.requireNonNullElse(ctx.queryParam("estado"), "");

    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Donaciones asignadas");
    model.put("rol", "entidad");
    model.put("seccion", "donaciones");

    model.put("donaciones", donacionesRepository.buscarPorUsuarioEntidad(ctx.sessionAttribute(SesionUsuario.USUARIO_ID)).stream()
        .map(this::donacionParaVista)
        .filter(d -> estado.isEmpty() || estado.equals(d.get("estado")))
        .toList());
    model.put("estadoElegido", estado);
    // A la entidad solo le llegan donaciones ya asignadas: no tiene sentido filtrar por En depósito ni Vencida
    model.put("estados", Arrays.stream(EstadoDonacion.values())
        .filter(e -> e != EstadoDonacion.EN_DEPOSITO && e != EstadoDonacion.VENCIDA)
        .map(e -> Map.of("valor", e.name(), "texto", e.nombre()))
        .toList());
    return model;
  }

  public Map<String, Object> recepcion(Context ctx) {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Confirmar recepción");
    model.put("rol", "entidad");
    model.put("seccion", "donaciones");

    Donacion donacion = donacionDeLaEntidad(ctx);
    model.put("donacion", donacionParaVista(donacion));
    model.put("fotos", enlacesFotos(donacion, "entidad"));
    model.put("puedeConfirmar", donacion.getEstado() == EstadoDonacion.EN_TRASLADO);
    model.put("storageDisponible", fotosRecepcion.configurado());
    return model;
  }

  public void confirmarRecepcion(Context ctx) {
    TransaccionPorRequest.iniciarSiEsNecesario();
    Donacion donacion = donacionDeLaEntidad(ctx);
    // Serializa confirmaciones de la misma donación, incluso con dos pestañas.
    donacionesRepository.entityManager().refresh(donacion, javax.persistence.LockModeType.PESSIMISTIC_WRITE);
    if (!perteneceAEntidad(donacion, ctx.sessionAttribute(SesionUsuario.USUARIO_ID))) throw new NotFoundResponse();
    if (donacion.getEstado() != EstadoDonacion.EN_TRASLADO) {
      errorRecepcion(ctx, 409, "Solo podés confirmar donaciones en traslado.");
      return;
    }
    if (!fotosRecepcion.configurado()) {
      errorRecepcion(ctx, 503, "El almacenamiento de fotos no está disponible.");
      return;
    }
    if (ctx.req().getContentLengthLong() > 16L * 1024 * 1024) {
      errorRecepcion(ctx, 413, "La carga completa supera el límite de 16 MB.");
      return;
    }
    List<FotosRecepcion.Foto> fotos;
    if (!ctx.isMultipartFormData()) {
      errorRecepcion(ctx, 400, "Adjuntá las fotos con el formulario de recepción.");
      return;
    }
    ctx.req().setAttribute("org.eclipse.jetty.multipartConfig", new jakarta.servlet.MultipartConfigElement(
        System.getProperty("java.io.tmpdir"), FotosRecepcion.MAX_BYTES, 16L * 1024 * 1024, 0));
    try {
      fotos = fotosRecepcion.validar(ctx.uploadedFiles("fotos"));
    } catch (IllegalStateException e) {
      errorRecepcion(ctx, 413, "La carga supera el tamaño permitido.");
      return;
    } catch (IllegalArgumentException e) {
      errorRecepcion(ctx, 400, e.getMessage());
      return;
    }
    try {
      for (var foto : fotos) {
        TransaccionPorRequest.despuesDelRollback(() -> retirarFoto(foto.id()));
        fotosRecepcion.guardar(foto);
        donacion.agregarFotoRecepcion(foto.id(), foto.tipo());
      }
      donacion.cambiarEstado(EstadoDonacion.ENTREGADA, "Recepción confirmada por la entidad");
      donacionesRepository.entityManager().flush();
      ReportarEntregaResponse response = logisticaGrpcClient.reportarEntrega(donacion.getId(), true);
      if (!response.getExito()) {
        ctx.status(502).result("Logística no pudo confirmar la recepción. Volvé a intentarlo.");
        return;
      }
      ctx.redirect("/entidad/donaciones", HttpStatus.SEE_OTHER);
    } catch (RuntimeException e) {
      logger.warn("Falló la confirmación de recepción de la donación {} ({})",
          donacion.getId(), e.getClass().getSimpleName());
      ctx.status(502).result("No se pudo guardar la recepción. Volvé a intentarlo.");
    }
  }

  private void errorRecepcion(Context ctx, int status, String mensaje) {
    var modelo = recepcion(ctx);
    modelo.put("error", mensaje);
    ctx.status(status).render("entidad/recepcion", modelo);
  }

  private void retirarFoto(String id) {
    try {
      fotosRecepcion.borrar(id);
    } catch (RuntimeException e) {
      logger.warn("Quedó pendiente retirar la foto {}", id);
    }
  }

  public void fotoEntidad(Context ctx) {
    mostrarFoto(ctx, donacionDeLaEntidad(ctx));
  }

  public void fotoDonante(Context ctx) {
    Long usuarioId = ctx.sessionAttribute(SesionUsuario.USUARIO_ID);
    Donacion donacion = buscarDonacionDeRuta(ctx);
    var usuario = donacion.getDonante().getUsuario();
    if (usuario == null || !usuario.getId().equals(usuarioId)) throw new NotFoundResponse();
    mostrarFoto(ctx, donacion);
  }

  private void mostrarFoto(Context ctx, Donacion donacion) {
    String id = ctx.pathParam("foto");
    String tipo = donacion.getFotosRecepcion().get(id);
    if (tipo == null) throw new NotFoundResponse();
    try {
      byte[] bytes = fotosRecepcion.leer(id);
      if (bytes == null) throw new NotFoundResponse();
      ctx.header("Cache-Control", "no-store");
      ctx.header("X-Content-Type-Options", "nosniff");
      ctx.contentType(tipo).result(bytes);
    } catch (IllegalStateException e) {
      ctx.status(502).result("La foto no está disponible por el momento.");
    }
  }

  private List<Map<String, String>> enlacesFotos(Donacion donacion, String rol) {
    return donacion.getFotosRecepcion().keySet().stream().sorted()
        .map(id -> Map.of("url", "/" + rol + "/donaciones/" + donacion.getId() + "/fotos/" + id))
        .toList();
  }

  public void informarNoRecibida(Context ctx) {
    Long donacionId = donacionDeLaEntidad(ctx).getId();
    try {

      // Simplemente notificamos a Logística por gRPC
      ReportarEntregaResponse response = logisticaGrpcClient.reportarEntrega(donacionId, false);

      if (!response.getExito()) {
        ctx.status(500).result("Error al notificar a logística: " + response.getMensaje());
        return;
      }

      ctx.redirect("/entidad/donaciones");
    } catch (Exception e) {
      ctx.status(500).result("Error al procesar el rechazo: " + e.getMessage());
    }
  }

  private Donacion donacionDeLaEntidad(Context ctx) {
    Donacion donacion = buscarDonacionDeRuta(ctx);
    if (!perteneceAEntidad(donacion, ctx.sessionAttribute(SesionUsuario.USUARIO_ID))) throw new NotFoundResponse();
    return donacion;
  }

  private boolean perteneceAEntidad(Donacion donacion, Long usuarioId) {
    var entidad = donacion.getEntidadBeneficiaria();
    return entidad != null && entidad.getUsuario() != null && entidad.getUsuario().getId().equals(usuarioId);
  }

  private Donacion buscarDonacionDeRuta(Context ctx) {
    try {
      return donacionesRepository.buscarPorId(Long.valueOf(ctx.pathParam("id"))).orElseThrow(NotFoundResponse::new);
    } catch (NumberFormatException e) {
      throw new NotFoundResponse();
    }
  }

  private Map<String, String> donacionParaVista(Donacion donacion) {
    Bien bien = donacion.getBien();
    Map<String, String> fila = new HashMap<>();
    fila.put("id", donacion.getId().toString());
    fila.put("bien", bien.getSubcategoria().nombre());
    fila.put("cantidad", (bien.getCantidad() + " " + Objects.toString(bien.getUnidad(), "")).trim());
    fila.put("fecha", Objects.toString(donacion.getFecha(), ""));
    fila.put("actualizado", donacion.getHistorialEstados().stream()
        .map(RegistroCambioEstado::fecha).filter(Objects::nonNull).max(java.util.Date::compareTo)
        .map(java.util.Date::toString).orElse(fila.get("fecha")));
    fila.put("estado", donacion.getEstado().name());
    if (!donacion.getFotosRecepcion().isEmpty()) fila.put("tieneFotos", "true");
    fila.put("estadoTexto", textoEstado(donacion.getEstado()));
    fila.put("categoria", bien.getSubcategoria().getCategoria().name());
    fila.put("subcategoria", bien.getSubcategoria().name());
    fila.put("donante", donacion.getDonante().getNombre());
    var entidad = donacion.getEntidadBeneficiaria();
    fila.put("entidad", entidad == null ? "—" : entidad.getRazonSocial());
    return fila;
  }

  private String textoEstado(EstadoDonacion estado) {
    return estado.nombre();
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
