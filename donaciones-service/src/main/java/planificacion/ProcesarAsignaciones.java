package planificacion;

import donaciones.controller.AsignacionesController;
import donaciones.controller.Notificador;
import donaciones.db.TransaccionPorRequest;
import donaciones.domain.algoritmos.CompatibilidadSemantica;
import donaciones.domain.algoritmos.OrganizadorAsignaciones;
import donaciones.domain.algoritmos.PrioridadSubAtendidos;
import donaciones.grpc.LogisticaGrpcClient;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.NotificacionRepository;
import donaciones.repository.SugerenciaAsignacionRepository;

import java.util.List;

public class ProcesarAsignaciones {

  public static void main(String[] args) {
    DonacionRepository donacionRepository = new DonacionRepository();
    EntidadBeneficiariaRepository entidadRepository = new EntidadBeneficiariaRepository();
    SugerenciaAsignacionRepository sugerenciaRepository = new SugerenciaAsignacionRepository();
    OrganizadorAsignaciones organizadorAsignaciones = new OrganizadorAsignaciones(
        List.of(new CompatibilidadSemantica(), new PrioridadSubAtendidos())
    );

    // Instanciar el cliente gRPC conectando al puerto de logistica-service (9090)
    LogisticaGrpcClient logisticaGrpcClient = new LogisticaGrpcClient("localhost", 9090);

    AsignacionesController controller = new AsignacionesController(
        donacionRepository,
        entidadRepository,
        sugerenciaRepository,
        logisticaGrpcClient,
        new Notificador(new NotificacionRepository()),
        organizadorAsignaciones
    );

    new TransaccionPorRequest().withTransaction(() -> {
      controller.limpiarSugerencias(); // TODO: revisar la actualización de sugerencias existentes para no saturar la DB.
      var sugerencias = controller.procesarDonacionesEnDeposito();

      System.out.println("==== Inicio de procesamiento de asignaciones ====");
      if (sugerencias.isEmpty()) {
        System.out.println("No hay donaciones en depósito para procesar.");
        return;
      }

      System.out.println("Total de sugerencias guardadas: " + controller.obtenerSugerenciasGuardadas().size());
      System.out.println("==== Fin de procesamiento de asignaciones ====");
    });
  }
}
