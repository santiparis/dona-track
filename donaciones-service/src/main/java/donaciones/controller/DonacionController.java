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
import donaciones.service.excepcion.CategoriaInvalidaException;
import donaciones.service.excepcion.DonanteNoEncontradoException;
import donaciones.service.excepcion.EstadoBienInvalidoException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

  public void marcarEnTraslado(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      Donacion donacion = donacionesRepository.buscarPorId(id)
          .orElseThrow(() -> new IllegalArgumentException("No se encontró la donación"));
      donacion.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);

      notificador.inicioDeTraslado(donacion, ctx.queryParam("urlMapa"));

      ctx.result("Donación en traslado");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al marcar la donacion en traslado: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al marcar la donacion en traslado", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al cambiar estado: " + e.getMessage());
    }
  }

  public void confirmarEntrega(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      Donacion donacion = donacionesRepository.buscarPorId(id)
          .orElseThrow(() -> new IllegalArgumentException("No se encontró la donación"));
      donacion.cambiarEstado(EstadoDonacion.ENTREGADA, null);

      notificador.entregaConfirmada(donacion, ctx.queryParam("nombreCamion"));

      ctx.result("Entrega confirmada");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al confirmar la entrega: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al confirmar la entrega", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al cambiar estado: " + e.getMessage());
    }
  }

  public void registrarEntregaFallida(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      String motivo = ctx.queryParam("motivo");
      Donacion donacion = donacionesRepository.buscarPorId(id)
          .orElseThrow(() -> new IllegalArgumentException("No se encontró la donación"));
      donacion.cambiarEstado(EstadoDonacion.ENTREGA_FALLIDA, motivo);

      notificador.entregaFallida(donacion, motivo, administradorasRepository.obtenerTodos());

      ctx.result("Entrega fallida registrada");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al registrar la entrega fallida: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al registrar la entrega fallida", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al cambiar estado: " + e.getMessage());
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
      this.actualizarDonacionParcial(id, dto);
      ctx.result("Donación actualizada parcialmente");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al actualizar parcialmente donacion: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al actualizar parcialmente donacion", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al actualizar donacion: " + e.getMessage());
    }
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
        throw new DonanteNoEncontradoException("No se encontró el donante con id: " + dto.idDonante());
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
      throw new CategoriaInvalidaException("Categoría inválida: " + subcategoria);
    }

    try {
      return Subcategoria.valueOf(valor);
    } catch (IllegalArgumentException e) {
      try {
        Categoria categoria = Categoria.valueOf(valor);
        return Arrays.stream(Subcategoria.values())
            .filter(sub -> sub.getCategoria() == categoria)
            .findFirst()
            .orElseThrow(() -> new CategoriaInvalidaException("Categoría inválida: " + subcategoria));
      } catch (IllegalArgumentException ignored) {
        throw new CategoriaInvalidaException("Categoría inválida: " + subcategoria, e);
      }
    }
  }

  public EstadoBien parsearEstado(String nombreEstado) {
    try {
      return EstadoBien.valueOf(nombreEstado.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new EstadoBienInvalidoException("Estado de bien inválido: " + nombreEstado, e);
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
        throw new DonanteNoEncontradoException("No se encontró el donante con id: " + dto.idDonante());
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
        throw new DonanteNoEncontradoException("No se encontró el donante con id: " + dto.idDonante());
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
}
