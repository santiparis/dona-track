package logistica.repository;

import logistica.domain.Coordenadas;
import logistica.domain.Entrega;

import java.util.List;

public class SeedEntregas {
  public static List<Entrega> entregas() {
    return List.of(
        new Entrega("1",new Coordenadas(1,2)),
        new Entrega("2",new Coordenadas(3,4))

    );
  }
}
