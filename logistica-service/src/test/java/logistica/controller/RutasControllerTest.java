package logistica.controller;

import io.javalin.http.Context;
import logistica.domain.Camion;
import logistica.domain.Entrega;
import logistica.domain.Ruta;
import logistica.service.EntregasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RutasControllerTest {

  private EntregasService entregasService;
  private RutasController controller;
  private Context ctx;

  @BeforeEach
  void setUp() {
    entregasService = mock(EntregasService.class);
    controller = new RutasController(entregasService);
    ctx = mock(Context.class, RETURNS_DEEP_STUBS);
  }

  @Test
  void obtenerRutasDevuelveLaListaDelServicio() {
    var ruta = new Ruta(new Camion("AB123CD", 10, 2, 1000), List.of(new Entrega(List.of(), "Calle 123", "Comedor Sol")));
    when(entregasService.listarRutas()).thenReturn(List.of(ruta));

    controller.obtenerRutas(ctx);

    verify(ctx).json(List.of(ruta));
  }
}
