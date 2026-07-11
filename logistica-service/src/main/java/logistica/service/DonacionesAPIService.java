package logistica.service;

import logistica.client.DonacionesAPICalls;
import logistica.domain.Entrega;
import logistica.repository.RepositorioEntregas;

import java.util.Optional;

public class DonacionesAPIService {

  private final RepositorioEntregas repositorioEntregas;
  private final DonacionesAPICalls donacionesApi;

  public DonacionesAPIService(RepositorioEntregas repositorio,
                              DonacionesAPICalls donacionesApi){

    this.repositorioEntregas = repositorio;
    this.donacionesApi = donacionesApi;

  }

  public void agregar(Entrega entrega) {
    repositorioEntregas.agregar(entrega);
  }

  public Optional<Entrega> buscarPorId(String id) {
    return repositorioEntregas.buscarPorId(id);
  }
}
