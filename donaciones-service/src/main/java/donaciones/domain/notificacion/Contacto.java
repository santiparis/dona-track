package donaciones.domain.notificacion;

import javax.persistence.*;

@Entity
@Table(name = "contacto")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_contacto", discriminatorType = DiscriminatorType.STRING)
@IdClass(ContactoId.class)
public abstract class Contacto {
    @Id @Column(name = "notificable_id") private Long notificableId;
    @Id @Column(name = "tipo_notificable", length = 8) private String tipoNotificable;
    private String valor;

    protected Contacto() { }
    public Contacto(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public void identificarNotificable(Long notificableId, String tipoNotificable) {
        this.notificableId = notificableId;
        this.tipoNotificable = tipoNotificable;
    }

    public String getEstrategia() {
        return this.getClass().getSimpleName()
            .replace("NotificacionPor", "")
            .toUpperCase();
    }

    public abstract boolean enviar(String mensaje);
}
