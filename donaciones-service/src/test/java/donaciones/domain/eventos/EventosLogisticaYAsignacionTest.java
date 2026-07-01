package donaciones.domain.eventos;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.PersonaAdministradora;
import donaciones.domain.donante.Persona;
import donaciones.domain.eventos.listeners.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.Mockito.*;

public class EventosLogisticaYAsignacionTest {

    private Publicador<DonacionAsignadaEvent> pubAsignacion;
    private Publicador<InicioRutaEvent> pubInicioRuta;
    private Publicador<EntregaRealizadaEvent> pubEntregaRealizada;
    private Publicador<EntregaNoSatisfactoriaEvent> pubEntregaFallida;

    private Persona donanteMock;
    private EntidadBeneficiaria entidadMock;
    private PersonaAdministradora adminMock;

    @BeforeEach
    public void setup() {
        pubAsignacion = new Publicador<>();
        pubAsignacion.suscribir(new DonacionAsignadaListener());

        pubInicioRuta = new Publicador<>();
        pubInicioRuta.suscribir(new InicioRutaListener());

        pubEntregaRealizada = new Publicador<>();
        pubEntregaRealizada.suscribir(new EntregaRealizadaListener());

        pubEntregaFallida = new Publicador<>();
        pubEntregaFallida.suscribir(new EntregaNoSatisfactoriaListener());

        donanteMock = mock(Persona.class);
        entidadMock = mock(EntidadBeneficiaria.class);
        adminMock = mock(PersonaAdministradora.class);
    }

    @Test
    public void testEventoDonacionAsignadaNotificaDonanteYEntidad() {
        when(entidadMock.getRazonSocial()).thenReturn("Comedor Los Niños");

        DonacionAsignadaEvent evento = new DonacionAsignadaEvent(donanteMock, entidadMock);
        pubAsignacion.publicar(evento);

        verify(donanteMock, times(1)).notificar(contains("Comedor Los Niños"));
        verify(entidadMock, times(1)).notificar(contains("Se le ha asignado satisfactoriamente"));
    }

    @Test
    public void testEventoInicioRutaNotificaAInvolucradosConEnlaceMapa() {
        InicioRutaEvent evento = new InicioRutaEvent(
                List.of(donanteMock),
                List.of(entidadMock),
                "https://donatrack.org/mapa/123"
        );
        pubInicioRuta.publicar(evento);

        verify(donanteMock, times(1)).notificar(contains("https://donatrack.org/mapa/123"));
        verify(entidadMock, times(1)).notificar(contains("https://donatrack.org/mapa/123"));
    }

    @Test
    public void testEventoEntregaRealizadaNotificaComprobante() {
        EntregaRealizadaEvent evento = new EntregaRealizadaEvent(
                donanteMock, entidadMock, "2026-07-01 14:00", "CAM-999"
        );
        pubEntregaRealizada.publicar(evento);

        verify(donanteMock, times(1)).notificar(contains("CAM-999"));
        verify(entidadMock, times(1)).notificar(contains("CAM-999"));
    }

    @Test
    public void testEventoEntregaNoSatisfactoriaNotificaAAdministradores() {
        EntregaNoSatisfactoriaEvent evento = new EntregaNoSatisfactoriaEvent(
                donanteMock, entidadMock, List.of(adminMock), "Ausencia en el domicilio"
        );
        pubEntregaFallida.publicar(evento);

        verify(donanteMock, times(1)).notificar(contains("Ausencia en el domicilio"));
        verify(entidadMock, times(1)).notificar(contains("Ausencia en el domicilio"));
        verify(adminMock, times(1)).notificar(contains("Ausencia en el domicilio"));
    }
}
