import Donante.Donante;
import java.util.ArrayList;
import java.util.List;

public class DonacionEntrante {
    private final Donante donante;
    private final String descripcion;
    private final List<Bien> bienes;
    private List<DonacionIndependiente> donaciones_independientes;

    public DonacionEntrante(Donante donante, String descripcion, List<Bien> bienes) {

        if (bienes == null || bienes.isEmpty()) {
          throw new IllegalArgumentException("La donación debe tener al menos un bien");
        }
        if (descripcion == null || descripcion.isBlank()) {
          throw new IllegalArgumentException("La donación debe tener una descripción");
        }

        this.donante = donante;
        this.descripcion = descripcion;
        this.bienes = bienes;
        this.donaciones_independientes = segmentarEnIndependientes(bienes);

    }

    List<DonacionIndependiente> segmentarEnIndependientes(List<Bien> bienes){

      this.donaciones_independientes = new ArrayList<>();

      for(Bien bien : bienes){
        DonacionIndependiente donacionIndependiente = new DonacionIndependiente(bien);
        donaciones_independientes.add(donacionIndependiente);
      }
      return donaciones_independientes;
    }

    public Donante getDonante() {
        return this.donante;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public List<Bien> getBienes() {
        return this.bienes;
    }

  public List<DonacionIndependiente> getDonaciones_independientes() {
    return donaciones_independientes;
  }
}
