package donaciones.service.excepcion;

public class EstadoBienInvalidoException extends RuntimeException {
    public EstadoBienInvalidoException(String message) {
        super(message);
    }

    public EstadoBienInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
