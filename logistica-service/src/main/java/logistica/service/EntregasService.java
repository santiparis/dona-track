package logistica.service;

import logistica.retrofit_client.DonacionesAPICalls;
import logistica.domain.Donacion;
import logistica.domain.Entrega;
import logistica.domain.Ruta;
import logistica.repository.RutasRepository;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class EntregasService {

  // nombres de estado que entiende donaciones-service (EstadoDonacionIndependiente)
  private static final String EN_TRASLADO = "EN_TRASLADO";
  private static final String ENTREGADA = "ENTREGADA";
  private static final String ENTREGA_FALLIDA = "ENTREGA_FALLIDA";

  private final RutasRepository rutasRepository;
  private final DonacionesAPICalls donacionesApi;

  public EntregasService(RutasRepository rutasRepository, DonacionesAPICalls donacionesApi) {
    this.rutasRepository = rutasRepository;
    this.donacionesApi = donacionesApi;
  }

  public Optional<Entrega> buscarPorId(Long id) {
    return rutasRepository.buscarEntregaPorId(id);
  }

  public Optional<Ruta> buscarRutaPorId(Long id) {
    return rutasRepository.buscarPorId(id);
  }

  public List<Ruta> listarRutas() {
    return rutasRepository.obtenerTodas();
  }

  public Ruta iniciarRuta(Long rutaId) throws IOException {
    Ruta ruta = rutasRepository.buscarPorId(rutaId)
        .orElseThrow(() -> new NoSuchElementException("Ruta no encontrada: " + rutaId));

    ruta.iniciar();
    for (Entrega entrega : ruta.getEntregas()) {
      notificarEstado(entrega, EN_TRASLADO, null);
    }

    return ruta;
  }


  public Entrega confirmarEntrega(Long entregaId) {
    Entrega entrega = rutasRepository.buscarEntregaPorId(entregaId)
        .map(entregaEncontrada -> {
          entregaEncontrada.marcarEntregada();
          return entregaEncontrada;
        })
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));
    try {
      String nombreCamion = rutasRepository.buscarRutaPorEntregaId(entregaId)
          .map(ruta -> ruta.getCamion().getPatente())
          .orElse(null);
      notificarEstado(entrega, ENTREGADA, nombreCamion);
      return entrega;
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo notificar a donaciones-service", e);
    }
  }

  public Entrega marcarNoRecibida(Long entregaId) {
    Entrega entrega = rutasRepository.buscarEntregaPorId(entregaId)
        .map(entregaEncontrada -> {
          entregaEncontrada.marcarNoRecibida();
          return entregaEncontrada;
        })
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));
    try {
      notificarEstado(entrega, ENTREGA_FALLIDA, null);
      return entrega;
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo notificar a donaciones-service", e);
    }
  }

  public Entrega reingresarADeposito(Long entregaId) {
    Entrega entrega = rutasRepository.buscarEntregaPorId(entregaId)
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));

    entrega.reingresarADeposito();
    return entrega;
  }

  // donaciones cambia el estado por donacion; una entrega puede agrupar varias
  private void notificarEstado(Entrega entrega, String nuevoEstado, String nombreCamion) throws IOException {
    for (Donacion donacion : entrega.getListaDonaciones()) {
      donacionesApi.cambiarEstado(donacion.getDonacionID(), nuevoEstado, nombreCamion).execute();
    }
  }
}
