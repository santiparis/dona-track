package donaciones.service;

import donaciones.domain.*;
import donaciones.domain.donante.Persona;
import donaciones.domain.eventos.EntregaNoSatisfactoriaEvent;
import donaciones.domain.eventos.EntregaRealizadaEvent;
import donaciones.domain.eventos.InicioRutaEvent;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.dto.BienDTO;
import donaciones.dto.DonacionRequestDTO;
import donaciones.repository.DonacionRepository;
import donaciones.service.excepcion.CategoriaInvalidaException;
import donaciones.service.excepcion.DonanteNoEncontradoException;
import donaciones.service.excepcion.EstadoBienInvalidoException;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;
import java.util.Random;

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
        this.donacionesRepository.guardar(new Donacion(persona.get(), bien, new Random().nextLong()));
      } else {
        throw new DonanteNoEncontradoException("No se encontró el donante con documento: " + dto.documentoDonante());
      }
    }
  }

  public List<Bien> crearListaBienes(List<BienDTO> bienesDto) {
    return bienesDto.stream()
        .map(bienDto -> {
          Subcategoria subcat = this.crearSubcategoria(bienDto.categoria(), bienDto.requiereEstado(), bienDto.requiereVencimiento(), bienDto.nombreSubcategoria());
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

  public Subcategoria crearSubcategoria(String categoria, boolean requiereEstado, boolean requiereVencimiento, String nombre) {
    Categoria categoriaEnum = this.parsearCategoria(categoria);
    return new Subcategoria(categoriaEnum, requiereEstado, requiereVencimiento, nombre);
  }

  public Categoria parsearCategoria(String categoria) {
    try {
      return Categoria.valueOf(categoria.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new CategoriaInvalidaException("Categoría inválida: " + categoria, e);
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

  public void cambiarEstado(int id, String nuevoEstadoTexto, String nombreCamion) {

    if (nuevoEstadoTexto == null || nuevoEstadoTexto.isBlank()) {
      throw new IllegalArgumentException("Debe indicar el nuevo estado");
    }

    Optional<Donacion> donacionOpt = donacionesRepository.buscarPorPosicion(id);
    if (donacionOpt.isPresent()) {
      EstadoDonacionIndependiente nuevoEstado;
      Donacion donacion = donacionOpt.get();

      try {
        nuevoEstado = EstadoDonacionIndependiente.valueOf(
            nuevoEstadoTexto.toUpperCase()
        );
      } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("Estado de donación inválido");
      }
      donacion.setEstado(nuevoEstado);

      // TODO Separar en diferentes metodos del service
      if (nuevoEstado == EstadoDonacionIndependiente.ENTREGA_FALLIDA) {
        EntregaNoSatisfactoriaEvent evento = new EntregaNoSatisfactoriaEvent(donacion.getDonante(), donacion.getEntidadBeneficiaria(), null);
        evento.notificarAInvolucrados();
      } else if (nuevoEstado == EstadoDonacionIndependiente.ENTREGADA) {
        String fechaYHora = LocalDate.now().toString();
        EntregaRealizadaEvent evento = new EntregaRealizadaEvent(donacion.getDonante(), donacion.getEntidadBeneficiaria(), fechaYHora, nombreCamion);
        evento.notificarAInvolucrados();
      } else if (nuevoEstado == EstadoDonacionIndependiente.EN_TRASLADO) {
        InicioRutaEvent evento = new InicioRutaEvent(donacion, null);
        evento.notificarAInvolucrados();
      }

    } else {
      throw new IllegalArgumentException("No se encontró la donación");
    }
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