import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrganizadorAsignaciones {

  private EstrategiaAsignacion estrategiaSemantica;
  private EstrategiaAsignacion estrategiaPrioridad;

  public OrganizadorAsignaciones() {
    this.estrategiaSemantica = new CompatibilidadSemantica();
    this.estrategiaPrioridad = new PrioridadSubAtendidos();
  }

  public SugerenciaAsignacion ejecutarMatchmaking(DonacionIndependiente donacion, List<EntidadBeneficiaria> todasLasEntidades) {

    List<EntidadBeneficiaria> candidatasSemantica = estrategiaSemantica.sugerirEntidades(donacion, todasLasEntidades);
    List<EntidadBeneficiaria> candidatasPrioridad = estrategiaPrioridad.sugerirEntidades(donacion, todasLasEntidades);

    List<EntidadBeneficiaria> coincidencias = candidatasSemantica.stream()
            .filter(candidatasPrioridad::contains)
            .collect(Collectors.toList());

    if (!coincidencias.isEmpty()) {
      return new SugerenciaAsignacion(donacion, coincidencias);
    } else {
      List<EntidadBeneficiaria> ambasEjecuciones = Stream.concat(
                      candidatasSemantica.stream(),
                      candidatasPrioridad.stream()
              )
              .distinct()
              .collect(Collectors.toList());

      return new SugerenciaAsignacion(donacion, ambasEjecuciones);
    }
  }
}