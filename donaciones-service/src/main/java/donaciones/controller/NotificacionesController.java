package donaciones.controller;

import donaciones.repository.NotificacionRepository;
import donaciones.web.DatosMock;

import java.util.HashMap;
import java.util.Map;

public class NotificacionesController {

  private final NotificacionRepository notificacionRepository;

  public NotificacionesController(NotificacionRepository notificacionRepository) {
    this.notificacionRepository = notificacionRepository;
  }

  public Map<String, Object> notificacionesDonante() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Notificaciones");
    model.put("rol", "donante");
    model.put("seccion", "notificaciones");
    model.put("usuario", DatosMock.donante());   // MOCK
    model.put("aviso", "También te llegan por tu medio preferido: correo.");   // MOCK
    // List<Notificacion> notificaciones = notificacionRepository.obtenerTodas().stream().filter(n -> n.getReceptorId().equals(idDonante)).toList();
    model.put("notificaciones", DatosMock.notificaciones().stream()
        .filter(n -> "donante".equals(n.get("destinatario")))
        .toList());   // MOCK
    return model;
  }

  public Map<String, Object> notificacionesEntidad() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Notificaciones");
    model.put("rol", "entidad");
    model.put("seccion", "notificaciones");
    model.put("usuario", DatosMock.entidad());   // MOCK
    model.put("aviso", "También llegan a los correos de los representantes.");
    // List<Notificacion> notificaciones = notificacionRepository.obtenerTodas().stream().filter(n -> n.getReceptorId().equals(idEntidad)).toList();
    model.put("notificaciones", DatosMock.notificaciones().stream()
        .filter(n -> "entidad".equals(n.get("destinatario")))
        .toList());   // MOCK
    return model;
  }
}
