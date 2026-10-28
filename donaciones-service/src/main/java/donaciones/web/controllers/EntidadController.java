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

  public Map<String, Object> seguimiento() {
    Map<String, Object> model = new HashMap<>();
    model.put("titulo", "Seguimiento de entregas");
    model.put("rol", "entidad");
    model.put("seccion", "seguimiento");
    Map<String, Object> entidad = DatosMock.entidad();   // MOCK
    model.put("usuario", entidad);
    model.put("subtitulo", "Camiones en camino con donaciones para tu entidad.");
    model.put("textoMapa", "Un marcador por camión y la ubicación de tu entidad");
    // Las ubicaciones vienen de logistica-service por HTTP (endpoint a definir en LogisticaAPICalls):
    // List<UbicacionCamionDTO> camiones = logisticaAPICalls.camionesConDestino(idEntidad).execute().body();
    model.put("camiones", DatosMock.camionesEnRuta().stream()
        .filter(c -> c.get("destino").equals(entidad.get("nombre")))
        .toList());   // MOCK
    return model;
  }
}
