package logistica.service;

import logistica.client.DonacionesAPICalls;
import logistica.domain.RepositorioEntregas;

public class DonacionesAPIService {

  RepositorioEntregas repositorioEntregas;
  DonacionesAPICalls donacionesApi;

  public DonacionesAPIService(RepositorioEntregas repositorio,
                              DonacionesAPICalls donacionesApi){

    this.repositorioEntregas = repositorio;
    this.donacionesApi = donacionesApi;

  }
}
