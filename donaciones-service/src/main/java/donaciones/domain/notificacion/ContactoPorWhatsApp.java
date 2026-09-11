package donaciones.domain.notificacion;

public class ContactoPorWhatsApp extends Contacto {

    public ContactoPorWhatsApp(String valor) {
        super(valor);
    }

    public boolean enviar(String mensaje) {
        return true;
    }
}