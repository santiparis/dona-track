package planificacion;

import donaciones.controller.AsignacionesController;
import donaciones.controller.Notificador;
import donaciones.db.TransaccionPorRequest;
import donaciones.domain.algoritmos.CompatibilidadSemantica;
import donaciones.domain.algoritmos.OrganizadorAsignaciones;
import donaciones.domain.algoritmos.PrioridadSubAtendidos;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.SugerenciaAsignacionRepository;
import donaciones.repository.NotificacionRepository;
import donaciones.retrofit_client.RetrofitConfig;

import java.util.List;

public class ProcesarAsignaciones {

  public static void main(String[] args) {
    DonacionRepository donacionRepository = new DonacionRepository();
    EntidadBeneficiariaRepository entidadRepository = new EntidadBeneficiariaRepository();
    SugerenciaAsignacionRepository sugerenciaRepository = new SugerenciaAsignacionRepository();
    OrganizadorAsignaciones organizadorAsignaciones = new OrganizadorAsignaciones(
        List.of(new CompatibilidadSemantica(), new PrioridadSubAtendidos())
    );

    AsignacionesController controller = new AsignacionesController(
        donacionRepository,
        entidadRepository,
        sugerenciaRepository,
        new RetrofitConfig().logisticaAPICalls(),
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

    System.out.println("==== Inicio de procesamiento de asignaciones ====");
    if (sugerencias.isEmpty()) {
      System.out.println("No hay donaciones en depósito para procesar.");
      return;
    }

    System.out.println("Total de sugerencias guardadas: " + sugerencias.size());
    System.out.println("==== Fin de procesamiento de asignaciones ====");
  }
}
