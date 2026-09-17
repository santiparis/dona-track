package donaciones.domain.notificacion;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("WHATSAPP")
public class ContactoPorWhatsApp extends Contacto {
    protected ContactoPorWhatsApp() { }

    public ContactoPorWhatsApp(String valor) {
        super(valor);
    }

    public boolean enviar(String mensaje) {
        return true;
    }
}
