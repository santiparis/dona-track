import donante.PersonaJuridica;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

public class SistemaDonaciones {

  private HashMap<Subcategoria, List<DonacionIndependiente>> stock_donaciones = new HashMap<>();
  private List<DonacionIndependiente> donaciones_vencidas = new ArrayList<>();
  private List<Asignacion> asignaciones = new ArrayList<>();
  private List<Necesidad> necesidades = new ArrayList<>();
  private List<PersonaJuridica> entidadesBeneficiarias = new ArrayList<>();

  public void actualizarEstadoDelSistema() {
    this.actualizarDonacionesVencidas();
    this.actualizarNecesidades();
  }

  public void cargarDonacion(DonacionEntrante donacionEntrante) {
    for (DonacionIndependiente donacion : donacionEntrante.getDonacionesIndependientes()) {
      Subcategoria subcategoria = donacion.getBien().getSubcategoria();
      stock_donaciones.putIfAbsent(subcategoria, new ArrayList<>());
      stock_donaciones.get(subcategoria).add(donacion);
    }
  }

  public void registrarNecesidad(Necesidad necesidad) {
    this.necesidades.add(necesidad);
  }

  public void registrarEntidadBeneficiaria(PersonaJuridica entidadBeneficiaria) {
    if (!this.entidadesBeneficiarias.contains(entidadBeneficiaria)) {
      this.entidadesBeneficiarias.add(entidadBeneficiaria);
    }
  }

  public List<PersonaJuridica> getEntidadesBeneficiarias() {
    return this.entidadesBeneficiarias;
  }

  public List<Asignacion> getAsignaciones() {
    return this.asignaciones;
  }

  public List<Necesidad> getNecesidades() {
    return this.necesidades;
  }

  public List<DonacionIndependiente> getDonacionesVencidas() {
    return this.donaciones_vencidas;
  }

  public void cambiarEstadoAsignacion(int indice, EstadoAsignacion nuevoEstado) {
    this.cambiarEstadoAsignacion(indice, nuevoEstado, null);
  }

  public void cambiarEstadoAsignacion(int indice, EstadoAsignacion nuevoEstado, String justificacion) {
    Asignacion asignacion = this.asignaciones.get(indice);
    asignacion.setEstado(nuevoEstado, justificacion);
    if (nuevoEstado == EstadoAsignacion.ENTREGADA) {
      this.cerrarNecesidadRecurrente(asignacion.getNecesidad());
    }
  }

  public void actualizarDonacionesVencidas() {
    Date fechaActual = new Date();
    for (Subcategoria subcategoria : this.stock_donaciones.keySet()) {
      if (subcategoria.requiereVencimiento()) {
        Iterator<DonacionIndependiente> iterator = this.stock_donaciones.get(subcategoria).iterator();
        while (iterator.hasNext()) {
          DonacionIndependiente donacion = iterator.next();
          if (donacion.getBien().getVencimiento().before(fechaActual)) {
            this.desasignarDonacionVencida(donacion);
            donacion.setEstado(EstadoDonacionIndependiente.VENCIDA);
            this.donaciones_vencidas.add(donacion);
            iterator.remove();
          }
        }
      }
    }
  }

  private void desasignarDonacionVencida(DonacionIndependiente donacionVencida) {
    for (AsignacionItem<Necesidad, Integer> item : donacionVencida.getAsignaciones()) {
      Necesidad necesidad = item.necesidad();
      int cantidad = item.cantidad();
      Asignacion asignacion = this.encontrarAsignacion(necesidad);
      if (asignacion != null && asignacion.getEstado() != EstadoAsignacion.ENTREGADA) {
        necesidad.restarSuplido(donacionVencida.getBien(), cantidad);
        asignacion.reducirBienDeSubcategoria(donacionVencida.getBien(), cantidad);
        if (asignacion.getBienes().isEmpty()) {
          this.asignaciones.remove(asignacion);
        }
      }
    }
    donacionVencida.getAsignaciones().clear();
  }

