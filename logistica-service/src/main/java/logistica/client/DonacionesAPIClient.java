package logistica.client;

//en esta clase iran los .execute de las Calls, luego el service dirigira CUANDO

public class DonacionesAPIClient {
  DonacionesAPICalls donacionesCalls;

  public DonacionesAPIClient( DonacionesAPICalls donacionesCalls){
    this.donacionesCalls = donacionesCalls;
  }
}
