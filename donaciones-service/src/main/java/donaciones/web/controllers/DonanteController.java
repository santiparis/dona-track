package donaciones.web.controllers;

import donaciones.web.DatosMock;

import java.util.HashMap;
import java.util.Map;

public class DonanteController {

  public Map<String, Object> notificaciones() {
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
}
