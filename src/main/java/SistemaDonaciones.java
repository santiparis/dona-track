import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SistemaDonaciones {

  //pensar como implementar fifo
  private final HashMap<String, Integer> stock_donaciones = new HashMap<>();

  private final List<DonacionEntrante> donaciones = new ArrayList<>();
  private final List<Necesidad> necesidadesExtra = new ArrayList<>();
  private final List<Necesidad> necesidadesRecu = new ArrayList<>();

  //incompleto
  public void donar(Donante donante, DonacionEntrante donacionEntrante){

    segmentarEnStock(donacionEntrante);
  }

  public void segmentarEnStock(DonacionEntrante donacionEntrante){

    List<Bien> bienes = donacionEntrante.getBienes();
    for(Bien bien : bienes){
      //si stock > 0, sumo lo que entra
      stock_donaciones.put(bien.getSubcategoria().nombre(), stock_donaciones.getOrDefault(bien.getSubcategoria().nombre(), 0) + bien.getCantidad());
    }

  }


  public void registrarNecesidadExtra(Necesidad necesidad) {
    necesidadesExtra.add(necesidad);
  }
  public void registrarNecesidadRecu(Necesidad necesidad) {
    necesidadesRecu.add(necesidad);
  }

  public List<DonacionEntrante> getDonaciones() { return donaciones; }
  public List<Necesidad> getNecesidadesExtra() { return necesidadesExtra; }
  public List<Necesidad> getNecesidadesRecu() { return necesidadesRecu; }

}