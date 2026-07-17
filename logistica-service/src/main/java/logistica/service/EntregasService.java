package logistica.service;

import logistica.retrofit_client.DonacionesAPICalls;
import logistica.domain.Entrega;
import logistica.domain.Ruta;
import logistica.repository.RutasRepository;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class EntregasService {

  private final RutasRepository rutasRepository;
  private final DonacionesAPICalls donacionesApi;

  public EntregasService(RutasRepository rutasRepository, DonacionesAPICalls donacionesApi) {
    this.rutasRepository = rutasRepository;
    this.donacionesApi = donacionesApi;
  }

  public Optional<Entrega> buscarPorId(String id) {
    return rutasRepository.buscarEntregaPorId(id);
  }

  public Optional<Ruta> buscarRutaPorId(String id) {
    return rutasRepository.buscarPorId(id);
  }

  public List<Ruta> listarRutas() {
    return rutasRepository.obtenerTodas();
  }

  public Ruta iniciarRuta(String rutaId) throws IOException {
    Ruta ruta = rutasRepository.buscarPorId(rutaId)
        .orElseThrow(() -> new NoSuchElementException("Ruta no encontrada: " + rutaId));

    ruta.iniciar();
    donacionesApi.rutaIniciada(ruta).execute();

    return ruta;
  }


  public Entrega confirmarEntrega(String entregaId) {
    Entrega entrega = rutasRepository.buscarEntregaPorId(entregaId)
        .map(entregaEncontrada -> {
          entregaEncontrada.marcarEntregada();
          return entregaEncontrada;
        })
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));
    try {
      donacionesApi.entregaCompletada(entrega).execute();
      return entrega;
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo notificar a donaciones-service", e);
    }
  }

  public Entrega marcarNoRecibida(String entregaId) {
    Entrega entrega = rutasRepository.buscarEntregaPorId(entregaId)
        .map(entregaEncontrada -> {
          entregaEncontrada.marcarNoRecibida();
          return entregaEncontrada;
        })
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));
    try {
      donacionesApi.entregaFallida(entrega).execute();
      return entrega;
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo notificar a donaciones-service", e);
    }
  }

  public Entrega reingresarADeposito(String entregaId) {
    return rutasRepository.buscarEntregaPorId(entregaId)
        .map(entrega -> {
          entrega.reingresarADeposito();
          return entrega;
        })
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));
  }
}
