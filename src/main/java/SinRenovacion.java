public class SinRenovacion implements PoliticaDeRenovacion {
  @Override
  public boolean seRenueva() {
    return false;
  }

  @Override
  public boolean estaVencida() {
    return false;
  }
}
