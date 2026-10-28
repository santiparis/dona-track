package donaciones.web.controllers;

import donaciones.web.DatosMock;

import java.util.HashMap;
import java.util.Map;

public class PublicoController {

  public Map<String, Object> inicio() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Inicio");
    model.put("seccion", "inicio");
    return model;
  }

  public Map<String, Object> legal() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Legal y privacidad");
    model.put("seccion", "legal");
    return model;
  }

  public Map<String, Object> entregadas() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Donaciones entregadas");
    model.put("seccion", "entregadas");
    // List<Donacion> entregadas = donacionRepository.obtenerTodas().stream().filter(d -> d.getEstado() == EstadoDonacion.ENTREGADA).toList();
    model.put("entregas", DatosMock.donacionesEntregadas());   // MOCK
    return model;
  }
}
