package donaciones.service.excepcion;

public class SubcategoriaInvalidaException extends RuntimeException {
    public SubcategoriaInvalidaException(String message) {
        super(message);
    }
}
