package donaciones.service;

import donaciones.domain.donante.Persona;
import donaciones.domain.donante.Contacto;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.PersonaJuridica;
import donaciones.domain.notificacion.EstrategiaDeNotificacion;
import donaciones.domain.notificacion.NotificacionPorEmail;
import donaciones.domain.notificacion.NotificacionPorSMS;
import donaciones.domain.notificacion.NotificacionPorWhatsApp;
import donaciones.dto.ContactoDTO;
import donaciones.dto.DonanteRequestDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DonanteService {
  private final RepositorioPersonas repository;

  public DonanteService(RepositorioPersonas repository) {
    this.repository = repository;
  }

  public void crearDonante(DonanteRequestDTO dto) {
    Persona nuevaPersona;
    List<Contacto> contactos = this.crearContactos(dto.contactos());
    Contacto medioPredeterminado = contactos.get(0);

    if (dto.tipo().equalsIgnoreCase("HUMANA")) {
      nuevaPersona = new PersonaHumana(
              dto.nombre(), dto.apellido(), dto.edad(), null, dto.documento(),
              null, dto.direccion(), contactos, medioPredeterminado, null
      );
    } else if (dto.tipo().equalsIgnoreCase("JURIDICA")) {
      nuevaPersona = new PersonaJuridica(
              null, dto.documento(), dto.nombre(), null, dto.rubro(),
              new ArrayList<>(), contactos, medioPredeterminado, null
      );
    } else {
      throw new IllegalArgumentException("Tipo no compatible");
    }

    repository.agregar(nuevaPersona);
  }

  public List<Persona> listarDonantes() {
    return repository.obtenerTodas();
  }

  public void actualizarDonante(Long id, DonanteRequestDTO dto) {
    Optional<Persona> donanteOpt = repository.buscarPorId(id);

    if (donanteOpt.isPresent()) {
      Persona donanteExistente = donanteOpt.get();
      Persona datosNuevos;
      List<Contacto> contactos = this.crearContactos(dto.contactos());
      Contacto medioPredeterminado = contactos.get(0);
      if (dto.tipo().equalsIgnoreCase("HUMANA")) {
        datosNuevos = new PersonaHumana(
                dto.nombre(), dto.apellido(), dto.edad(), null, dto.documento(),
                null, dto.direccion(), contactos, medioPredeterminado, null
        );
      } else {
        datosNuevos = new PersonaJuridica(
                null, dto.documento(), dto.nombre(), null, dto.rubro(),
                new ArrayList<>(), contactos, medioPredeterminado, null
        );
      }
      donanteExistente.actualizarseDesde(datosNuevos);

    } else {
      throw new IllegalArgumentException("No se encontró un donante ");
    }
  }

  public void eliminarDonante(Long id) {
    repository.eliminarPorId(id);
  }

  private List<Contacto> crearContactos(List<ContactoDTO> contactosDto) {
    if (contactosDto == null || contactosDto.isEmpty()) {
      throw new IllegalArgumentException("La lista de contactos no puede ser nula o vacía");
    }

    return contactosDto.stream()
        .map(this::crearContacto)
        .toList();
  }

  private Contacto crearContacto(ContactoDTO dto) {
    EstrategiaDeNotificacion estrategia = switch (dto.estrategia().toUpperCase()) {
      case "EMAIL" -> new NotificacionPorEmail();
      case "WHATSAPP" -> new NotificacionPorWhatsApp();
      case "SMS" -> new NotificacionPorSMS();
      default -> throw new IllegalArgumentException("Estrategia de notificacion invalida: " + dto.estrategia());
    };

    return new Contacto(estrategia, dto.valor());
  }
}
