package logistica.notificacion;

import logistica.domain.DonacionEncolada;
import logistica.domain.Entrega;
import logistica.dto.CambioEstadoDTO;
import logistica.retrofit_client.DonacionesAPICalls;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificadorEntregasTest {
  private final DonacionesAPICalls api = mock(DonacionesAPICalls.class);
  private final Call<Void> llamada = mock(Call.class);
  private final NotificadorEntregas notificador = new NotificadorEntregas(api);
  private final Entrega entrega = new Entrega(List.of(
      new DonacionEncolada(7L, 2, "unidades", "Calle 1", "Comedor")), "Calle 1", "Comedor");

  @BeforeEach
  void prepararLlamada() {
    when(api.cambiarEstado(eq(7L), any())).thenReturn(llamada);
  }

  @Test
  void informaElEstadoYElComprobanteSiLaApiAcepta() throws Exception {
    when(llamada.execute()).thenReturn(Response.success(null));
    assertDoesNotThrow(() -> notificador.avisarEntregada(entrega, "comprobante"));
    verify(api).cambiarEstado(7L, new CambioEstadoDTO("ENTREGADA", "comprobante"));
  }

  @Test
  void noTrataErroresHttpComoNotificacionesExitosas() throws Exception {
    for (int estado : new int[]{401, 403, 500}) {
      when(llamada.execute()).thenReturn(Response.error(estado, ResponseBody.create(null, "rechazado")));
      IllegalStateException error = assertThrows(IllegalStateException.class,
          () -> notificador.avisarEntregada(entrega, "comprobante"));
      assertTrue(error.getMessage().contains("HTTP " + estado));
    }
  }

  @Test
  void conservaLaCausaSiFallaLaConexion() throws Exception {
    IOException causa = new IOException("conexión interrumpida");
    when(llamada.execute()).thenThrow(causa);
    IllegalStateException error = assertThrows(IllegalStateException.class,
        () -> notificador.avisarFallida(entrega));
    assertSame(causa, error.getCause());
  }
}
