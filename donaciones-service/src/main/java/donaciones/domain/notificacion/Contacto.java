package donaciones.domain.notificacion;

public abstract class Contacto {
    private String valor;

    public Contacto(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public String getEstrategia() {
        return this.getClass().getSimpleName()
            .replace("NotificacionPor", "")
            .toUpperCase();
    }

    public abstract boolean enviar(String mensaje);
}
