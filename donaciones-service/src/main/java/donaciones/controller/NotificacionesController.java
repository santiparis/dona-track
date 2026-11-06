package donaciones.controller;

import donaciones.repository.NotificacionRepository;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.web.SesionUsuario;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.util.Map;

public class NotificacionesController {

  private final NotificacionRepository notificacionRepository;

  public NotificacionesController(NotificacionRepository notificacionRepository) {
    this.notificacionRepository = notificacionRepository;
  }

  public Map<String, Object> notificacionesDonante(Context ctx) {
    var donante = new RepositorioPersonas().buscarPorUsuario(ctx.sessionAttribute(SesionUsuario.USUARIO_ID))
        .orElseThrow(NotFoundResponse::new);
    return bandeja("donante", "PERSONA", donante.getId());
  }

  public Map<String, Object> notificacionesEntidad(Context ctx) {
    var entidad = new EntidadBeneficiariaRepository().buscarPorUsuario(ctx.sessionAttribute(SesionUsuario.USUARIO_ID))
        .orElseThrow(NotFoundResponse::new);
    return bandeja("entidad", "ENTIDAD", entidad.getId());
  }

  private Map<String, Object> bandeja(String rol, String tipo, Long id) {
    return Map.of("titulo", "Notificaciones", "rol", rol, "seccion", "notificaciones",
        "notificaciones", notificacionRepository.buscarPorReceptor(tipo, id).stream()
            .map(n -> Map.of("fecha", n.getFecha().toString(), "mensaje", n.getMensaje()))
            .toList());
  }
}
