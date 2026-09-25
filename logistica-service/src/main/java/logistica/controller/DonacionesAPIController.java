package logistica.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import io.javalin.http.Context;
import logistica.domain.DonacionEncolada;

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
    List<DonacionEncolada> donaciones =
        ctx.bodyAsClass(new TypeReference<List<DonacionEncolada>>() {}.getType());
    donacionesRepository.agregarTodos(donaciones);
    ctx.status(201).json(donaciones);
  }
}