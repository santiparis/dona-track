import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SistemaDonaciones {

  //pensar como implementar fifo
  private HashMap<Subcategoria, List<DonacionIndependiente>> stock_donaciones = new HashMap<>();
  private List<Asignacion> asignaciones = new ArrayList<>();
  private List<Necesidad> necesidades = new ArrayList<>();

  //incompleto
//  public void donar(Donante donante, DonacionEntrante donacionEntrante) {
//
//    segmentarEnStock(donacionEntrante);
//  }
//
//  public void segmentarEnStock(DonacionEntrante donacionEntrante) {
//
//    List<Bien> bienes = donacionEntrante.getBienes();
//    for (Bien bien : bienes) {
//      //si stock > 0, sumo lo que entra
//      stock_donaciones.put(bien.getSubcategoria().nombre(), stock_donaciones.getOrDefault(bien.getSubcategoria().nombre(), 0) + bien.getCantidad());
//    }
//
//  }

  public void actualizarNecesidades() {
    HashMap<Subcategoria, Integer> cantidadSubcategorias = this.getBienesDisponibles();
    for (Necesidad necesidad : this.necesidades) {
      if (this.sePuedeSuplir(necesidad, cantidadSubcategorias)) {
        this.suplirNecesidad(necesidad, cantidadSubcategorias);
        this.necesidades.remove(necesidad);
      }
    }
  }

  private HashMap<Subcategoria, Integer> getBienesDisponibles() {
    HashMap<Subcategoria, Integer> stock = new HashMap<>();
    for (Subcategoria key : this.stock_donaciones.keySet()) {
      Integer cantidad = (Integer) this.stock_donaciones.get(key)
              .stream()
              .mapToInt(DonacionIndependiente::getDisponible).sum();
      stock.put(key, cantidad);
    }
    return stock;
  }

  private boolean sePuedeSuplir(Necesidad necesidad, HashMap<Subcategoria, Integer> cantidades) {
    HashMap<Subcategoria, Integer> cantidadesNecesitadas = necesidad.getCantidades();
    for (Subcategoria key : cantidadesNecesitadas.keySet()) {
      if (cantidadesNecesitadas.get(key) > cantidades.get(key)) {
        return false;
      }
    }
    return true;
  }

  private HashMap<Subcategoria, Integer> suplirNecesidad(Necesidad necesidad, HashMap<Subcategoria, Integer> cantidades) {
    Asignacion asignacion = new Asignacion(necesidad, EstadoDonacion.ASIGNACION_REALIZADA);
    for (Bien bien : necesidad.getBienes()) {
      int cantidadNecesitada = bien.getCantidad();
      Subcategoria subcategoria = bien.getSubcategoria();
      int cantidad = this.asignarDonacion(cantidadNecesitada, subcategoria, necesidad);
      cantidades.put(subcategoria, Integer.valueOf(cantidadNecesitada - cantidad));
      asignacion.agregarBien(bien);
    }
    this.asignaciones.add(asignacion);
    return cantidades;
  }

  private int asignarDonacion(int cantidad, Subcategoria subcategoria, Necesidad necesidad) {
    int suplido = 0;
    while (suplido < cantidad) {
      DonacionIndependiente donacion = stock_donaciones.get(subcategoria).stream().findFirst().orElseThrow();
      if(donacion.getDisponible() > cantidad - suplido) {
        donacion.usar(Integer.valueOf(cantidad - suplido));
        donacion.agregarAsignacion(new AsignacionItem<>(necesidad, Integer.valueOf(cantidad - suplido)));
        break;
      } else {
        Integer disponible = donacion.getDisponible();
        suplido += disponible;
        donacion.agregarAsignacion(new AsignacionItem<>(necesidad, disponible));
        donacion.usar(disponible);
      }
    }
    return suplido;
  }
}