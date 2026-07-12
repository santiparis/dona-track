package logistica.service;

import logistica.client.DonacionesAPICalls;
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

  public Ruta iniciarRuta(String rutaId) throws IOException {
    Ruta ruta = rutasRepository.buscarPorId(rutaId)
        .orElseThrow(() -> new NoSuchElementException("Ruta no encontrada: " + rutaId));

    ruta.iniciar();
    //inicia las request
    donacionesApi.rutaIniciada(ruta).execute();

    return ruta;
  }
}