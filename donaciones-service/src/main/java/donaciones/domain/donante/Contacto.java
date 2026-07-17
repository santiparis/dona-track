package donaciones.domain.donante;

import donaciones.domain.notificacion.EstrategiaDeNotificacion;

public class Contacto {
    private final String valor;
    private final EstrategiaDeNotificacion estrategiaDeNotificacion;

    public Contacto(EstrategiaDeNotificacion estrategiaDeNotificacion, String valor) {
        this.estrategiaDeNotificacion = estrategiaDeNotificacion;
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public String getEstrategia() {
        return estrategiaDeNotificacion.getClass().getSimpleName()
            .replace("NotificacionPor", "")
            .toUpperCase();
    }

    public boolean enviar(String mensaje) {
        return estrategiaDeNotificacion.enviar(valor, mensaje);
    }
}
