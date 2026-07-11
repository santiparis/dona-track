package logistica.repository;

import logistica.domain.Coordenadas;
import logistica.domain.Donacion;
import logistica.domain.Entrega;

import java.util.ArrayList;
import java.util.List;

public class SeedEntregas {
  public static List<Entrega> entregas() {
    return List.of(
        new Entrega(new ArrayList<>(List.of(new Donacion("1", 1, "unidades"))), new Coordenadas(1, 2)),
        new Entrega(new ArrayList<>(List.of(new Donacion("2", 1, "unidades"))), new Coordenadas(3, 4))
    );
  }
}
