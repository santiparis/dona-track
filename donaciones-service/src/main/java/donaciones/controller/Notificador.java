package donaciones.controller;

import donaciones.domain.Donacion;
import donaciones.domain.PersonaAdministradora;
import donaciones.domain.donante.Persona;
import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.Notificacion;
import donaciones.db.TransaccionPorRequest;
import donaciones.repository.NotificacionRepository;

import java.util.List;

public class Notificador {
  private final NotificacionRepository notificacionRepository;

  public Notificador() {
    this(null);
  }

  public Notificador(NotificacionRepository notificacionRepository) {
    this.notificacionRepository = notificacionRepository;
  }

  public void donacionAsignada(Donacion donacion) {
    this.notificar(donacion.getDonante(), "Su donación ha sido asignada a la entidad: "
        + donacion.getEntidadBeneficiaria().getRazonSocial());
    this.notificar(donacion.getEntidadBeneficiaria(), "Se le ha asignado satisfactoriamente una nueva donación.");
  }

  public void inicioDeTraslado(Donacion donacion, String urlMapa) {
    String mensaje = "Su entrega está en camino. Siga el recorrido en tiempo real aquí: " + urlMapa;
    this.avisarADonanteYEntidad(donacion, mensaje);
  }

  public void rutaIniciada(List<Donacion> donaciones, String urlMapa) {
    donaciones.forEach(donacion -> this.inicioDeTraslado(donacion, urlMapa));
  }

  // comprobante: lo arma logística con la entrega, la fecha y hora y la patente del camión
  public void entregaConfirmada(Donacion donacion, String comprobante) {
    String mensaje = "¡La donación llegó a destino!\n\n"
        + "Qué se entregó: " + donacion.getBien().getCantidad() + " " + donacion.getBien().getUnidad()
        + " de " + donacion.getBien().getSubcategoria().nombre() + "\n"
        + "Donado por: " + donacion.getDonante().getNombre() + "\n"
        + "Para: " + donacion.getEntidadBeneficiaria().getRazonSocial() + "\n"
        + "Dirección: " + donacion.getEntidadBeneficiaria().getDireccion() + "\n"
        + "Comprobante: " + comprobante + "\n\n"
        + "¡Gracias por hacer posible esta ayuda!";
    this.avisarADonanteYEntidad(donacion, mensaje);
  }

  public void entregaFallida(Donacion donacion, String motivo, List<PersonaAdministradora> administradoras) {
    String mensaje = "Alerta: Entrega no satisfactoria. Motivo: " + motivo;
    this.avisarADonanteYEntidad(donacion, mensaje);
    administradoras.forEach(administradora -> this.notificar(administradora, mensaje));
  }

  private void avisarADonanteYEntidad(Donacion donacion, String mensaje) {
    this.notificar(donacion.getDonante(), mensaje);
    this.notificar(donacion.getEntidadBeneficiaria(), mensaje);
  }

  private void notificar(donaciones.domain.notificacion.Notificable receptor, String mensaje) {
    Contacto medio = receptor.getMedioPredeterminado();
    if (medio == null && receptor.getContactos() != null && !receptor.getContactos().isEmpty()) {
      medio = receptor.getContactos().get(0);
    }
    if (medio == null) {
      return;
    }

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
    this.notificar(donante, "Bienvenido/a a DonaTrack. Para su primer acceso ingrese con el usuario "
        + donante.getUsuario().getNombreUsuario() + " y la contraseña provisoria " + passwordProvisoria + ".");
  }

  public void ausencia(Persona donante, int diasInactividad) {
    this.notificar(donante, "¡Hola " + donante.getNombre() + "! Notamos más de " + diasInactividad
        + " días sin actividad. ¡Te invitamos a realizar una nueva donación en la plataforma!");
  }
}
