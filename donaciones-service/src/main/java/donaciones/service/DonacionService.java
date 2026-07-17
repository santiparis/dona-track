package donaciones.service;

import donaciones.domain.*;
import donaciones.domain.donante.Persona;
import donaciones.domain.eventos.EntregaNoSatisfactoriaEvent;
import donaciones.domain.eventos.EntregaRealizadaEvent;
import donaciones.domain.eventos.InicioRutaEvent;
import donaciones.domain.eventos.PublicadorDeEventos;
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
  private final PublicadorDeEventos publicador;

  public DonacionService(DonacionRepository donacionesRepository, RepositorioPersonas personasRepository, PublicadorDeEventos publicador) {
    this.donacionesRepository = donacionesRepository;
    this.personasRepository = personasRepository;
    this.publicador = publicador;
  }

  public void crearDonacion(DonacionRequestDTO dto) {
    List<Bien> bienesDelDominio = this.crearListaBienes(dto.bienes());

    for (Bien bien : bienesDelDominio) {
      Optional<Persona> persona = this.personasRepository.buscarPorId(dto.idDonante());

      if (persona.isPresent()) {
        this.donacionesRepository.guardar(new Donacion(persona.get(), bien));
      } else {
        throw new DonanteNoEncontradoException("No se encontró el donante con id: " + dto.idDonante());
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

  public void cambiarEstado(Long id, String nuevoEstadoTexto, String nombreCamion) {

    if (nuevoEstadoTexto == null || nuevoEstadoTexto.isBlank()) {
      throw new IllegalArgumentException("Debe indicar el nuevo estado");
    }

    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorId(id);
    if (donacionOpt.isPresent()) {
      EstadoDonacion nuevoEstado;
      Donacion donacion = donacionOpt.get();

      try {
        nuevoEstado = EstadoDonacion.valueOf(
            nuevoEstadoTexto.toUpperCase()
        );
      } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("Estado de donación inválido");
      }
      donacion.setEstado(nuevoEstado);

      if (nuevoEstado == EstadoDonacion.ENTREGA_FALLIDA) {
        publicador.publicar(
            new EntregaNoSatisfactoriaEvent(donacion));
      } else if (nuevoEstado == EstadoDonacion.ENTREGADA) {
        String fechaYHora = LocalDate.now().toString();
        publicador.publicar(
            new EntregaRealizadaEvent(donacion, fechaYHora, nombreCamion));
      } else if (nuevoEstado == EstadoDonacion.EN_TRASLADO) {
        publicador.publicar(new InicioRutaEvent(donacion, null));
      }

    } else {
      throw new IllegalArgumentException("No se encontró la donación");
    }
  }

  public void actualizarDonacion(Long id, DonacionRequestDTO dto) {
    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorId(id);
    if (donacionOpt.isEmpty()) {
      throw new IllegalArgumentException("No se encontró la donación");
    }

    Donacion donacionExistente = donacionOpt.get();
    Bien bienActualizado = null;

    if (dto.bienes() != null && !dto.bienes().isEmpty()) {
      List<Bien> bienes = this.crearListaBienes(dto.bienes());
      bienActualizado = bienes.get(0);
    }

    if (dto.idDonante() != null) {
      Optional<Persona> persona = this.personasRepository.buscarPorId(dto.idDonante());
      if (persona.isPresent()) {
        donacionExistente.actualizarDatos(persona.get(), bienActualizado);
      } else {
        throw new DonanteNoEncontradoException("No se encontró el donante con id: " + dto.idDonante());
      }
    } else {
      donacionExistente.actualizarDatos(null, bienActualizado);
    }
  }

  public void actualizarDonacionParcial(Long id, DonacionPatchDTO dto) {
    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorId(id);
    if (donacionOpt.isEmpty()) {
      throw new IllegalArgumentException("No se encontró la donación");
    }

    Donacion donacionExistente = donacionOpt.get();
    Persona personaActualizada = null;
    Bien bienActualizado = null;

    if (dto.idDonante() != null) {
      Optional<Persona> persona = this.personasRepository.buscarPorId(dto.idDonante());
      if (persona.isPresent()) {
        personaActualizada = persona.get();
      } else {
        throw new DonanteNoEncontradoException("No se encontró el donante con id: " + dto.idDonante());
      }
    }

    if (dto.bienes() != null && !dto.bienes().isEmpty()) {
      List<Bien> bienes = this.crearListaBienes(dto.bienes());
      bienActualizado = bienes.get(0);
    }

    donacionExistente.actualizarDatos(personaActualizada, bienActualizado);
  }

  public void eliminarDonacion(Long id) {
    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorId(id);
    if (donacionOpt.isPresent()) {
      donacionesRepository.borrarPorId(id);
    } else {
      throw new IllegalArgumentException("No se encontró la donación");
    }
  }
}