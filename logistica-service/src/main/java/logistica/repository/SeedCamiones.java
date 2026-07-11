package logistica.repository;

import logistica.domain.Camion;

import java.util.List;

public class SeedCamiones {
  public static List<Camion> camiones() {
    return List.of(
        new Camion("AB123CD", 45.0, 3.2, 12000),
        new Camion("XY987ZW", 30.0, 2.8, 8000)
    );
  }
}
