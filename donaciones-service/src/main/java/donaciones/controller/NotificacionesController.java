package donaciones.controller;

import donaciones.repository.NotificacionRepository;
import donaciones.domain.notificacion.Notificacion;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.PersonasAdministradorasRepository;
import donaciones.web.SesionUsuario;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import java.time.format.DateTimeFormatter;
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
            .map(n -> Map.of("fecha", n.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), "mensaje", n.getMensaje(),
                "id", n.getId(), "leida", n.isLeida()))
            .toList());
  }

  public void marcarLeidaDonante(Context ctx) {
    marcarLeida(ctx, "PERSONA", idDonante(ctx));
    ctx.redirect("/donante/notificaciones");
  }

  public void marcarTodasLeidasDonante(Context ctx) {
    notificacionRepository.buscarPorReceptor("PERSONA", idDonante(ctx)).forEach(Notificacion::marcarComoLeida);
    ctx.redirect("/donante/notificaciones");
  }

  public void marcarLeidaEntidad(Context ctx) {
    marcarLeida(ctx, "ENTIDAD", idEntidad(ctx));
    ctx.redirect("/entidad/notificaciones");
  }

  public void marcarTodasLeidasEntidad(Context ctx) {
    notificacionRepository.buscarPorReceptor("ENTIDAD", idEntidad(ctx)).forEach(Notificacion::marcarComoLeida);
    ctx.redirect("/entidad/notificaciones");
  }

  private void marcarLeida(Context ctx, String tipo, Long id) {
    notificacionRepository.buscarDelReceptor(Long.valueOf(ctx.pathParam("id")), tipo, id)
        .orElseThrow(NotFoundResponse::new)
        .marcarComoLeida();
  }

  private Long idDonante(Context ctx) {
    return new RepositorioPersonas().buscarPorUsuario(ctx.sessionAttribute(SesionUsuario.USUARIO_ID))
        .orElseThrow(NotFoundResponse::new).getId();
  }

  private Long idEntidad(Context ctx) {
    return new EntidadBeneficiariaRepository().buscarPorUsuario(ctx.sessionAttribute(SesionUsuario.USUARIO_ID))
        .orElseThrow(NotFoundResponse::new).getId();
  }

  public Map<String, Object> notificacionesAdmin(Context ctx) {
    return bandeja("admin", "ADMINISTRADORA", idAdministradora(ctx));
  }

  public void marcarLeidaAdmin(Context ctx) {
    marcarLeida(ctx, "ADMINISTRADORA", idAdministradora(ctx));
    ctx.redirect("/admin/notificaciones");
  }

  public void marcarTodasLeidasAdmin(Context ctx) {
    notificacionRepository.buscarPorReceptor("ADMINISTRADORA", idAdministradora(ctx)).forEach(Notificacion::marcarComoLeida);
    ctx.redirect("/admin/notificaciones");
  }

  private Long idAdministradora(Context ctx) {
    return new PersonasAdministradorasRepository().buscarPorUsuario(ctx.sessionAttribute(SesionUsuario.USUARIO_ID))
        .orElseThrow(NotFoundResponse::new).getId();
  }
}
