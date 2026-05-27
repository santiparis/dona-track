package donante;

import java.util.ArrayList;
import java.util.List;
import notificacion.EstrategiaDeNotificacion;
import notificacion.Notificacion;

public class Contacto {
    private final TipoContacto tipo;
    private final String valor;
    private final List<Notificacion> notificaciones = new ArrayList<>();

    public Contacto(TipoContacto tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    public TipoContacto getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public void enviar(String mensaje) {
        EstrategiaDeNotificacion estrategia = this.tipo.crearEstrategia();
        boolean exito = estrategia.enviar(this.valor, mensaje);

        Notificacion notificacion = new Notificacion(mensaje);
        if (exito) {
            notificacion.marcarComoCompletada();
        } else {
            notificacion.marcarComoFallida();
        }
        this.notificaciones.add(notificacion);
    }
}
