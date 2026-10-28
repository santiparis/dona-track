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

  public static List<Map<String, String>> estados() {
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

  public static List<Map<String, String>> categorias() {
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

  // Los textos entre corchetes quedan como en el boceto: el dominio de incentivos todavía no está definido
  public static Map<String, Object> incentivos() {
    return Map.of(
        "categoria", Map.of("nombre", "[CATEGORÍA ACTUAL]", "porcentaje", "70",
            "faltan", "Te faltan [N] donaciones para llegar a [PRÓXIMA CATEGORÍA]"),
        "resumenMisiones", "2 en curso · 1 cumplida",
        "misiones", List.of(
            Map.of("titulo", "[Misión] Doná alimentos 3 meses seguidos", "cumplida", false,
                "porcentaje", "66", "detalle", "2 de 3 · Recompensa: insignia [NOMBRE]"),
            Map.of("titulo", "[Misión] Doná a 3 entidades distintas", "cumplida", false,
                "porcentaje", "33", "detalle", "1 de 3"),
            Map.of("titulo", "[Misión] Primera donación", "cumplida", true,
                "porcentaje", "100", "detalle", "Cumplida el 02/09/2026")),
        "insignias", List.of(
            Map.of("nombre", "[Insignia 1]", "obtenida", true),
            Map.of("nombre", "[Insignia 2]", "obtenida", true),
            Map.of("nombre", "[Insignia 3]", "obtenida", false),
            Map.of("nombre", "[Insignia 4]", "obtenida", false))
    );
  }

  // Todas las notificaciones juntas, como las devuelve el repositorio: cada controller filtra por destinatario
  public static List<Map<String, Object>> notificaciones() {
    return List.of(
        Map.of("destinatario", "donante", "tipo", "Donación asignada", "fecha", "Hoy 03:00", "leida", false,
            "mensaje", "Tu donación de 6 sillas fue asignada a Escuela Rural N°10.",
            "link", "/donante/donaciones", "linkTexto", "Ver donación"),
        Map.of("destinatario", "donante", "tipo", "Misión cumplida", "fecha", "Ayer 18:20", "leida", false,
            "mensaje", "Cumpliste la misión [Primera donación].",
            "link", "/donante/incentivos", "linkTexto", "Ver incentivos"),
        Map.of("destinatario", "donante", "tipo", "Subiste de categoría", "fecha", "15/10/2026", "leida", true,
            "mensaje", "Ahora sos [CATEGORÍA]."),
        Map.of("destinatario", "donante", "tipo", "Entrega recibida", "fecha", "02/09/2026", "leida", true,
            "mensaje", "Escuela Rural N°10 recibió tus 10 bancos. Camión AB123CD, 02/09/2026 11:40."),
        Map.of("destinatario", "entidad", "tipo", "Donación asignada", "fecha", "Hoy 03:00", "leida", false,
            "mensaje", "Se te asignaron 6 sillas usadas de Arcos Plateados S.A.",
            "link", "/entidad/donaciones", "linkTexto", "Ver donaciones"),
        Map.of("destinatario", "entidad", "tipo", "Recepción confirmada", "fecha", "02/09/2026", "leida", true,
            "mensaje", "Registramos la recepción de 10 bancos. Comprobante: camión AB123CD, 02/09/2026 11:40.")
    );
  }

  // "donacion" es lo que ve el donante (su donación); "trae" y "paradasAntes" es lo que ve la entidad del destino
  public static List<Map<String, String>> camionesEnRuta() {
    return List.of(
        Map.of("patente", "AB123CD", "donacion", "Sillas · 6 unidades", "destino", "Escuela Rural N°10",
            "trae", "Sillas · 6 unidades, Mesas · 1 unidad", "paradasAntes", "1",
            "ubicacion", "-34.4521, -59.4467", "velocidad", "62 km/h", "actualizacion", "Hace 1 min (10:42)"),
        Map.of("patente", "AE456FG", "donacion", "Fideos · 100 paquetes", "destino", "Comedor Escobar Sonrisas",
            "trae", "Fideos · 100 paquetes", "paradasAntes", "0",
            "ubicacion", "-34.3489, -58.7956", "velocidad", "0 km/h", "actualizacion", "Hace 4 min (10:39)")
    );
  }

  public static Map<String, Object> admin() {
    return Map.of("nombre", "admin@donatrack", "notificacionesSinLeer", 0);
  }

  public static Map<String, Object> entidad() {
    return Map.of("nombre", "Escuela Rural N°10", "notificacionesSinLeer", 1);
  }

  public static List<Map<String, String>> necesidadesDeLaEntidad() {
    return List.of(
        Map.of("id", "1", "subcategoria", "BANCOS", "subcategoriaTexto", "Bancos", "descripcion", "Reposición tras inundación",
            "tipo", "EXTRAORDINARIA", "tipoTexto", "Extraordinaria", "periodo", "",
            "cantidad", "30", "avance", "10 / 30", "porcentaje", "33"),
        Map.of("id", "2", "subcategoria", "SILLAS", "subcategoriaTexto", "Sillas", "descripcion", "Reposición tras inundación",
            "tipo", "EXTRAORDINARIA", "tipoTexto", "Extraordinaria", "periodo", "",
            "cantidad", "30", "avance", "6 / 30", "porcentaje", "20"),
        Map.of("id", "3", "subcategoria", "LECHE", "subcategoriaTexto", "Leche", "descripcion", "Desayuno escolar",
            "tipo", "RECURRENTE", "tipoTexto", "Recurrente · mensual", "periodo", "MENSUAL",
            "cantidad", "50", "avance", "40 / 50", "porcentaje", "80")
    );
  }

  public static List<Map<String, String>> donacionesAsignadas() {
    return List.of(
        Map.of("id", "12", "bien", "Sillas · usadas", "cantidad", "6 unidades", "donante", "Arcos Plateados S.A.",
            "estado", "EN_TRASLADO", "estadoTexto", "En traslado", "actualizado", "Hoy 08:30", "camion", "AB123CD"),
        Map.of("id", "13", "bien", "Mesas · usada", "cantidad", "1 unidad", "donante", "Arcos Plateados S.A.",
            "estado", "LISTA_PARA_ENTREGAR", "estadoTexto", "Lista para entregar", "actualizado", "Ayer 03:00", "camion", "AE456FG"),
        Map.of("id", "7", "bien", "Bancos · nuevos", "cantidad", "10 unidades", "donante", "Ana Pérez",
            "estado", "ENTREGADA", "estadoTexto", "Entregada", "actualizado", "02/09/2026", "camion", "AB123CD"),
        Map.of("id", "5", "bien", "Bancos · usados", "cantidad", "4 unidades", "donante", "Ana Pérez",
            "estado", "ENTREGA_FALLIDA", "estadoTexto", "Entrega fallida", "actualizado", "28/08/2026", "camion", "AE456FG")
    );
  }

  public static List<Map<String, Object>> entidadesConNecesidades() {
    return List.of(
        Map.of("nombre", "Escuela Rural N°10", "direccion", "Ruta 5 km 32, San Andrés de Giles", "telefono", "(02325) 44-1234",
            "necesidades", List.of(
                Map.of("subcategoria", "BANCOS", "subcategoriaTexto", "Bancos", "tipo", "Extraordinaria",
                    "porcentaje", "33", "detalle", "10 de 30 unidades · Reposición tras inundación"),
                Map.of("subcategoria", "SILLAS", "subcategoriaTexto", "Sillas", "tipo", "Extraordinaria",
                    "porcentaje", "20", "detalle", "6 de 30 unidades"))),
        Map.of("nombre", "Comedor Escobar Sonrisas", "direccion", "Av. Tapia de Cruz 1200, Escobar", "telefono", "(0348) 442-5678",
            "necesidades", List.of(
                Map.of("subcategoria", "FIDEOS", "subcategoriaTexto", "Fideos", "tipo", "Recurrente · semanal",
                    "porcentaje", "60", "detalle", "60 de 100 paquetes esta semana"))),
        Map.of("nombre", "Espacio de Tutorías Barrio Norte", "direccion", "Calle 14 N°560, La Plata", "telefono", "(0221) 455-0000",
            "necesidades", List.of(
                Map.of("subcategoria", "MESAS", "subcategoriaTexto", "Mesas", "tipo", "Extraordinaria",
                    "porcentaje", "0", "detalle", "0 de 5 unidades")))
    );
  }
}
