package donaciones.controller;

import donaciones.domain.Donacion;
import donaciones.domain.PersonaAdministradora;
import donaciones.domain.donante.Persona;
import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.Notificacion;
import donaciones.db.TransaccionPorRequest;
import donaciones.repository.NotificacionRepository;

import java.util.List;
import java.util.Objects;

public class Notificador {
  // Dirección pública de la web de donaciones, para los links de los avisos
  private static final String URL_PUBLICA =
      Objects.requireNonNullElse(System.getenv("DONACIONES_URL_PUBLICA"), "http://localhost:8081");
  private static final String FIRMA = "\n\n\nGracias,\nde parte de todo el equipo de DonaTrack";
  private final NotificacionRepository notificacionRepository;

  public Notificador() {
    this(null);
  }

  public Notificador(NotificacionRepository notificacionRepository) {
    this.notificacionRepository = notificacionRepository;
  }

  public void donacionAsignada(Donacion donacion) {
    this.notificar(donacion.getDonante(), donacion.getDonante().getNombre(), "¡Tu donación ya tiene destino!\n\n"
        + this.detalle(donacion) + "\n\nTe vamos a avisar cuando el camión salga a entregarla.");
    this.notificar(donacion.getEntidadBeneficiaria(), donacion.getEntidadBeneficiaria().getRazonSocial(),
        "¡Les asignamos una nueva donación!\n\n" + this.detalle(donacion)
        + "\n\nLes vamos a avisar cuando el camión salga a entregarla.");
  }

  // El link lleva a la pantalla de seguimiento de cada uno (pide iniciar sesión)
  public void inicioDeTraslado(Donacion donacion) {
    String mensaje = "¡La donación está en camino!\n\n" + this.detalle(donacion)
        + "\n\nSeguí el recorrido del camión en tiempo real:\n";
    this.notificar(donacion.getDonante(), donacion.getDonante().getNombre(),
        mensaje + URL_PUBLICA + "/donante/seguimiento");
    this.notificar(donacion.getEntidadBeneficiaria(), donacion.getEntidadBeneficiaria().getRazonSocial(),
        mensaje + URL_PUBLICA + "/entidad/seguimiento");
  }

  public void rutaIniciada(List<Donacion> donaciones) {
    donaciones.forEach(this::inicioDeTraslado);
  }

  // comprobante: lo arma logística con la entrega, la fecha y hora y la patente del camión
  public void entregaConfirmada(Donacion donacion, String comprobante) {
    String mensaje = "¡La donación llegó a destino!\n\n" + this.detalle(donacion)
        + "\nComprobante: " + comprobante + "\n\n¡Gracias por hacer posible esta ayuda!";
    this.avisarADonanteYEntidad(donacion, mensaje);
  }

  public void entregaFallida(Donacion donacion, String motivo, List<PersonaAdministradora> administradoras) {
    String mensaje = "No pudimos completar una entrega.\n\n" + this.detalle(donacion)
        + "\nMotivo: " + motivo + "\n\nLa donación vuelve al depósito para volver a entregarla.";
    this.avisarADonanteYEntidad(donacion, mensaje);
    administradoras.forEach(administradora -> this.notificar(administradora, administradora.getNombre(), mensaje));
  }

  // Qué se dona, de quién, para quién y dónde: lo repiten todos los avisos de una donación
  private String detalle(Donacion donacion) {
    String descripcion = donacion.getBien().getDescripcion();
    return "Donación: " + donacion.getBien().getCantidad() + " " + donacion.getBien().getUnidad()
        + " de " + donacion.getBien().getSubcategoria().nombre() + (descripcion == null ? "" : " (" + descripcion + ")")
        + "\nDonada por: " + donacion.getDonante().getNombre()
        + "\nPara: " + donacion.getEntidadBeneficiaria().getRazonSocial()
        + "\nDirección: " + donacion.getEntidadBeneficiaria().getDireccion();
  }

  private void avisarADonanteYEntidad(Donacion donacion, String mensaje) {
    this.notificar(donacion.getDonante(), donacion.getDonante().getNombre(), mensaje);
    this.notificar(donacion.getEntidadBeneficiaria(), donacion.getEntidadBeneficiaria().getRazonSocial(), mensaje);
  }

  // Todos los avisos llevan el saludo con el nombre de quien lo recibe y la firma del equipo
  private void notificar(donaciones.domain.notificacion.Notificable receptor, String nombre, String cuerpo) {
    Contacto medio = receptor.getMedioPredeterminado();
    if (medio == null && receptor.getContactos() != null && !receptor.getContactos().isEmpty()) {
      medio = receptor.getContactos().get(0);
    }
    if (medio == null) {
      return;
    }

    String mensaje = "Hola, " + nombre + ":\n\n" + cuerpo + FIRMA;
    Contacto medioNotificacion = medio;
    Notificacion notificacion = new Notificacion(receptor, mensaje);
    TransaccionPorRequest.despuesDelCommit(() -> {
      if (medioNotificacion.enviar(mensaje)) {
        notificacion.marcarComoCompletada();
      } else {
        notificacion.marcarComoFallida();
      }
      if (notificacionRepository != null) {
        notificacionRepository.guardar(notificacion);
      }
    });
  }

  public void bienvenida(Persona donante, String passwordProvisoria) {
    this.notificar(donante, donante.getNombre(), "¡Te damos la bienvenida a DonaTrack!\n\n"
        + "Para tu primer acceso, ingresá con estos datos:\n"
        + "Usuario: \"" + donante.getUsuario().getNombreUsuario() + "\"\n"
        + "Contraseña provisoria: \"" + passwordProvisoria + "\"");
  }

  public void ausencia(Persona donante, int diasInactividad) {
    this.notificar(donante, donante.getNombre(), "Hace más de " + diasInactividad
        + " días que no te vemos por DonaTrack.\n\n"
        + "Hay entidades que siguen necesitando ayuda: te invitamos a hacer una nueva donación en la plataforma.");
  }
}
