import java.util.List;
import java.util.stream.Collectors;

public class CompatibilidadSemantica implements EstrategiaAsignacion {

  @Override
  public List<EntidadBeneficiaria> sugerirEntidades(DonacionIndependiente donacion, List<EntidadBeneficiaria> entidades) {
    // 1. Filtrar las entidades que tengan una Necesidad que coincida con la subcategoría de la donación
    // 2. Ordenarlas según qué tan precisa sea la coincidencia (podés darle más peso a necesidades extraordinarias)
    // 3. Limitar a las primeras 10.

    return entidades.stream()
        // .filter(entidad -> entidad.necesita(donacion.getSubcategoria())) // (Ejemplo de lógica)
        // .sorted(...)
        .limit(10)
        .collect(Collectors.toList());
  }
}