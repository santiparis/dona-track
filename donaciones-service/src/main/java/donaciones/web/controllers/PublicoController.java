package donaciones.web.controllers;

import java.util.HashMap;
import java.util.Map;

public class PublicoController {

  public Map<String, Object> inicio() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Inicio");
    model.put("seccion", "inicio");
    return model;
  }
}
