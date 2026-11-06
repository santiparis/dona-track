package logistica.repository;

import logistica.domain.Camion;
import logistica.domain.Coordenadas;
import logistica.domain.DonacionEncolada;
import logistica.domain.Entrega;
import logistica.domain.Ruta;

import java.util.ArrayList;
import java.util.List;

public class SeedCamiones {
  public static List<Camion> camiones() {
    Camion ab123cd = new Camion("AB123CD", 45.0, 3.2, 12000);
    ab123cd.actualizarLocalizacion(new Coordenadas(-34.5862, -59.1250), 62);
    Camion ae456fg = new Camion("AE456FG", 30.0, 2.8, 8000);
    ae456fg.actualizarLocalizacion(new Coordenadas(-34.3489, -58.7956), 0);
    return List.of(
        ab123cd,
        new Camion("XY987ZW", 30.0, 2.8, 8000),
        ae456fg
    );
  }

  // Rutas en curso con las donaciones en traslado del seed de donaciones-service (ids 8 y 9).
  // Los ids coinciden solo si las dos bases arrancan vacías.
  public static List<Ruta> rutas(List<Camion> camiones) {
    Ruta sillas = new Ruta(camiones.get(0), new ArrayList<>(List.of(new Entrega(
        new ArrayList<>(List.of(new DonacionEncolada(8L, 6, "unidades", "-34.4705,-59.4890", "Escuela Rural N°10"))),
        "-34.4705,-59.4890", "Escuela Rural N°10"))));
    Ruta fideos = new Ruta(camiones.get(2), new ArrayList<>(List.of(new Entrega(
        new ArrayList<>(List.of(new DonacionEncolada(9L, 100, "paquetes", "-34.3467,-58.7920", "Comedor Escobar Sonrisas"))),
        "-34.3467,-58.7920", "Comedor Escobar Sonrisas"))));
    sillas.iniciar();
    fideos.iniciar();
    return List.of(sillas, fideos);
  }
}
