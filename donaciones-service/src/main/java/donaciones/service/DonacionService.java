package donaciones.service;

import donaciones.domain.*;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.eventos.EntregaNoSatisfactoriaEvent;
import donaciones.domain.eventos.EntregaRealizadaEvent;
import donaciones.domain.eventos.InicioRutaEvent;
import donaciones.dto.DonacionRequestDTO;
import donaciones.repository.DonacionRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DonacionService {
  private final DonacionRepository repository;

  public DonacionService(DonacionRepository repository) {
    this.repository = repository;
  }

  public void crearDonacion(DonacionRequestDTO dto) {
    Persona personaMock = new PersonaHumana(
            "donante", "prueba", 30, null, dto.documentoDonante(), null, "Calle 123", new ArrayList<>(), null, null
    );
    List<Bien> bienesDelDominio = dto.bienes().stream()
            .map(bienDto -> {
              Subcategoria subcat = null;

              return new Bien(
                      subcat,
                      bienDto.cantidad(),
                      bienDto.unidad(),
                      bienDto.descripcion(),
                      null,
                      null,
                      null
              );
            })
            .toList();
    DonacionEntrante entrante = new DonacionEntrante(personaMock, dto.descripcion(), bienesDelDominio);
    entrante.getDonacionesIndependientes().forEach(repository::guardar);
  }

  public List<DonacionIndependiente> listarDonaciones() {
    return repository.obtenerTodas();
  }

  public void cambiarEstado(int id, String nuevoEstadoTexto, String nombreCamion) {

    if (nuevoEstadoTexto == null || nuevoEstadoTexto.isBlank()) {
      throw new IllegalArgumentException("Debe indicar el nuevo estado");
    }

    Optional<DonacionIndependiente> donacionOpt = repository.buscarPorPosicion(id);
    if (donacionOpt.isPresent()) {
      EstadoDonacionIndependiente nuevoEstado;
      DonacionIndependiente donacion = donacionOpt.get();

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
    Optional<DonacionIndependiente> donacionOpt = repository.buscarPorPosicion(id);
    if (donacionOpt.isPresent()) {
      repository.borrarPorPosicion(id);
    } else {
      throw new IllegalArgumentException("No se encontró la donación");
    }
  }
}