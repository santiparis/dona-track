package logistica;

import java.io.IOException;
import java.util.NoSuchElementException;
import logistica.domain.Donacion;
import logistica.domain.Entrega;
import logistica.domain.Ruta;
import logistica.repository.RutasRepository;
import logistica.retrofit_client.DonacionesAPICalls;

public class NotificadorEntregas {

  private static final String EN_TRASLADO = "EN_TRASLADO";
  private static final String ENTREGADA = "ENTREGADA";
  private static final String ENTREGA_FALLIDA = "ENTREGA_FALLIDA";

  private final RutasRepository rutasRepository;
  private final DonacionesAPICalls donacionesApi;

  public NotificadorEntregas(RutasRepository rutasRepository, DonacionesAPICalls donacionesApi) {
    this.rutasRepository = rutasRepository;
    this.donacionesApi = donacionesApi;
  }

  public Ruta iniciarRuta(Long rutaId) {
    Ruta ruta = rutasRepository.buscarPorId(rutaId)
        .orElseThrow(() -> new NoSuchElementException("Ruta no encontrada: " + rutaId));

    ruta.iniciar();
    try {
      for (Entrega entrega : ruta.getEntregas()) {
        // Notificamos sin el nombre del camión porque este estado no lo requiere
        notificarEstado(entrega, EN_TRASLADO, null);
      }
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo notificar a donaciones-service", e);
    }

    return ruta;
  }

  public Entrega confirmarEntrega(Long entregaId) {
    Entrega entrega = rutasRepository.buscarEntregaPorId(entregaId)
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));

    entrega.marcarEntregada();

    String nombreCamion = rutasRepository.buscarRutaPorEntregaId(entregaId)
        .map(ruta -> ruta.getCamion().getPatente())
        .orElse(null);

    try {
      notificarEstado(entrega, ENTREGADA, nombreCamion);
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo notificar a donaciones-service", e);
    }

    return entrega;
  }

  public Entrega marcarNoRecibida(Long entregaId) {
    Entrega entrega = rutasRepository.buscarEntregaPorId(entregaId)
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));

    entrega.marcarNoRecibida();

    try {
      notificarEstado(entrega, ENTREGA_FALLIDA, null);
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo notificar a donaciones-service", e);
    }

    return entrega;
  }

  private void notificarEstado(Entrega entrega, String nuevoEstado, String nombreCamion) throws IOException {
    for (Donacion donacion : entrega.getListaDonaciones()) {
      donacionesApi.cambiarEstado(donacion.getDonacionID(), nuevoEstado, nombreCamion).execute();
    }
  }
}
