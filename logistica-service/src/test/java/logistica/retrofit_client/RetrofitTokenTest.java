package logistica.retrofit_client;

import com.sun.net.httpserver.HttpServer;
import logistica.dto.CambioEstadoDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.*;

class RetrofitTokenTest {
  private HttpServer servidor;
  private RetrofitConfig config;
  private final ConcurrentLinkedQueue<Peticion> peticiones = new ConcurrentLinkedQueue<>();

  @BeforeEach
  void iniciarServidor() throws Exception {
    servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    servidor.createContext("/", exchange -> {
      peticiones.add(new Peticion(exchange.getRequestURI().getPath(),
          exchange.getRequestHeaders().getFirst("X-Token-Interno")));
      exchange.getRequestBody().readAllBytes();
      exchange.sendResponseHeaders(204, -1);
      exchange.close();
    });
    servidor.start();
    String baseUrl = "http://127.0.0.1:" + servidor.getAddress().getPort() + "/";
    config = new RetrofitConfig();
    config.setDonacionesBaseUrl(baseUrl);
    config.setPlanificadorBaseUrl(baseUrl);
  }

  @AfterEach
  void detenerServidor() {
    servidor.stop(0);
  }

  @Test
  void enviaElTokenSoloADonaciones() throws Exception {
    config.setDonacionesTokenInterno("token-de-prueba");
    assertTrue(config.donacionesAPICalls()
        .cambiarEstado(7L, new CambioEstadoDTO("ENTREGADA", "comprobante")).execute().isSuccessful());
    assertTrue(config.planificadorAPICalls()
        .enviarDonacionesAsignadas(new PlanificadorAPICalls.PlanificacionRequest(List.of(), List.of()))
        .execute().isSuccessful());

    assertEquals(new Peticion("/api/donaciones/7", "token-de-prueba"), peticiones.poll());
    assertEquals(new Peticion("/planificador/donaciones", null), peticiones.poll());
  }

  @Test
  void noInventaCredencialesCuandoElTokenNoEstaConfigurado() throws Exception {
    for (String token : new String[]{null, "", "  "}) {
      config.setDonacionesTokenInterno(token);
      config.donacionesAPICalls().cambiarEstado(7L, new CambioEstadoDTO("ENTREGADA", "comprobante")).execute();
      assertEquals(new Peticion("/api/donaciones/7", null), peticiones.poll());
    }
  }

  @Test
  void noReenviaLaCredencialAnteUnaRedireccion() throws Exception {
    servidor.createContext("/api/donaciones/7", exchange -> {
      exchange.getRequestBody().readAllBytes();
      exchange.getResponseHeaders().add("Location", "/otro-destino");
      exchange.sendResponseHeaders(302, -1);
      exchange.close();
    });
    config.setDonacionesTokenInterno("token-de-prueba");
    var respuesta = config.donacionesAPICalls()
        .cambiarEstado(7L, new CambioEstadoDTO("ENTREGADA", "comprobante")).execute();
    assertEquals(302, respuesta.code());
    if (respuesta.errorBody() != null) respuesta.errorBody().close();
    assertTrue(peticiones.isEmpty());
  }

  private record Peticion(String ruta, String token) { }
}
