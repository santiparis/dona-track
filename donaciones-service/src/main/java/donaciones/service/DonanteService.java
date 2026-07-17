package donaciones.service;

import donaciones.domain.donante.Persona;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.PersonaJuridica;
import donaciones.dto.DonanteRequestDTO;
import donaciones.repository.DonanteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DonanteService {
  private final DonanteRepository repository;

  public DonanteService(DonanteRepository repository) {
    this.repository = repository;
  }

  public void crearDonante(DonanteRequestDTO dto) {
    Persona nuevaPersona;

    if (dto.tipo().equalsIgnoreCase("HUMANA")) {
      nuevaPersona = new PersonaHumana(
              dto.nombre(), dto.apellido(), dto.edad(), null, dto.documento(),
              null, dto.direccion(), new ArrayList<>(), null, null
      );
    } else if (dto.tipo().equalsIgnoreCase("JURIDICA")) {
      nuevaPersona = new PersonaJuridica(
              null, dto.documento(), dto.nombre(), null, dto.rubro(),
              new ArrayList<>(), new ArrayList<>(), null, null
      );
    } else {
      throw new IllegalArgumentException("Tipo no compatible");
    }

    repository.guardar(nuevaPersona);
  }

  public List<Persona> listarDonantes() {
    return repository.obtenerTodos();
  }

  public void actualizarDonante(Long id, DonanteRequestDTO dto) {
    Optional<Persona> donanteOpt = repository.buscarPorId(id);

    if (donanteOpt.isPresent()) {
      Persona donanteExistente = donanteOpt.get();
      Persona datosNuevos;
      if (dto.tipo().equalsIgnoreCase("HUMANA")) {
        datosNuevos = new PersonaHumana(
                dto.nombre(), dto.apellido(), dto.edad(), null, dto.documento(),
                null, dto.direccion(), new ArrayList<>(), null, null
        );
      } else {
        datosNuevos = new PersonaJuridica(
                null, dto.documento(), dto.nombre(), null, dto.rubro(),
                new ArrayList<>(), new ArrayList<>(), null, null
        );
      }
      donanteExistente.actualizarseDesde(datosNuevos);

    } else {
      throw new IllegalArgumentException("No se encontró un donante ");
    }
  }

  public void eliminarDonante(Long id) {
    repository.borrarPorId(id);
  }
}
