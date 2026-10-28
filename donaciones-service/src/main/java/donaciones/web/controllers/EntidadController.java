package donaciones.web.controllers;

import donaciones.web.DatosMock;

import java.util.HashMap;
import java.util.Map;

public class EntidadController {

  public Map<String, Object> notificaciones() {
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
