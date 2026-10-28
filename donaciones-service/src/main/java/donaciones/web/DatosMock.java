package donaciones.web;

import java.util.List;
import java.util.Map;

public class DatosMock {

  public static List<Map<String, Object>> donacionesEntregadas() {
    return List.of(
        Map.of("entidad", "Escuela Rural N°10", "bien", "Bancos", "cantidad", "30 unidades", "fecha", "12/10/2026"),
        Map.of("entidad", "Comedor Escobar Sonrisas", "bien", "Fideos", "cantidad", "100 paquetes", "fecha", "08/10/2026"),
        Map.of("entidad", "Escuela Rural N°10", "bien", "Sillas", "cantidad", "30 unidades", "fecha", "05/10/2026"),
        Map.of("entidad", "Comedor Escobar Sonrisas", "bien", "Leche", "cantidad", "50 litros", "fecha", "01/10/2026")
    );
  }
}
