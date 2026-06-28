import java.util.List;
import java.util.stream.Collectors;

public class CompatibilidadSemantica implements EstrategiaAsignacion {

  @Override
  public List<EntidadBeneficiaria> sugerirEntidades(DonacionIndependiente donacion, List<EntidadBeneficiaria> entidades) {
    return entidades.stream().filter(entidad -> entidad.necesitaSubcategoria(donacion.getSubcategoria())).limit(10).collect(Collectors.toList());
  }
}