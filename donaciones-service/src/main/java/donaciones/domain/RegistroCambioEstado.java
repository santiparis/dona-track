package donaciones.domain;

import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "registro_cambio_estado")
public class RegistroCambioEstado<T> {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "registro_id") private Long id;
    @Enumerated(EnumType.STRING) @Column(name = "estado_anterior") private EstadoDonacion estadoAnterior;
    @Enumerated(EnumType.STRING) @Column(name = "estado_nuevo", nullable = false) private EstadoDonacion estadoNuevo;
    @Temporal(TemporalType.TIMESTAMP) private Date fecha;
    private String justificacion;

    protected RegistroCambioEstado() { }
    public RegistroCambioEstado(EstadoDonacion estadoAnterior, EstadoDonacion estadoNuevo, Date fecha, String justificacion) {
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fecha = fecha;
        this.justificacion = justificacion;
    }
    public Long getId() { return id; }
    public EstadoDonacion estadoAnterior() { return estadoAnterior; }
    public EstadoDonacion estadoNuevo() { return estadoNuevo; }
    public Date fecha() { return fecha; }
    public String justificacion() { return justificacion; }
}
