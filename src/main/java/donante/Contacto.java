package donante;

public class Contacto {
    private TipoContacto tipo;
    private String valor;

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
}
