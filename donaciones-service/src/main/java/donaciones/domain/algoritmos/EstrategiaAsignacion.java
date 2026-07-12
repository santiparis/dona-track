package donaciones.domain.algoritmos;

import donaciones.domain.DonacionIndependiente;
import donaciones.domain.EntidadBeneficiaria;

import java.util.List;

public interface EstrategiaAsignacion {
  List<EntidadBeneficiaria> sugerirEntidades(
      DonacionIndependiente donacion,
      List<EntidadBeneficiaria> entidades
  );
}