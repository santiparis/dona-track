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

  public static Map<String, Object> donante() {
    return Map.of("nombre", "Ana Pérez", "notificacionesSinLeer", 2);
  }

  public static List<Map<String, String>> donacionesDelDonante() {
    return List.of(
        Map.of("id", "1", "fecha", "20/10/2026", "bien", "Sillas · usadas", "cantidad", "6 unidades",
            "categoria", "MUEBLES", "subcategoria", "SILLAS",
            "estado", "EN_TRASLADO", "estadoTexto", "En traslado", "entidad", "Escuela Rural N°10"),
        Map.of("id", "2", "fecha", "20/10/2026", "bien", "Mesas · usada", "cantidad", "1 unidad",
            "categoria", "MUEBLES", "subcategoria", "MESAS",
            "estado", "ASIGNADA", "estadoTexto", "Asignación realizada", "entidad", "Escuela Rural N°10"),
        Map.of("id", "3", "fecha", "02/09/2026", "bien", "Fideos", "cantidad", "40 paquetes",
            "categoria", "ALIMENTOS", "subcategoria", "FIDEOS",
            "estado", "ENTREGADA", "estadoTexto", "Entregada", "entidad", "Comedor Escobar Sonrisas")
    );
  }

  public static List<Map<String, Object>> historialDeEstados() {
    return List.of(
        Map.of("fecha", "20/10/2026 09:12", "descripcion", "En depósito"),
        Map.of("fecha", "21/10/2026 03:00", "descripcion", "Asignación realizada a Escuela Rural N°10"),
        Map.of("fecha", "22/10/2026 03:00", "descripcion", "Lista para entregar"),
        Map.of("fecha", "23/10/2026 08:30", "descripcion", "En traslado (camión AB123CD)")
    );
  }

  public static List<Map<String, Object>> estados() {
    return List.of(
        Map.of("valor", "EN_DEPOSITO", "texto", "En depósito"),
        Map.of("valor", "ASIGNADA", "texto", "Asignación realizada"),
        Map.of("valor", "LISTA_PARA_ENTREGAR", "texto", "Lista para entregar"),
        Map.of("valor", "EN_TRASLADO", "texto", "En traslado"),
        Map.of("valor", "ENTREGADA", "texto", "Entregada"),
        Map.of("valor", "ENTREGA_FALLIDA", "texto", "Entrega fallida"),
        Map.of("valor", "VENCIDA", "texto", "Vencida")
    );
  }

  public static List<Map<String, Object>> categorias() {
    return List.of(
        Map.of("valor", "ALIMENTOS", "texto", "Alimentos"),
        Map.of("valor", "MUEBLES", "texto", "Muebles"),
        Map.of("valor", "ROPA", "texto", "Ropa")
    );
  }

  public static List<Map<String, String>> subcategorias(String categoria) {
    return switch (categoria) {
      case "ALIMENTOS" -> List.of(
          Map.of("valor", "FIDEOS", "texto", "Fideos"),
          Map.of("valor", "LECHE", "texto", "Leche"));
      case "MUEBLES" -> List.of(
          Map.of("valor", "BANCOS", "texto", "Bancos"),
          Map.of("valor", "SILLAS", "texto", "Sillas"),
          Map.of("valor", "MESAS", "texto", "Mesas"));
      case "ROPA" -> List.of(
          Map.of("valor", "ABRIGOS", "texto", "Abrigos"),
          Map.of("valor", "CALZADO", "texto", "Calzado"));
      default -> List.of();
    };
  }
}
