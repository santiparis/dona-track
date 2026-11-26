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
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

public class ProcesarAsignaciones {

  public static void main(String[] args) {
    long inicio = System.nanoTime();
    System.out.println("==== Inicio de procesamiento de asignaciones: " + OffsetDateTime.now() + " ====");

    String dbUrl = System.getenv("DB_URL");
    if (dbUrl != null && !dbUrl.isBlank()) {
      WithSimplePersistenceUnit.configure(properties -> properties.set("hibernate.connection.url", dbUrl));
    }

    DonacionRepository donacionRepository = new DonacionRepository();
    EntidadBeneficiariaRepository entidadRepository = new EntidadBeneficiariaRepository();
    SugerenciaAsignacionRepository sugerenciaRepository = new SugerenciaAsignacionRepository();
    OrganizadorAsignaciones organizadorAsignaciones = new OrganizadorAsignaciones(
        List.of(new CompatibilidadSemantica(), new PrioridadSubAtendidos())
    );

    // Instanciar el cliente gRPC conectando al puerto de logistica-service (9090)
    LogisticaGrpcClient logisticaGrpcClient = new LogisticaGrpcClient(
        System.getenv().getOrDefault("LOGISTICA_GRPC_HOST", "localhost"), 9090);

    AsignacionesController controller = new AsignacionesController(
        donacionRepository,
        entidadRepository,
        sugerenciaRepository,
        logisticaGrpcClient,
        new Notificador(new NotificacionRepository()),
        organizadorAsignaciones
    );

    TransaccionPorRequest transaccion = new TransaccionPorRequest();
    try {
      transaccion.withTransaction(controller::limpiarSugerencias);
    } finally {
      transaccion.perThreadEntityManagerAccess().dispose();
    }
    var sugerencias = controller.procesarDonacionesEnDepositoEnLotes(25);

    if (sugerencias.isEmpty()) {
      System.out.println("No hay donaciones en depósito para procesar.");
    }

    for (var sugerencia : sugerencias) {
      var bien = sugerencia.getDonacion().getBien();
      for (var ranking : sugerencia.getEntidadesPorAlgoritmo().entrySet()) {
        for (var entidad : ranking.getValue()) {
          System.out.printf("Donación #%d – %s → %s | Algoritmo: %s%n",
              sugerencia.getId(), bien.getSubcategoria().nombre(), entidad.getRazonSocial(), ranking.getKey());
        }
      }
    }

    long duracion = Duration.ofNanos(System.nanoTime() - inicio).toSeconds();
    System.out.println("Total de sugerencias guardadas: " + sugerencias.size());
    System.out.println("==== Fin de procesamiento de asignaciones (" + duracion + " s) ====");
  }
}
