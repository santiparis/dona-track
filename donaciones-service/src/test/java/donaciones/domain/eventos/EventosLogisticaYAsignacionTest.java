package donaciones.domain.eventos;

import donaciones.domain.Donacion;
import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.PersonaAdministradora;
import donaciones.domain.donante.Persona;
import donaciones.domain.eventos.listeners.DonacionAsignadaListener;
import donaciones.domain.eventos.listeners.EntregaNoSatisfactoriaListener;
import donaciones.domain.eventos.listeners.EntregaRealizadaListener;
import donaciones.domain.eventos.listeners.InicioRutaListener;
import donaciones.repository.PersonasAdministradorasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.Mockito.*;

public class EventosLogisticaYAsignacionTest {

    private Persona donanteMock;
    private EntidadBeneficiaria entidadMock;
    private PersonaAdministradora adminMock;
    private PublicadorDeEventos publicador;
    private Donacion donacionMock;

    @BeforeEach
    public void setup() {
        donanteMock = mock(Persona.class);
        entidadMock = mock(EntidadBeneficiaria.class);
        adminMock = mock(PersonaAdministradora.class);
        publicador = new PublicadorDeEventos();
        donacionMock = mock(Donacion.class);
        when(donacionMock.getDonante()).thenReturn(donanteMock);
        when(donacionMock.getEntidadBeneficiaria()).thenReturn(entidadMock);
    }

    @Test
    public void testRequerimientoDonacionAsignada_NotificaADonanteYEntidadBeneficiaria() {
        // Requerimiento: Cuando se asigna una donación, notificar a donante ("Su donación ha sido asignada...")
        // y a entidad beneficiaria ("Se le ha asignado satisfactoriamente una nueva donación.").
        when(entidadMock.getRazonSocial()).thenReturn("Comedor Los Niños");

        publicador.suscribir(DonacionAsignadaEvent.class, new DonacionAsignadaListener());
        publicador.publicar(new DonacionAsignadaEvent(donacionMock));

        verify(donanteMock, times(1)).notificar(contains("Comedor Los Niños"));
        verify(entidadMock, times(1)).notificar(contains("Se le ha asignado satisfactoriamente"));
    }

    @Test
    public void testRequerimientoInicioRuta_NotificaADonantesYEntidadesConEnlaceAlMapa() {
        // Requerimiento: Al iniciarse la ruta, notificar a entidades y donantes de esa ruta adjuntando
        // el enlace al mapa de seguimiento en tiempo real.
        publicador.suscribir(InicioRutaEvent.class, new InicioRutaListener());
        publicador.publicar(new InicioRutaEvent(donacionMock, "https://donatrack.org/mapa/123"));

        verify(donanteMock, times(1)).notificar(contains("https://donatrack.org/mapa/123"));
        verify(entidadMock, times(1)).notificar(contains("https://donatrack.org/mapa/123"));
    }

    @Test
    public void testRequerimientoEntregaRealizada_NotificaComprobanteADonanteYEntidad() {
        // Requerimiento: Cuando la entidad confirma la recepción, notificar a la entidad y al donante
        // adjuntando comprobante con fecha, hora y camión responsable.
        publicador.suscribir(EntregaRealizadaEvent.class, new EntregaRealizadaListener());
        publicador.publicar(new EntregaRealizadaEvent(donacionMock, "2026-07-01 14:00", "CAM-999"));

        verify(donanteMock, times(1)).notificar(contains("CAM-999"));
        verify(entidadMock, times(1)).notificar(contains("CAM-999"));
    }

    @Test
    public void testRequerimientoEntregaNoSatisfactoria_NotificaADonanteEntidadYAdministradoresConMotivo() {
        // Requerimiento: Cuando una entrega falla, notificar a entidad, donante y administradores
        // incluyendo la justificación del incidente.
        PersonasAdministradorasRepository repoAdmins = mock(PersonasAdministradorasRepository.class);
        when(repoAdmins.obtenerTodos()).thenReturn(List.of(adminMock));

        publicador.suscribir(EntregaNoSatisfactoriaEvent.class, new EntregaNoSatisfactoriaListener(repoAdmins));
        publicador.publicar(new EntregaNoSatisfactoriaEvent(donacionMock));

        verify(donanteMock, times(1)).notificar(contains("Alerta: Entrega no satisfactoria."));
        verify(entidadMock, times(1)).notificar(contains("Alerta: Entrega no satisfactoria."));
        verify(adminMock, times(1)).notificar(contains("Alerta: Entrega no satisfactoria."));
    }
}
