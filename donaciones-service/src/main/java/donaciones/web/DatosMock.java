package donaciones.web;

import donaciones.domain.Donacion;
import donaciones.domain.RegistroCambioEstado;
import donaciones.domain.Subcategoria;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DatosMock {

  // Las entregas reales, de la más nueva a la más vieja, con lo que la base todavía no tiene:
  // latitud y longitud de la entidad que recibió la entrega (ahí va el marcador del mapa; EntidadBeneficiaria no las tiene)
  // y foto, una URL como Bien.foto (las del mock son fotos libres de Wikimedia Commons, una por subcategoría)
  public static List<Map<String, Object>> donacionesEntregadas(List<Donacion> entregadas) {
    String commons = "https://thumb.wikimedia.org/wikipedia/commons/thumb/";
    Map<String, List<Double>> coordenadas = Map.of(
        "Escuela Rural N°10", List.of(-34.4705, -59.4890),
        "Comedor Escobar Sonrisas", List.of(-34.3467, -58.7920),
        "Espacio de Tutorías Barrio Norte", List.of(-34.9214, -57.9545),
        "Comedor Barrio Sur", List.of(-34.6624, -58.3650),
        "Hogar San José", List.of(-34.5703, -59.1050),
        "Merendero Los Pibes", List.of(-34.8069, -58.2760),
        "Merendero Mitaí", List.of(-27.3671, -55.8961),
        "Hogar Escuela Bariloche", List.of(-41.1335, -71.3103),
        "Comedor Los Changuitos", List.of(-24.7883, -65.4106));
    Map<Subcategoria, String> fotos = Map.of(
        Subcategoria.BANCOS, commons + "e/e9/Classroom_-_Basic_Desks_-_Loxton_Historical_Village_%282020-10-22_09.30.20_by_Michael_Coghlan%29.jpg/960px-Classroom_-_Basic_Desks_-_Loxton_Historical_Village_%282020-10-22_09.30.20_by_Michael_Coghlan%29.jpg",
        Subcategoria.SILLAS, "https://upload.wikimedia.org/wikipedia/commons/5/51/Wooden_Chair.jpg",
        Subcategoria.FIDEOS, commons + "2/26/Pasta_products_from_the_Albert_Heijn%2C_Winschoten_%282018%29.jpg/960px-Pasta_products_from_the_Albert_Heijn%2C_Winschoten_%282018%29.jpg",
        Subcategoria.LECHE, commons + "2/2d/Cartons_of_Nature%27s_Promise_milk_%2850855058622%29.jpg/960px-Cartons_of_Nature%27s_Promise_milk_%2850855058622%29.jpg",
        Subcategoria.CAMPERAS, commons + "e/e9/Blazer_Jackets_on_Rack_%28Unsplash%29.jpg/960px-Blazer_Jackets_on_Rack_%28Unsplash%29.jpg");
    return entregadas.stream()
        .sorted(Comparator.comparing(Donacion::getId).reversed())
        .map(donacion -> {
          // Una entidad que no está en la lista (ej: registrada después) cae en Buenos Aires
          List<Double> ubicacion = coordenadas.getOrDefault(donacion.getEntidadBeneficiaria().getRazonSocial(), List.of(-34.60, -58.38));
          List<RegistroCambioEstado> historial = donacion.getHistorialEstados();
          return Map.<String, Object>of(
              "entidad", donacion.getEntidadBeneficiaria().getRazonSocial(),
              "bien", donacion.getBien().getSubcategoria().nombre(),
              "cantidad", donacion.getBien().getCantidad() + " " + donacion.getBien().getUnidad(),
              "fecha", historial.get(historial.size() - 1).fecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
              "latitud", ubicacion.get(0),
              "longitud", ubicacion.get(1),
              "foto", fotos.getOrDefault(donacion.getBien().getSubcategoria(), ""));
        })
        .toList();
  }

  // latitud y longitud ubican a la entidad en el mapa del seguimiento (EntidadBeneficiaria todavía no las tiene)
  public static Map<String, Object> entidad() {
    return Map.of("nombre", "Escuela Rural N°10",
        "direccion", "Ruta 5 km 32, San Andrés de Giles", "latitud", -34.4705, "longitud", -59.4890);
  }
}
