package donaciones.domain.eventos;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.PersonaAdministradora;
import donaciones.domain.donante.Persona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.Mockito.*;

public class EventosLogisticaYAsignacionTest {

    private Persona donanteMock;
    private EntidadBeneficiaria entidadMock;
    private PersonaAdministradora adminMock;

    @BeforeEach
    public void setup() {
        donanteMock = mock(Persona.class);
        entidadMock = mock(EntidadBeneficiaria.class);
        adminMock = mock(PersonaAdministradora.class);
    }

    @Test
    public void testRequerimientoDonacionAsignada_NotificaADonanteYEntidadBeneficiaria() {
        // Requerimiento: Cuando se asigna una donación, notificar a donante ("Su donación ha sido asignada...")
        // y a entidad beneficiaria ("Se le ha asignado satisfactoriamente una nueva donación.").
        when(entidadMock.getRazonSocial()).thenReturn("Comedor Los Niños");

        CambioDeEstadoEnDonacion cambio = new DonacionAsignadaEvent(donanteMock, entidadMock);
        cambio.notificarAInvolucrados();

        verify(donanteMock, times(1)).notificar(contains("Comedor Los Niños"));
        verify(entidadMock, times(1)).notificar(contains("Se le ha asignado satisfactoriamente"));
    }

    @Test
    public void testRequerimientoInicioRuta_NotificaADonantesYEntidadesConEnlaceAlMapa() {
        // Requerimiento: Al iniciarse la ruta, notificar a entidades y donantes de esa ruta adjuntando
        // el enlace al mapa de seguimiento en tiempo real.
        CambioDeEstadoEnDonacion cambio = new InicioRutaEvent(
                List.of(donanteMock),
                List.of(entidadMock),
                "https://donatrack.org/mapa/123"
        );
        cambio.notificarAInvolucrados();

        verify(donanteMock, times(1)).notificar(contains("https://donatrack.org/mapa/123"));
        verify(entidadMock, times(1)).notificar(contains("https://donatrack.org/mapa/123"));
    }

    @Test
    public void testRequerimientoEntregaRealizada_NotificaComprobanteADonanteYEntidad() {
        // Requerimiento: Cuando la entidad confirma la recepción, notificar a la entidad y al donante
        // adjuntando comprobante con fecha, hora y camión responsable.
        CambioDeEstadoEnDonacion cambio = new EntregaRealizadaEvent(
                donanteMock, entidadMock, "2026-07-01 14:00", "CAM-999"
        );
        cambio.notificarAInvolucrados();

        verify(donanteMock, times(1)).notificar(contains("CAM-999"));
        verify(entidadMock, times(1)).notificar(contains("CAM-999"));
    }

    @Test
    public void testRequerimientoEntregaNoSatisfactoria_NotificaADonanteEntidadYAdministradoresConMotivo() {
        // Requerimiento: Cuando una entrega falla, notificar a entidad, donante y administradores
        // incluyendo la justificación del incidente.
        CambioDeEstadoEnDonacion cambio = new EntregaNoSatisfactoriaEvent(
                donanteMock, entidadMock, List.of(adminMock)
        );
        cambio.notificarAInvolucrados();

        verify(donanteMock, times(1)).notificar(contains("Alerta: Entrega no satisfactoria."));
        verify(entidadMock, times(1)).notificar(contains("Alerta: Entrega no satisfactoria."));
        verify(adminMock, times(1)).notificar(contains("Alerta: Entrega no satisfactoria."));
    }
}
