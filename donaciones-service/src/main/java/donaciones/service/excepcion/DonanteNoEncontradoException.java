package donaciones.service.excepcion;

public class DonanteNoEncontradoException extends RuntimeException {
    public DonanteNoEncontradoException(String message) {
        super(message);
    }
}
