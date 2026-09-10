package donaciones.domain;

import donaciones.domain.donante.Persona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DonacionCambioDeEstadoTest {

    private EntidadBeneficiaria entidad;
    private Donacion donacion;

    @BeforeEach
    void setUp() {
        entidad = mock(EntidadBeneficiaria.class);
        donacion = new Donacion(mock(Persona.class), mock(Bien.class));
        donacion.setId(7L);
    }

    @Test
    void alCrearseQuedaEnDepositoYConUnRegistroInicialEnElHistorial() {
        assertEquals(EstadoDonacion.EN_DEPOSITO, donacion.getEstado());
        assertEquals(1, donacion.getHistorialEstados().size());
        assertNull(donacion.getHistorialEstados().get(0).estadoAnterior());
        assertEquals(EstadoDonacion.EN_DEPOSITO, donacion.getHistorialEstados().get(0).estadoNuevo());
    }

    @Test
    void asignarAPasaAAsignadaYGuardaLaEntidad() {
        donacion.asignarA(entidad);

        assertEquals(EstadoDonacion.ASIGNADA, donacion.getEstado());
        assertEquals(entidad, donacion.getEntidadBeneficiaria());
    }

    @Test
    void iniciarTrasladoPasaAEnTraslado() {
        donacion.iniciarTraslado("https://donatrack.org/mapa/123");

        assertEquals(EstadoDonacion.EN_TRASLADO, donacion.getEstado());
    }

    @Test
    void confirmarEntregaPasaAEntregada() {
        donacion.confirmarEntrega("CAM-999");

        assertEquals(EstadoDonacion.ENTREGADA, donacion.getEstado());
    }

    @Test
    void registrarEntregaFallidaDejaElMotivoComoJustificacionEnElHistorial() {
        donacion.registrarEntregaFallida("Tocamos timbre pero nadie respondió");

        assertEquals(EstadoDonacion.ENTREGA_FALLIDA, donacion.getEstado());

        RegistroCambioEstado<EstadoDonacion> ultimo = donacion.getHistorialEstados().get(1);
        assertEquals(EstadoDonacion.EN_DEPOSITO, ultimo.estadoAnterior());
        assertEquals(EstadoDonacion.ENTREGA_FALLIDA, ultimo.estadoNuevo());
        assertEquals("Tocamos timbre pero nadie respondió", ultimo.justificacion());
    }

    @Test
    void cadaCambioDeEstadoDejaSuRegistroEnElHistorial() {
        donacion.asignarA(entidad);
        donacion.iniciarTraslado("https://donatrack.org/mapa/1");
        donacion.confirmarEntrega("CAM-001");

        assertEquals(4, donacion.getHistorialEstados().size());
        assertEquals(EstadoDonacion.ASIGNADA, donacion.getHistorialEstados().get(1).estadoNuevo());
        assertEquals(EstadoDonacion.EN_TRASLADO, donacion.getHistorialEstados().get(2).estadoNuevo());
        assertEquals(EstadoDonacion.ENTREGADA, donacion.getHistorialEstados().get(3).estadoNuevo());
    }
}
