package donaciones.controller;

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
}
