package logistica.grpc;

import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;
import io.grpc.stub.StreamObserver;
import logistica.db.TransaccionPorRequest;
import logistica.repository.DonacionesRepository;
import logistica.repository.RutasRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LogisticaServiceImplTest implements SimplePersistenceTest {

  @Test
  void recibeLaAsignacionPorGrpcYLaDejaPendienteUnaSolaVez() {
    var donaciones = new DonacionesRepository();
    var servicio = new LogisticaServiceImpl(
        new RutasRepository(), donaciones, null, new TransaccionPorRequest(), null);
    var solicitud = TrazarRutaRequest.newBuilder()
        .setIdDonacion("90123")
        .setIdEntidadDestino("Av. Mitre 300")
        .setCantidadBienes(3)
        .setUnidad("unidades")
        .setEntidadNombre("Comedor Sol")
        .build();
    @SuppressWarnings("unchecked")
    StreamObserver<TrazarRutaResponse> respuesta = mock(StreamObserver.class);

    servicio.trazarRuta(solicitud, respuesta);
    servicio.trazarRuta(solicitud, respuesta);

    var respuestas = forClass(TrazarRutaResponse.class);
    verify(respuesta, times(2)).onNext(respuestas.capture());
    assertTrue(respuestas.getAllValues().stream().allMatch(TrazarRutaResponse::getExito));
    withTransaction(() -> {
      var pendientes = donaciones.obtenerTodas().stream()
          .filter(donacion -> donacion.getDonacionID() == 90123L)
          .toList();
      assertEquals(1, pendientes.size());
      var pendiente = pendientes.get(0);
      assertEquals(3, pendiente.getCantidadBienes());
      assertEquals("unidades", pendiente.getUnidad());
      assertEquals("Av. Mitre 300", pendiente.getDestino());
      assertEquals("Comedor Sol", pendiente.getEntidadNombre());
      donaciones.remover(pendientes);
    });
  }
}
