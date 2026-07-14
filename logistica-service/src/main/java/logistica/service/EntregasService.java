package logistica.service;

import logistica.retrofit_client.DonacionesAPICalls;
import logistica.domain.Entrega;
import logistica.domain.Ruta;
import logistica.repository.RutasRepository;

import java.io.IOException;
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

  public Ruta iniciarRuta(String rutaId) throws IOException {
    Ruta ruta = rutasRepository.buscarPorId(rutaId)
        .orElseThrow(() -> new NoSuchElementException("Ruta no encontrada: " + rutaId));

    ruta.iniciar();
    //inicia las request
    donacionesApi.rutaIniciada(ruta).execute();

    return ruta;
  }

<<<<<<< Updated upstream
  public Entrega confirmarEntrega(String entregaId) {
    return rutasRepository.buscarEntregaPorId(entregaId)
        .map(entrega -> {
          entrega.marcarEntregada();
          return entrega;
        })
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));
  }

  public Entrega marcarNoRecibida(String entregaId) {
    return rutasRepository.buscarEntregaPorId(entregaId)
        .map(entrega -> {
          entrega.marcarNoRecibida();
          return entrega;
        })
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));
  }

  public Entrega reingresarADeposito(String entregaId) {
    return rutasRepository.buscarEntregaPorId(entregaId)
        .map(entrega -> {
          entrega.reingresarADeposito();
          return entrega;
        })
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));
=======

    // buscar la entrega, entrega.marcarEntregada(), avisar a donacionesApi.entregaCompletada(entrega)
    public Entrega confirmarEntrega(String entregaId) throws IOException {
      Entrega entrega = rutasRepository.buscarEntregaPorId(entregaId)
          .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));

      entrega.marcarEntregada();
      donacionesApi.entregaCompletada(entrega).execute();
      return entrega;
    }

  public Entrega marcarNoRecibida(String entregaId) throws IOException {
    Entrega entrega = rutasRepository.buscarEntregaPorId(entregaId)
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));
    entrega.marcarNoRecibida();
    donacionesApi.entregaFallida(entrega).execute();
    return entrega;
  }

  public Entrega reingresarADeposito(String entregaId) {
    Entrega entrega = rutasRepository.buscarEntregaPorId(entregaId)
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));

    entrega.reingresarADeposito();
    return entrega;
>>>>>>> Stashed changes
  }
}