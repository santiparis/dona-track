package donaciones.controller;

import donaciones.domain.Bien;
import donaciones.domain.Donacion;
import donaciones.domain.RegistroCambioEstado;
import donaciones.domain.donante.Contacto;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.PersonaJuridica;
import donaciones.dto.BienResponseDTO;
import donaciones.dto.ContactoResponseDTO;
import donaciones.dto.DonacionRequestDTO;
import donaciones.dto.DonacionResponseDTO;
import donaciones.dto.DonanteResponseDTO;
import donaciones.dto.RegistroCambioEstadoDTO;
import donaciones.service.DonacionService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DonacionController {
  private static final Logger logger = LoggerFactory.getLogger(DonacionController.class);

  private final DonacionService donacionService;

  public DonacionController(DonacionService service) {
    this.donacionService = service;
  }

  public void listar(Context ctx) {
    try {
      List<DonacionResponseDTO> respuesta = donacionService.listarDonaciones()
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
      donacionService.crearDonacion(dto);
      ctx.status(HttpStatus.CREATED).result("Donación recibida y guardada");
    } catch (IllegalArgumentException e) {
      logger.warn("Error de validacion al crear donacion: {}", e.getMessage());
      ctx.status(HttpStatus.BAD_REQUEST).result("Error: " + e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al crear donacion", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al crear donacion: " + e.getMessage());
    }
  }

  public void cambiarEstado(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      String nuevoEstadoTexto = ctx.queryParam("nuevo");
      String nombreCamion =  ctx.queryParam("nombreCamion");
      donacionService.cambiarEstado(id, nuevoEstadoTexto, nombreCamion);
      ctx.result("Estado actualizado");
    } catch (IllegalArgumentException e) {
      logger.warn("Error al cambiar estado de donacion: {}", e.getMessage());
      ctx.status(HttpStatus.BAD_REQUEST).result("Error: " + e.getMessage());
    } catch (RuntimeException e) {
      logger.error("Error inesperado al cambiar estado de donacion", e);
      ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Error al cambiar estado: " + e.getMessage());
    }
  }

  public void actualizar(Context ctx) {
    try {
      Long id = Long.parseLong(ctx.pathParam("id"));
      DonacionRequestDTO dto = ctx.bodyAsClass(DonacionRequestDTO.class);
      donacionService.actualizarDonacion(id, dto);
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
      donaciones.dto.DonacionPatchDTO dto = ctx.bodyAsClass(donaciones.dto.DonacionPatchDTO.class);
      donacionService.actualizarDonacionParcial(id, dto);
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
      donacionService.eliminarDonacion(id);
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
}
