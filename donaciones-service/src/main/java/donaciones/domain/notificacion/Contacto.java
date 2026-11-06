package donaciones.domain.notificacion;

import javax.persistence.*;

@Entity
@Table(name = "contacto")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_contacto", discriminatorType = DiscriminatorType.STRING)
public abstract class Contacto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contacto_id") private Long id;
    @Column(name = "notificable_id", insertable = false, updatable = false) private Long notificableId;
    @Column(name = "tipo_notificable", nullable = false, length = 16) private String tipoNotificable;
    @Column(name = "medio_predeterminado", nullable = false) private boolean medioPredeterminado;
    private String valor;

    protected Contacto() { }
    public Contacto(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public Long getId() {
        return id;
    }

    public Long getNotificableId() {
        return notificableId;
    }

    public boolean esMedioPredeterminado() {
        return medioPredeterminado;
    }

    public void identificarNotificable(String tipoNotificable) {
        this.tipoNotificable = tipoNotificable;
    }

    public void marcarComoMedioPredeterminado(boolean medioPredeterminado) {
        this.medioPredeterminado = medioPredeterminado;
    }

    public String getEstrategia() {
        return this.getClass().getSimpleName()
            .replace("NotificacionPor", "")
            .toUpperCase();
    }

    public abstract boolean enviar(String mensaje);
}
