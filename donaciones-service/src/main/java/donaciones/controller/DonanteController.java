package donaciones.controller;

import donaciones.domain.donante.Contacto;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.PersonaJuridica;
import donaciones.dto.ContactoResponseDTO;
import donaciones.dto.DonanteRequestDTO;
import donaciones.dto.DonanteResponseDTO;
import donaciones.service.DonanteService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DonanteController {
  private static final Logger logger = LoggerFactory.getLogger(DonanteController.class);

  private final DonanteService donanteService;

  public DonanteController(DonanteService donanteService) {
    this.donanteService = donanteService;
  }

  public void listar(Context ctx) {
    try {
      List<DonanteResponseDTO> respuesta = donanteService.listarDonantes()
          .stream()
          .map(DonanteController::toResponseDTO)
          .toList();
      ctx.json(respuesta);
    } catch (RuntimeException e) {
      logger.error("Error al listar donantes", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al listar donantes: " + e.getMessage());
    }
  }

  public void crear(Context ctx) {
    try {
      DonanteRequestDTO dto = ctx.bodyAsClass(DonanteRequestDTO.class);
      donanteService.crearDonante(dto);
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
      String documento = ctx.pathParam("documento");
      DonanteRequestDTO dto = ctx.bodyAsClass(DonanteRequestDTO.class);
      donanteService.actualizarDonante(documento, dto);
      ctx.result("Datos del donante actualizados");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al actualizar donante: {}", e.getMessage());
      ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al actualizar donante", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al actualizar donante: " + e.getMessage());
    }
  }

  public void eliminar(Context ctx) {
    try {
      String documento = ctx.pathParam("documento");
      donanteService.eliminarDonante(documento);
      ctx.result("Donante eliminado");
    } catch (RuntimeException e) {
      logger.error("Error al eliminar donante", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al eliminar donante: " + e.getMessage());
    }
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
