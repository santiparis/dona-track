package donaciones.controller;

import donaciones.domain.notificacion.Contacto;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.PersonaJuridica;
import donaciones.dto.ContactoResponseDTO;
import donaciones.dto.DonantePatchDTO;
import donaciones.dto.DonanteRequestDTO;
import donaciones.dto.DonanteResponseDTO;
import donaciones.dto.ContactoDTO;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.domain.notificacion.ContactoPorEmail;
import donaciones.domain.notificacion.ContactoPorSMS;
import donaciones.domain.notificacion.ContactoPorWhatsApp;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

public class DonanteController {
  private static final Logger logger = LoggerFactory.getLogger(DonanteController.class);

  private final RepositorioPersonas personasRepository;

  public DonanteController(RepositorioPersonas personasRepository) {
    this.personasRepository = personasRepository;
  }

  public void listar(Context ctx) {
    try {
      List<DonanteResponseDTO> respuesta = personasRepository.obtenerTodas()
          .stream()
          .map(DonanteController::toResponseDTO)
          .toList();
      ctx.json(respuesta);
    } catch (RuntimeException e) {
      logger.error("Error al listar donantes", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al listar donantes: " + e.getMessage());
    }
  }

  public void obtener(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
        Persona donante = personasRepository.buscarPorId(id)
          .orElseThrow(() -> new IllegalArgumentException("No se encontró un donante"));
        ctx.json(toResponseDTO(donante));
    } catch (IllegalArgumentException e) {
      logger.warn("Error al obtener donante: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al obtener donante", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al obtener donante: " + e.getMessage());
    }
  }

  public void crear(Context ctx) {
    try {
      DonanteRequestDTO dto = ctx.bodyAsClass(DonanteRequestDTO.class);
      personasRepository.agregar(crearPersona(dto));
      ctx.status(HttpStatus.CREATED).result("Donante registrado");
    } catch (IllegalArgumentException e) {
      logger.warn("Error de validacion al crear donante: {}", e.getMessage());
      ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al crear donante", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al crear donante: " + e.getMessage());
    }
  }

  public void actualizar(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      DonanteRequestDTO dto = ctx.bodyAsClass(DonanteRequestDTO.class);
      Persona existente = personasRepository.buscarPorId(id)
          .orElseThrow(() -> new IllegalArgumentException("No se encontró un donante"));
      existente.actualizarseDesde(crearPersona(dto));
      personasRepository.agregar(existente);
      ctx.result("Datos del donante actualizados");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al actualizar donante: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al actualizar donante", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al actualizar donante: " + e.getMessage());
    }
  }

  public void actualizarParcial(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      DonantePatchDTO dto = ctx.bodyAsClass(DonantePatchDTO.class);
      Persona existente = personasRepository.buscarPorId(id)
          .orElseThrow(() -> new IllegalArgumentException("No se encontró un donante"));
      List<Contacto> contactos = dto.contactos() == null ? null : crearContactos(dto.contactos());
      existente.actualizarDatosParciales(
          dto.nombre(), dto.documento(), dto.apellido(), dto.edad(), dto.direccion(), dto.rubro(), contactos);
      personasRepository.agregar(existente);
      ctx.result("Datos del donante actualizados parcialmente");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al actualizar parcialmente donante: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al actualizar parcialmente donante", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al actualizar donante: " + e.getMessage());
    }
  }

  public void eliminar(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      personasRepository.eliminarPorId(id);
      ctx.result("Donante eliminado");
    } catch (RuntimeException e) {
      logger.error("Error al eliminar donante", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al eliminar donante: " + e.getMessage());
    }
  }

  /** Traduce el contrato HTTP a objetos del dominio; no es una regla de negocio. */
  private Persona crearPersona(DonanteRequestDTO dto) {
    List<Contacto> contactos = crearContactos(dto.contactos());
    Contacto medioPredeterminado = contactos.get(0);

    return switch (dto.tipo().toUpperCase()) {
      case "HUMANA" -> new PersonaHumana(
          dto.nombre(), dto.apellido(), dto.edad(), null, dto.documento(), null,
          dto.direccion(), contactos, medioPredeterminado, null);
      case "JURIDICA" -> new PersonaJuridica(
          null, dto.documento(), dto.nombre(), null, dto.rubro(), new ArrayList<>(),
          contactos, medioPredeterminado, null);
      default -> throw new IllegalArgumentException("Tipo no compatible");
    };
  }

  private List<Contacto> crearContactos(List<ContactoDTO> contactosDto) {
    if (contactosDto == null || contactosDto.isEmpty()) {
      throw new IllegalArgumentException("La lista de contactos no puede ser nula o vacía");
    }
    return contactosDto.stream().map(this::crearContacto).toList();
  }

  private Contacto crearContacto(ContactoDTO dto) {
    return switch (dto.estrategia().toUpperCase()) {
      case "EMAIL" -> new ContactoPorEmail(dto.valor());
      case "WHATSAPP" -> new ContactoPorWhatsApp(dto.valor());
      case "SMS" -> new ContactoPorSMS(dto.valor());
      default -> throw new IllegalArgumentException("Estrategia de notificacion invalida: " + dto.estrategia());
    };
  }

  private static DonanteResponseDTO toResponseDTO(Persona persona) {
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

  private static List<ContactoResponseDTO> toContactosDTO(List<Contacto> contactos) {
    return contactos.stream()
        .map(contacto -> new ContactoResponseDTO(contacto.getEstrategia(), contacto.getValor()))
        .toList();
  }
}
