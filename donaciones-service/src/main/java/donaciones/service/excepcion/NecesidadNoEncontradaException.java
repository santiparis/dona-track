package donaciones.service.excepcion;

public class NecesidadNoEncontradaException extends RuntimeException {
    public NecesidadNoEncontradaException(String message) {
        super(message);
    }
}
