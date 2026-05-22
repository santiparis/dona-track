package donante;

public class Contacto {
    private TipoContacto tipo;
    private String valor;

    /**
     * Construye un Contacto, validando que el valor sea apropiado para el tipo.
     * @param tipo El tipo de contacto (EMAIL, SMS, WHATSAPP).
     * @param valor El dato del contacto (dirección de email, número de teléfono).
     * @throws IllegalArgumentException si el valor no es válido para el tipo especificado.
     */
    public Contacto(TipoContacto tipo, String valor) {
        validarValor(tipo, valor);
        this.tipo = tipo;
        this.valor = valor;
    }

    public TipoContacto getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    private void validarValor(TipoContacto tipo, String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El valor del contacto no puede ser nulo o vacío.");
        }

        if (tipo == TipoContacto.SMS || tipo == TipoContacto.WHATSAPP) {
            if (valor.replaceAll("\\D", "").length() < 10) {
                throw new IllegalArgumentException("El número de teléfono '" + valor + "' es inválido para " + tipo + ".");
            }
        } else if (tipo == TipoContacto.EMAIL) {
            String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
            if (!valor.matches(emailRegex)) {
                throw new IllegalArgumentException("La dirección de email '" + valor + "' es inválida.");
            }
        }
    }
}
