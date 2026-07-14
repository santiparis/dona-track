package logistica.service;

import logistica.domain.Donacion;
import logistica.repository.DonacionesRepository;

import java.util.List;

public class DonacionesService {

  private final DonacionesRepository donacionesRepository;

  public DonacionesService(DonacionesRepository donacionesRepository) {
    this.donacionesRepository = donacionesRepository;
  }


  public void agregarDonaciones(List<Donacion> donaciones) {
    donacionesRepository.agregarTodos(donaciones);
  }

  public List<Donacion> obtenerPendientes() {
    return donacionesRepository.obtenerTodas();
  }
}