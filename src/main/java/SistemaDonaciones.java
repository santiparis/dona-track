import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SistemaDonaciones {

  private final List<DonacionRecibida> donaciones = new ArrayList<>();
  private final List<DonacionIndependiente> donacionesIndependientes = new ArrayList<>();
  private final List<EntidadBeneficiaria> entidades = new ArrayList<>();
  private final List<Necesidad> necesidadesExtra = new ArrayList<>();
  private final List<Necesidad> necesidadesRecu = new ArrayList<>();




  public List<DonacionIndependiente> segmentarEnIndependientes(DonacionRecibida donacionRecibida){

    List<Bien> bienes = donacionRecibida.getBienes();
    HashMap<String, Integer> mapIndependientes = new HashMap<>();
    for(Bien bien : bienes){
      mapIndependientes.put(bien.getSubcategoria().nombre(), bien.getCantidad());
    }





    return donacionesIndependientes;
  }



  public List<DonacionIndependiente> registrarDonacion(DonacionRecibida donacionRecibida) {
    donaciones.add(donacionRecibida);
    List<DonacionIndependiente> generadas = segmentarEnIndependientes(donacionRecibida);
    donacionesIndependientes.addAll(generadas);
    return generadas;
  }

/*
  private List<DonacionIndependiente> segmentarEnIndependientes(DonacionRecibida donacion) {

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

  public void registrarNecesidadExtra(Necesidad necesidad) {
    necesidadesExtra.add(necesidad);
  }
  public void registrarNecesidadRecu(Necesidad necesidad) {
    necesidadesRecu.add(necesidad);
  }

  public List<DonacionRecibida> getDonaciones() { return donaciones; }
  public List<DonacionIndependiente> getDonacionesIndependientes() { return donacionesIndependientes; }
  public List<EntidadBeneficiaria> getEntidades() { return entidades; }
  public List<Necesidad> getNecesidadesExtra() { return necesidadesExtra; }
  public List<Necesidad> getNecesidadesRecu() { return necesidadesRecu; }

}