package planificacion;

import donaciones.controller.AsignacionesController;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.SugerenciaAsignacionRepository;

public class ProcesarAsignaciones {

  public static void main(String[] args) {
    DonacionRepository donacionRepository = new DonacionRepository();
    EntidadBeneficiariaRepository entidadRepository = new EntidadBeneficiariaRepository();
    SugerenciaAsignacionRepository sugerenciaRepository = new SugerenciaAsignacionRepository();

    AsignacionesController controller = new AsignacionesController(
        donacionRepository,
        entidadRepository,
        sugerenciaRepository
    );

    controller.limpiarSugerencias(); // TODO: Cuando tengamos persistencia esto no conviene, tenemos que revisar y actualizar las sugerencias que ya existan para no saturar la DB.
    var sugerencias = controller.procesarDonacionesEnDeposito();

    System.out.println("==== Inicio de procesamiento de asignaciones ====");
    if (sugerencias.isEmpty()) {
      System.out.println("No hay donaciones en depósito para procesar.");
      return;
    }

    System.out.println("Total de sugerencias guardadas: " + controller.obtenerSugerenciasGuardadas().size());
    System.out.println("==== Fin de procesamiento de asignaciones ====");
  }
}
