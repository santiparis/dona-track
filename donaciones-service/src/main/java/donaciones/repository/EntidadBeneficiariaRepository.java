package donaciones.repository;

import donaciones.domain.Categoria;
import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.Necesidad;
import donaciones.domain.Subcategoria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntidadBeneficiariaRepository {

  private static final List<EntidadBeneficiaria> baseDeDatosSimulada = new ArrayList<>();

  public EntidadBeneficiariaRepository() {
    if (baseDeDatosSimulada.isEmpty()) {

      EntidadBeneficiaria e1 = new EntidadBeneficiaria(1L, "razon1", "direc1", "tel1", new ArrayList<>());
      Map<Subcategoria, Integer> cantidadesE1 = new HashMap<>();
      cantidadesE1.put(new Subcategoria(Categoria.MUEBLES, true, false, "Bancos"), 50);

      Necesidad necesidadE1 = new Necesidad(
              "faltante alimentos",
              null,
              cantidadesE1
      );
      e1.getNecesidades().add(necesidadE1);
      baseDeDatosSimulada.add(e1);

      EntidadBeneficiaria e2 = new EntidadBeneficiaria(2L, "razon2", "direc2", "tel2", new ArrayList<>());
      Map<Subcategoria, Integer> cantidadesE2 = new HashMap<>();
      cantidadesE2.put(new Subcategoria(Categoria.ROPA, true, false, "Remeras"), 15);
      cantidadesE2.put(new Subcategoria(Categoria.ALIMENTOS, false, false, "Arroz"), 40);

      Necesidad necesidadE2 = new Necesidad(
              "invierno",
              null,
              cantidadesE2
      );

      e2.getNecesidades().add(necesidadE2);
      baseDeDatosSimulada.add(e2);
    }
  }

  public List<EntidadBeneficiaria> obtenerTodas() {
    return baseDeDatosSimulada;
  }

  public Optional<EntidadBeneficiaria> obtenerPorId(Long idEntidad) {
    return baseDeDatosSimulada.stream()
            .filter(entidad -> entidad.getId().equals(idEntidad))
            .findFirst();
  }
}