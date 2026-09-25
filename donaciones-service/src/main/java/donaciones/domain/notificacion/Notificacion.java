package donaciones.domain.notificacion;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.PersonaAdministradora;
import donaciones.domain.donante.Persona;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
public class Notificacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificacion_id")
    private Long id;
    @Column(name = "receptor_id", nullable = false)
    private Long receptorId;
    @Column(name = "tipo_receptor", nullable = false, length = 32)
    private String tipoReceptor;
    @Column(nullable = false, length = 1000)
    private String mensaje;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EstadoNotificacion estado;
    @Column(nullable = false)
    private LocalDateTime fecha;

    protected Notificacion() { }

    public Notificacion(Notificable receptor, String mensaje) {
        this.receptorId = obtenerId(receptor);
        this.tipoReceptor = obtenerTipo(receptor);
        this.mensaje = mensaje;
        this.estado = EstadoNotificacion.PENDIENTE;
        this.fecha = LocalDateTime.now();
    }

    private Long obtenerId(Notificable receptor) {
        if (receptor instanceof Persona persona) return persona.getId();
        if (receptor instanceof EntidadBeneficiaria entidad) return entidad.getId();
        if (receptor instanceof PersonaAdministradora administradora) return administradora.getId();
        throw new IllegalArgumentException("Receptor no persistible: " + receptor.getClass().getName());
    }

    private String obtenerTipo(Notificable receptor) {
        if (receptor instanceof Persona) return "PERSONA";
        if (receptor instanceof EntidadBeneficiaria) return "ENTIDAD";
        if (receptor instanceof PersonaAdministradora) return "ADMINISTRADORA";
        throw new IllegalArgumentException("Receptor no persistible: " + receptor.getClass().getName());
    }

    public Long getId() { return id; }

    public Long getReceptorId() { return receptorId; }

    public String getTipoReceptor() { return tipoReceptor; }

    public LocalDateTime getFecha() { return fecha; }

    public String getMensaje() {
        return mensaje;
    }

    public EstadoNotificacion getEstado() {
        return estado;
    }

    public void marcarComoCompletada() {
        this.estado = EstadoNotificacion.COMPLETADA;
    }

    public void marcarComoFallida() {
        this.estado = EstadoNotificacion.FALLIDA;
    }
}