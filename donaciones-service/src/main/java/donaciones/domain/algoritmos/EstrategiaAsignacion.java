package donaciones.domain.algoritmos;

import donaciones.domain.Donacion;
import donaciones.domain.EntidadBeneficiaria;

import java.util.List;

public interface EstrategiaAsignacion {
  List<EntidadBeneficiaria> sugerirEntidades(
      Donacion donacion,
      List<EntidadBeneficiaria> entidades
  );
}