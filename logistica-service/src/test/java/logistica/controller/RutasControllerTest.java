package logistica.controller;

import io.javalin.http.Context;
import logistica.domain.Camion;
import logistica.domain.Entrega;
import logistica.domain.Ruta;
import logistica.notificacion.NotificadorEntregas;
import logistica.repository.RutasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RutasControllerTest {

  private RutasRepository rutasRepository;
  private NotificadorEntregas notificadorEntregas;
  private RutasController controller;
  private Context ctx;

  @BeforeEach
  void setUp() {
    rutasRepository = mock(RutasRepository.class);
    notificadorEntregas = mock(NotificadorEntregas.class);
    controller = new RutasController(rutasRepository, notificadorEntregas);
    ctx = mock(Context.class, RETURNS_DEEP_STUBS);
  }

  @Test
  void obtenerRutasDevuelveLaListaDelRepositorio() {
    var rutas = List.of(unaRuta());
    when(rutasRepository.obtenerTodas()).thenReturn(rutas);

    controller.obtenerRutas(ctx);

    verify(ctx).json(rutas);
  }

  @Test
  void obtenerRutaDevuelveLaRutaCuandoExiste() {
    Ruta ruta = unaRuta();
    when(ctx.pathParam("id")).thenReturn("1");
    when(rutasRepository.buscarPorId(1L)).thenReturn(Optional.of(ruta));

    controller.obtenerRuta(ctx);

    verify(ctx).json(ruta);
  }

  @Test
  void obtenerRutaDevuelveNotFoundCuandoNoExiste() {
    when(ctx.pathParam("id")).thenReturn("1");
    when(rutasRepository.buscarPorId(1L)).thenReturn(Optional.empty());

    controller.obtenerRuta(ctx);

    verify(ctx).status(404);
  }

  @Test
  void iniciarRutaDevuelveLaRutaIniciada() {
    Ruta ruta = unaRuta();
    when(ctx.pathParam("id")).thenReturn("1");
    when(notificadorEntregas.iniciarRuta(1L)).thenReturn(ruta);

    controller.iniciarRuta(ctx);

    verify(ctx).json(ruta);
  }

  @Test
  void iniciarRutaDevuelveNotFoundSiLaRutaNoExiste() {
    when(ctx.pathParam("id")).thenReturn("1");
    doThrow(new NoSuchElementException("Ruta no encontrada: 1"))
        .when(notificadorEntregas).iniciarRuta(1L);

    controller.iniciarRuta(ctx);

    verify(ctx).status(404);
  }

  @Test
  void iniciarRutaDevuelveConflictSiLaRutaYaEstaIniciada() {
    when(ctx.pathParam("id")).thenReturn("1");
    doThrow(new IllegalStateException("La ruta ya fue iniciada"))
        .when(notificadorEntregas).iniciarRuta(1L);

    controller.iniciarRuta(ctx);

    verify(ctx).status(409);
  }

  @Test
  void iniciarRutaDevuelveBadRequestSiElIdNoEsNumerico() {
    when(ctx.pathParam("id")).thenReturn("ruta-1");

    controller.iniciarRuta(ctx);

    verify(ctx).status(400);
  }

  private Ruta unaRuta() {
    return new Ruta(
        new Camion("AB123CD", 10, 2, 1000),
        List.of(new Entrega(List.of(), "Calle 123", "Comedor Sol"))
    );
  }
}
