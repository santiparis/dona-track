package donaciones.domain.cargaCSV;

public class MapeoCsvEnPersonaException extends RuntimeException {
  public MapeoCsvEnPersonaException(String message) {
    super("Hubo un error en el mapeo de lineas de CSV a Persona: " + message);
  }
}
