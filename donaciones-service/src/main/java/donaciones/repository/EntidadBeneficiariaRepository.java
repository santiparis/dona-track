package donaciones.repository;

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
  private final List<EntidadBeneficiaria> entidadesBeneficiarias = new ArrayList<>();
  private static long idSequence = 3L;
  private static long necesidadIdSequence = 3L;

  public EntidadBeneficiariaRepository() {
    if (baseDeDatosSimulada.isEmpty()) {

      EntidadBeneficiaria e1 = new EntidadBeneficiaria("razon1", "direc1", "tel1", new ArrayList<>());
      e1.setId(1L);
      Map<Subcategoria, Integer> cantidadesE1 = new HashMap<>();
      cantidadesE1.put(Subcategoria.BANCOS, 50);

      Necesidad necesidadE1 = new Necesidad(
              "faltante alimentos",
              null,
              cantidadesE1
      );
      necesidadE1.setId(1L);
      e1.getNecesidades().add(necesidadE1);
      baseDeDatosSimulada.add(e1);

      EntidadBeneficiaria e2 = new EntidadBeneficiaria("razon2", "direc2", "tel2", new ArrayList<>());
      e2.setId(2L);
      Map<Subcategoria, Integer> cantidadesE2 = new HashMap<>();
      cantidadesE2.put(Subcategoria.REMERAS, 15);
      cantidadesE2.put(Subcategoria.ARROZ, 40);

      Necesidad necesidadE2 = new Necesidad(
              "invierno",
              null,
              cantidadesE2
      );
      necesidadE2.setId(2L);

      e2.getNecesidades().add(necesidadE2);
      baseDeDatosSimulada.add(e2);
    }
  }

  public List<EntidadBeneficiaria> obtenerTodas() {
    return new ArrayList<>(baseDeDatosSimulada);
  }

  public Optional<EntidadBeneficiaria> buscarPorId(Long id) {
    return baseDeDatosSimulada.stream()
            .filter(entidad -> entidad.getId() != null && entidad.getId().equals(id))
            .findFirst();
  }

  public void eliminarPorId(Long id) {
    baseDeDatosSimulada.removeIf(entidad -> entidad.getId() != null && entidad.getId().equals(id));
  }

  public void guardar(EntidadBeneficiaria entidadBeneficiaria) {
    if (entidadBeneficiaria.getId() == null) {
      entidadBeneficiaria.setId(idSequence++);
    }
    for (Necesidad nec : entidadBeneficiaria.getNecesidades()) {
      if (nec.getId() == null) {
        nec.setId(necesidadIdSequence++);
      }
    }
    if (!baseDeDatosSimulada.contains(entidadBeneficiaria)) {
      baseDeDatosSimulada.add(entidadBeneficiaria);
    }
  }

  public Optional<EntidadBeneficiaria> obtenerPorId(Long idEntidad) {
    return buscarPorId(idEntidad);
  }
}