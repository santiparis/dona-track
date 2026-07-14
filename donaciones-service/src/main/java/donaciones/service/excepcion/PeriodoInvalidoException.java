package donaciones.service.excepcion;

public class PeriodoInvalidoException extends RuntimeException {
    public PeriodoInvalidoException(String message) {
        super(message);
    }
}
