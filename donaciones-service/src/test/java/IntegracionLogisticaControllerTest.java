import donaciones.controller.IntegracionLogisticaController;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class IntegracionLogisticaControllerTest {

  private IntegracionLogisticaController controller;
  private Context ctx;

  @BeforeEach
  void setUp() {
    controller = new IntegracionLogisticaController();
    ctx = mock(Context.class, RETURNS_DEEP_STUBS);
  }

  @Test
  void rutasIniciadasRespondeOk() {
    controller.rutasIniciadas(ctx);

    verify(ctx).status(200);
  }

  @Test
  void entregaCompletadaRespondeOk() {
    controller.entregaCompletada(ctx);

    verify(ctx).status(200);
  }

  @Test
  void entregaFallidaRespondeOk() {
    controller.entregaFallida(ctx);

    verify(ctx).status(200);
  }
}
