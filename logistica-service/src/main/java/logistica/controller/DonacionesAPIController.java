package logistica.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import io.javalin.http.Context;
import logistica.domain.Donacion;

import logistica.repository.DonacionesRepository;

import java.util.List;


public class DonacionesAPIController {
  private final DonacionesRepository donacionesRepository;

  public DonacionesAPIController(DonacionesRepository repository) {
    this.donacionesRepository = repository;
  }

  public record ErrorResponse(String mensaje) { }

  public void obtenerDonaciones(Context ctx) {
    //se usa TypeReference para ayudar a Jackson a deserealizar la lista
    List<Donacion> donaciones =
        ctx.bodyAsClass(new TypeReference<List<Donacion>>() {}.getType());
    donacionesRepository.agregarTodos(donaciones);
    ctx.status(201).json(donaciones);
  }
}