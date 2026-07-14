package donaciones.service.excepcion;

public class EntidadBeneficiariaNoEncontradaException extends RuntimeException {
    public EntidadBeneficiariaNoEncontradaException(String message) {
        super(message);
    }
}
