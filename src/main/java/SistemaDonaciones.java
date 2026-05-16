import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SistemaDonaciones {

  private final List<Donacion> donaciones = new ArrayList<>();
  private final List<DonacionIndependiente> donacionesIndependientes = new ArrayList<>();
  private final List<EntidadBeneficiaria> entidades = new ArrayList<>();
  private final List<Necesidad> necesidades = new ArrayList<>();




  public List<DonacionIndependiente> segmentarEnIndependientes(Donacion donacion){

    List<Bien> bienes = donacion.getBienes();
    HashMap<String, Integer> mapIndependientes = new HashMap<>();
    for(Bien bien : bienes){
      mapIndependientes.put(bien.getSubcategoria().nombre(), bien.getCantidad());
    }





    return donacionesIndependientes;
  }



  public List<DonacionIndependiente> registrarDonacion(Donacion donacion) {
    donaciones.add(donacion);
    List<DonacionIndependiente> generadas = segmentarEnIndependientes(donacion);
    donacionesIndependientes.addAll(generadas);
    return generadas;
  }

/*
  private List<DonacionIndependiente> segmentarEnIndependientes(Donacion donacion) {

    Map<Subcategoria, List<Bien>> agrupados = new HashMap<>();
    for (Bien bien : donacion.getBienes()) {
      agrupados
          .computeIfAbsent(bien.getSubcategoria(), k -> new ArrayList<>())
          .add(bien);
    }

    List<DonacionIndependiente> resultado = new ArrayList<>();
    for (Map.Entry<Subcategoria, List<Bien>> entry : agrupados.entrySet()) {
      int totalCantidad = entry.getValue().stream()
          .mapToInt(Bien::getCantidad)
          .sum();
      String unidad = entry.getValue().get(0).getUnidad();
      resultado.add(new DonacionIndependiente(entry.getKey(), totalCantidad, unidad));
    }
    return resultado;
  }*/

  public void registrarEntidad(EntidadBeneficiaria entidad) {
    entidades.add(entidad);
  }

  public void registrarNecesidad(Necesidad necesidad) {
    necesidades.add(necesidad);
  }

  public List<Donacion> getDonaciones() { return donaciones; }
  public List<DonacionIndependiente> getDonacionesIndependientes() { return donacionesIndependientes; }
  public List<EntidadBeneficiaria> getEntidades() { return entidades; }
  public List<Necesidad> getNecesidades() { return necesidades; }
}