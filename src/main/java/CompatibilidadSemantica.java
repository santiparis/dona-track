import java.util.List;

public class CompatibilidadSemantica implements EstrategiaAsignacion {

  @Override
  public List<EntidadBeneficiaria> sugerirEntidades(DonacionIndependiente donacion, List<EntidadBeneficiaria> entidades) {
    return entidades
            .stream()
            .filter(entidad -> entidad.necesitaSubcategoria(donacion.getBien().getSubcategoria())).limit(10)
            .toList();
  }
}