package donaciones.service.excepcion;

public class CategoriaInvalidaException extends RuntimeException {
    public CategoriaInvalidaException(String message) {
        super(message);
    }

    public CategoriaInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
