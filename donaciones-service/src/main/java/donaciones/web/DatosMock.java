package donaciones.web;

import java.util.List;
import java.util.Map;

public class DatosMock {

  // latitud y longitud son las de la entidad que recibió la entrega: ahí va el marcador del mapa.
  // foto es una URL, como Bien.foto (las del mock son fotos libres de Wikimedia Commons)
  public static List<Map<String, Object>> donacionesEntregadas() {
    String commons = "https://thumb.wikimedia.org/wikipedia/commons/thumb/";
    return List.of(
        Map.of("entidad", "Escuela Rural N°10", "bien", "Bancos", "cantidad", "30 unidades", "fecha", "12/10/2026",
            "latitud", -34.4705, "longitud", -59.4890,
            "foto", commons + "e/e9/Classroom_-_Basic_Desks_-_Loxton_Historical_Village_%282020-10-22_09.30.20_by_Michael_Coghlan%29.jpg/960px-Classroom_-_Basic_Desks_-_Loxton_Historical_Village_%282020-10-22_09.30.20_by_Michael_Coghlan%29.jpg"),
        Map.of("entidad", "Comedor Escobar Sonrisas", "bien", "Fideos", "cantidad", "100 paquetes", "fecha", "08/10/2026",
            "latitud", -34.3467, "longitud", -58.7920,
            "foto", commons + "2/26/Pasta_products_from_the_Albert_Heijn%2C_Winschoten_%282018%29.jpg/960px-Pasta_products_from_the_Albert_Heijn%2C_Winschoten_%282018%29.jpg"),
        Map.of("entidad", "Escuela Rural N°10", "bien", "Sillas", "cantidad", "30 unidades", "fecha", "05/10/2026",
            "latitud", -34.4705, "longitud", -59.4890,
            "foto", "https://upload.wikimedia.org/wikipedia/commons/5/51/Wooden_Chair.jpg"),
        Map.of("entidad", "Comedor Escobar Sonrisas", "bien", "Leche", "cantidad", "50 litros", "fecha", "01/10/2026",
            "latitud", -34.3467, "longitud", -58.7920,
            "foto", commons + "2/2d/Cartons_of_Nature%27s_Promise_milk_%2850855058622%29.jpg/960px-Cartons_of_Nature%27s_Promise_milk_%2850855058622%29.jpg"),
        Map.of("entidad", "Merendero Mitaí (Posadas, Misiones)", "bien", "Leche", "cantidad", "60 litros", "fecha", "26/09/2026",
            "latitud", -27.3671, "longitud", -55.8961,
            "foto", commons + "7/7f/Tetra-Milk-Carton---Betsukai-Milk---2024-04-27_08.jpg/960px-Tetra-Milk-Carton---Betsukai-Milk---2024-04-27_08.jpg"),
        Map.of("entidad", "Hogar Escuela Bariloche (Río Negro)", "bien", "Camperas", "cantidad", "40 unidades", "fecha", "22/09/2026",
            "latitud", -41.1335, "longitud", -71.3103,
            "foto", commons + "e/e9/Blazer_Jackets_on_Rack_%28Unsplash%29.jpg/960px-Blazer_Jackets_on_Rack_%28Unsplash%29.jpg"),
        Map.of("entidad", "Comedor Los Changuitos (Salta)", "bien", "Fideos", "cantidad", "80 paquetes", "fecha", "18/09/2026",
            "latitud", -24.7883, "longitud", -65.4106,
            "foto", "https://upload.wikimedia.org/wikipedia/commons/a/a8/Pates_pas_fraiches.jpg")
    );
  }

  public static Map<String, Object> donante() {
    return Map.of("nombre", "Ana Pérez", "notificacionesSinLeer", 1);
  }

  // Todas las notificaciones juntas, como las devuelve el repositorio: cada controller filtra por destinatario
  public static List<Map<String, Object>> notificaciones() {
    return List.of(
        Map.of("destinatario", "donante", "tipo", "Donación asignada", "fecha", "Hoy 03:00", "leida", false,
            "mensaje", "Tu donación de 6 sillas fue asignada a Escuela Rural N°10.",
            "link", "/donante/donaciones", "linkTexto", "Ver donación"),
        Map.of("destinatario", "donante", "tipo", "Entrega recibida", "fecha", "02/09/2026", "leida", true,
            "mensaje", "Escuela Rural N°10 recibió tus 10 bancos. Camión AB123CD, 02/09/2026 11:40."),
        Map.of("destinatario", "entidad", "tipo", "Donación asignada", "fecha", "Hoy 03:00", "leida", false,
            "mensaje", "Se te asignaron 6 sillas usadas de Arcos Plateados S.A.",
            "link", "/entidad/donaciones", "linkTexto", "Ver donaciones"),
        Map.of("destinatario", "entidad", "tipo", "Recepción confirmada", "fecha", "02/09/2026", "leida", true,
            "mensaje", "Registramos la recepción de 10 bancos. Comprobante: camión AB123CD, 02/09/2026 11:40.")
    );
  }

  public static Map<String, Object> admin() {
    return Map.of("nombre", "admin@donatrack", "notificacionesSinLeer", 0);
  }

  // latitud y longitud ubican a la entidad en el mapa del seguimiento (EntidadBeneficiaria todavía no las tiene)
  public static Map<String, Object> entidad() {
    return Map.of("nombre", "Escuela Rural N°10", "notificacionesSinLeer", 1,
        "direccion", "Ruta 5 km 32, San Andrés de Giles", "latitud", -34.4705, "longitud", -59.4890);
  }

  public static List<Map<String, String>> camiones() {
    return List.of(
        Map.of("id", "1", "patente", "AB123CD", "volumen", "18", "altura", "3.2", "cargaMax", "3500",
            "estado", "EN_RUTA", "estadoTexto", "En ruta"),
        Map.of("id", "2", "patente", "AE456FG", "volumen", "12", "altura", "2.8", "cargaMax", "2000",
            "estado", "DISPONIBLE", "estadoTexto", "Disponible"),
        Map.of("id", "3", "patente", "AC789HI", "volumen", "25", "altura", "3.8", "cargaMax", "6000",
            "estado", "NO_DISPONIBLE", "estadoTexto", "No disponible")
    );
  }

  // Resumen de una importación de donantes por CSV (hoy ImportadorPersonas no devuelve resumen: es solo para la vista)
  public static Map<String, Object> resultadoImportacion() {
    return Map.of(
        "fecha", "Hoy 10:15",
        "leidas", "20.000", "creados", "18.450", "actualizados", "1.520", "conError", "30",
        "errores", List.of("Fila 1.204: falta el correo electrónico", "Fila 7.881: TipoDoc inválido"));
  }
}
