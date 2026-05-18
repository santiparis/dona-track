package cargaCSV;

public class MapeoCsvEnDonanteException extends RuntimeException {
  public MapeoCsvEnDonanteException(String message) {
    super("Hubo un error en el mapeo de lineas de CSV a Donantes: " + message);
  }
}
