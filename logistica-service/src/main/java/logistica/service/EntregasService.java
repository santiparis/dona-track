package logistica.service;

import logistica.retrofit_client.DonacionesAPICalls;
import logistica.domain.Donacion;
import logistica.domain.Entrega;
import logistica.domain.Ruta;
import logistica.repository.RutasRepository;

import java.io.IOException;
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

  public Ruta iniciarRuta(Long rutaId) throws IOException {
    Ruta ruta = rutasRepository.buscarPorId(rutaId)
        .orElseThrow(() -> new NoSuchElementException("Ruta no encontrada: " + rutaId));

    ruta.iniciar();
    // notifica a donaciones el inicio de ruta, una donacion a la vez
    for (Entrega entrega : ruta.getEntregas()) {
      notificarEstado(entrega, EN_TRASLADO, null);
    }

    return ruta;
  }

  public Entrega confirmarEntrega(Long entregaId) throws IOException {
    Entrega entrega = rutasRepository.buscarEntregaPorId(entregaId)
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));

    entrega.marcarEntregada();
    // el comprobante debe registrar que camion realizo la entrega
    String patente = rutasRepository.buscarRutaPorEntregaId(entregaId)
        .map(ruta -> ruta.getCamion().getPatente())
        .orElse(null);
    notificarEstado(entrega, ENTREGADA, patente);
    return entrega;
  }

  public Entrega marcarNoRecibida(Long entregaId) throws IOException {
    Entrega entrega = rutasRepository.buscarEntregaPorId(entregaId)
        .orElseThrow(() -> new NoSuchElementException("Entrega no encontrada: " + entregaId));
    entrega.marcarNoRecibida();
    notificarEstado(entrega, ENTREGA_FALLIDA, null);
    return entrega;
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