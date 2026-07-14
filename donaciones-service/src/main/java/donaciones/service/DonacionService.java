package donaciones.service;

import donaciones.domain.*;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.dto.BienDTO;
import donaciones.dto.DonacionPatchDTO;
import donaciones.dto.DonacionRequestDTO;
import donaciones.repository.DonacionRepository;
import donaciones.service.excepcion.CategoriaInvalidaException;
import donaciones.service.excepcion.DonanteNoEncontradoException;
import donaciones.service.excepcion.EstadoBienInvalidoException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DonacionService {
  private final DonacionRepository donacionesRepository;
  private final RepositorioPersonas personasRepository;

  public DonacionService(DonacionRepository donacionesRepository, RepositorioPersonas personasRepository) {
    this.donacionesRepository = donacionesRepository;
    this.personasRepository = personasRepository;
  }

  public void crearDonacion(DonacionRequestDTO dto) {
    List<Bien> bienesDelDominio = this.crearListaBienes(dto.bienes());

    for (Bien bien : bienesDelDominio) {
      Optional<Persona> persona = this.personasRepository.buscarPorDocumento(dto.documentoDonante());

      if (persona.isPresent()) {
        this.donacionesRepository.guardar(new Donacion(persona.get(), bien));
      } else {
        throw new DonanteNoEncontradoException("No se encontró el donante con documento: " + dto.documentoDonante());
      }
    }
  }

  public List<Bien> crearListaBienes(List<BienDTO> bienesDto) {
    return bienesDto.stream()
        .map(bienDto -> {
          Subcategoria subcat = this.parsearSubcategoria(bienDto.nombreSubcategoria());
          EstadoBien estado;

          if (bienDto.estado() != null) {
            estado = this.parsearEstado(bienDto.estado());
          } else {
            estado = null;
          }

          LocalDate vencimiento;
          if (bienDto.vencimiento() != null) {
            vencimiento = LocalDate.parse(bienDto.vencimiento());
          } else {
            vencimiento = null;
          }

          return new Bien(
              subcat,
              bienDto.cantidad(),
              bienDto.unidad(),
              bienDto.descripcion(),
              bienDto.foto(),
              estado,
              vencimiento
          );
        })
        .toList();
  }

  public Subcategoria parsearSubcategoria(String subcategoria) {
    String valor = subcategoria == null ? "" : subcategoria.trim().toUpperCase();

    if (valor.isEmpty()) {
      throw new CategoriaInvalidaException("Categoría inválida: " + subcategoria);
    }

    try {
      return Subcategoria.valueOf(valor);
    } catch (IllegalArgumentException e) {
      try {
        Categoria categoria = Categoria.valueOf(valor);
        return Arrays.stream(Subcategoria.values())
            .filter(sub -> sub.getCategoria() == categoria)
            .findFirst()
            .orElseThrow(() -> new CategoriaInvalidaException("Categoría inválida: " + subcategoria));
      } catch (IllegalArgumentException ignored) {
        throw new CategoriaInvalidaException("Categoría inválida: " + subcategoria, e);
      }
    }
  }

  public EstadoBien parsearEstado(String nombreEstado) {
    try {
      return EstadoBien.valueOf(nombreEstado.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new EstadoBienInvalidoException("Estado de bien inválido: " + nombreEstado, e);
    }
  }

  public List<Donacion> listarDonaciones() {
    return donacionesRepository.obtenerTodas();
  }

  public void cambiarEstado(int id, String nuevoEstadoTexto) {

    if (nuevoEstadoTexto == null || nuevoEstadoTexto.isBlank()) {
      throw new IllegalArgumentException("Debe indicar el nuevo estado");
    }

    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorPosicion(id);
    if (donacionOpt.isPresent()) {
      EstadoDonacionIndependiente nuevoEstado;
      try {
        nuevoEstado = EstadoDonacionIndependiente.valueOf(
            nuevoEstadoTexto.toUpperCase()
        );
      } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("Estado de donación inválido");
      }
      donacionOpt.get().setEstado(nuevoEstado);
    } else {
      throw new IllegalArgumentException("No se encontró la donación");
    }
  }

  public void actualizarDonacion(int id, DonacionRequestDTO dto) {
    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorPosicion(id);
    if (donacionOpt.isEmpty()) {
      throw new IllegalArgumentException("No se encontró la donación");
    }

    Donacion donacionExistente = donacionOpt.get();
    Bien bienActualizado = null;

    if (dto.bienes() != null && !dto.bienes().isEmpty()) {
      List<Bien> bienes = this.crearListaBienes(dto.bienes());
      bienActualizado = bienes.get(0);
    }

    if (dto.documentoDonante() != null) {
      Optional<Persona> persona = this.personasRepository.buscarPorDocumento(dto.documentoDonante());
      if (persona.isPresent()) {
        donacionExistente.actualizarDatos(persona.get(), bienActualizado);
      } else {
        throw new DonanteNoEncontradoException("No se encontró el donante con documento: " + dto.documentoDonante());
      }
    } else {
      donacionExistente.actualizarDatos(null, bienActualizado);
    }
  }

  public void actualizarDonacionParcial(int id, DonacionPatchDTO dto) {
    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorPosicion(id);
    if (donacionOpt.isEmpty()) {
      throw new IllegalArgumentException("No se encontró la donación");
    }

    Donacion donacionExistente = donacionOpt.get();
    Persona personaActualizada = null;
    Bien bienActualizado = null;

    if (dto.documentoDonante() != null) {
      Optional<Persona> persona = this.personasRepository.buscarPorDocumento(dto.documentoDonante());
      if (persona.isPresent()) {
        personaActualizada = persona.get();
      } else {
        throw new DonanteNoEncontradoException("No se encontró el donante con documento: " + dto.documentoDonante());
      }
    }

    if (dto.bienes() != null && !dto.bienes().isEmpty()) {
      List<Bien> bienes = this.crearListaBienes(dto.bienes());
      bienActualizado = bienes.get(0);
    }

    donacionExistente.actualizarDatos(personaActualizada, bienActualizado);
  }

  public void eliminarDonacion(int id) {
    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorPosicion(id);
    if (donacionOpt.isPresent()) {
      donacionesRepository.borrarPorPosicion(id);
    } else {
      throw new IllegalArgumentException("No se encontró la donación");
    }
  }
}