  private Asignacion encontrarAsignacion(Necesidad necesidad) {
    for (Asignacion asignacion : this.asignaciones) {
      if (asignacion.getNecesidad() == necesidad) {
        return asignacion;
      }
    }
    return null;
  }

  public void actualizarNecesidades() {
    HashMap<Subcategoria, Integer> cantidadSubcategorias = this.getBienesDisponibles();
    List<Necesidad> nuevasNecesidades = new ArrayList<>();
    Iterator<Necesidad> iterator = this.necesidades.iterator();
    while (iterator.hasNext()) {
      Necesidad necesidad = iterator.next();
      if (necesidad instanceof NecesidadRecurrente necesidadRecurrente && necesidadRecurrente.estaVencida()) {
        necesidad.resolver().ifPresent(nuevasNecesidades::add);
        iterator.remove();
        continue;
      }
      if (this.hayStockParaSuplirParcialmente(necesidad, cantidadSubcategorias)) {
        this.suplirNecesidadParcialmente(necesidad, cantidadSubcategorias);
        if (!necesidad.estaSatisfecha()) {
          continue;
        }
        necesidad.resolver().ifPresent(nuevasNecesidades::add);
        iterator.remove();
      }
    }
    this.necesidades.addAll(nuevasNecesidades);
  }

  private void cerrarNecesidadRecurrente(Necesidad necesidad) {
    if (this.necesidades.remove(necesidad)) {
      necesidad.resolver().ifPresent(this.necesidades::add);
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

  private boolean hayStockParaSuplirParcialmente(Necesidad necesidad, HashMap<Subcategoria, Integer> cantidades) {
    HashMap<Subcategoria, Integer> cantidadesPendientes = necesidad.getCantidadesPendientes();
    for (Subcategoria key : cantidadesPendientes.keySet()) {
      if (cantidades.getOrDefault(key, 0) > 0) {
        return true;
      }
    }
    return false;
  }

  private HashMap<Subcategoria, Integer> suplirNecesidadParcialmente(Necesidad necesidad, HashMap<Subcategoria, Integer> cantidades) {
    Asignacion asignacion = new Asignacion(necesidad, EstadoAsignacion.ASIGNACION_REALIZADA);
    boolean huboAsignacion = false;
    for (Bien bien : necesidad.getBienes()) {
      int cantidadNecesitada = necesidad.getCantidadPendiente(bien);
      if (cantidadNecesitada == 0) {
        continue;
      }
      Subcategoria subcategoria = bien.getSubcategoria();
      int cantidad = this.asignarDonacion(cantidadNecesitada, subcategoria, necesidad);
      if (cantidad > 0) {
        cantidades.put(subcategoria, cantidades.get(subcategoria) - cantidad);
        necesidad.registrarSuplido(bien, cantidad);
        asignacion.agregarBien(bien.conCantidad(cantidad));
        huboAsignacion = true;
      }
    }
    if (huboAsignacion) {
      this.asignaciones.add(asignacion);
    }
    return cantidades;
  }

  private int asignarDonacion(int cantidad, Subcategoria subcategoria, Necesidad necesidad) {
    int suplido = 0;
    while (suplido < cantidad) {
      List<DonacionIndependiente> donaciones = stock_donaciones.get(subcategoria);
      if (donaciones == null) {
        return suplido;
      }
      DonacionIndependiente donacion = donaciones.stream()
              .filter(donacionIndependiente -> donacionIndependiente.getDisponible() > 0)
              .findFirst()
              .orElse(null);
      if (donacion == null) {
        return suplido;
      }
      int cantidadAUsar = Math.min(donacion.getDisponible(), cantidad - suplido);
      donacion.agregarAsignacion(new AsignacionItem<>(necesidad, cantidadAUsar));
      donacion.usar(cantidadAUsar);
      donacion.actualizarEstadoSegunUso(necesidad);
      suplido += cantidadAUsar;
      if(donacion.getDisponible() > 0) {
        break;
      }
    }
    return suplido;
  }
}
