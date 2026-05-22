import donante.Persona;

import java.util.ArrayList;
import java.util.List;

public class DonacionEntrante {
    private final Persona persona;
    private final String descripcion;
    private final List<Bien> bienes;
    private List<DonacionIndependiente> donaciones_independientes;

    public DonacionEntrante(Persona persona, String descripcion, List<Bien> bienes) {

        if (bienes == null || bienes.isEmpty()) {
          throw new IllegalArgumentException("La donación debe tener al menos un bien");
        }
        if (descripcion == null || descripcion.isBlank()) {
          throw new IllegalArgumentException("La donación debe tener una descripción");
        }

        this.persona = persona;
        this.descripcion = descripcion;
        this.bienes = bienes;
        this.donaciones_independientes = segmentarEnIndependientes(bienes);

    }

    List<DonacionIndependiente> segmentarEnIndependientes(List<Bien> bienes){
        List<Bien> bienesSegmentados = new ArrayList<>();

        for(Bien bien : bienes){
            Bien bienExistente = bienesSegmentados.stream()
                    .filter(bienSegmentado -> bienSegmentado.comparteSegmentoCon(bien))
                    .findFirst()
                    .orElse(null);

            if (bienExistente == null) {
                bienesSegmentados.add(bien);
            } else {
                bienesSegmentados.remove(bienExistente);
                bienesSegmentados.add(bienExistente.conCantidad(bienExistente.getCantidad() + bien.getCantidad()));
            }
        }

        this.donaciones_independientes = new ArrayList<>();
        for(Bien bien : bienesSegmentados){
            DonacionIndependiente donacionIndependiente = new DonacionIndependiente(bien);
            donaciones_independientes.add(donacionIndependiente);
        }
        return donaciones_independientes;
    }

    public Persona getDonante() {
        return this.persona;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public List<Bien> getBienes() {
        return this.bienes;
    }

  public List<DonacionIndependiente> getDonacionesIndependientes() {
    return donaciones_independientes;
  }
}
