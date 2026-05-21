package notificacion;

public class Notificador {
    private EstrategiaDeNotificacion estrategia;

    public Notificador(EstrategiaDeNotificacion estrategia) {
        this.estrategia = estrategia;
    }

    public void setEstrategia(EstrategiaDeNotificacion estrategia) {
        this.estrategia = estrategia;
    }

    public void enviar(Notificacion notificacion) {
        boolean exito = this.estrategia.enviar(notificacion);
        if (exito) {
            notificacion.marcarComoCompletada();
        } else{
            notificacion.marcarComoFallida();
        }

    }
}