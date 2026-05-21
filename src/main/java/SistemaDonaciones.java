import Donante.Donante;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SistemaDonaciones {

  //pensar como implementar fifo
  private HashMap<Subcategoria, List<DonacionIndependiente>> stock_donaciones = new HashMap<>();
  private List<Asignacion> asignaciones = new ArrayList<>();
  private List<Necesidad> necesidades = new ArrayList<>();



  public void ingresarDonacion(DonacionEntrante donacion){
    List<DonacionIndependiente> lista_ind = donacion.getDonacionesIndependientes();
    for(DonacionIndependiente donacion_ind : lista_ind){
      Subcategoria subcategoria_bien_donado = donacion_ind.getBien().getSubcategoria();
      if(stock_donaciones.get(subcategoria_bien_donado) == null){
        stock_donaciones.put(subcategoria_bien_donado, new ArrayList<>());
      }
      stock_donaciones.get(subcategoria_bien_donado).add(donacion_ind);

    }
  }

  public void actualizarNecesidades() {
    HashMap<Subcategoria, Integer> cantidadSubcategorias = this.getBienesDisponibles();

    List<Necesidad> cubiertas = new ArrayList<>();

    for (Necesidad necesidad : this.necesidades) {
      if (this.sePuedeSuplir(necesidad, cantidadSubcategorias)) {
        this.suplirNecesidad(necesidad, cantidadSubcategorias);
        cubiertas.add(necesidad);
      }
    }
    this.necesidades.removeAll(cubiertas);
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
      Integer disponible = cantidades.getOrDefault(key, 0);
      if (cantidadesNecesitadas.get(key) > disponible) {
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

  public void agregarNecesidad(Necesidad necesidad) {
    this.necesidades.add(necesidad);
  }

  public List<Necesidad> getNecesidades() {
    return this.necesidades;
  }
}