import donaciones.domain.Bien;
import donaciones.domain.DonacionIndependiente;
import donaciones.domain.EstadoDonacionIndependiente;
import donaciones.domain.Subcategoria;
import donaciones.domain.donante.Persona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class DonacionIndependienteTest {

    private Bien bien;
    private DonacionIndependiente donacion;

    @BeforeEach
    void setUp() {
        bien = new Bien(Subcategoria.FIDEOS, 50, "kg", null, null, null, null);
        donacion = new DonacionIndependiente(1L, bien, mock(Persona.class));
    }

    @Test
    void estadoInicialEsEnDeposito() {
        assertEquals(EstadoDonacionIndependiente.EN_DEPOSITO, donacion.getEstado());
    }

    @Test
    void getBienDevuelveElBienAsociado() {
        assertSame(bien, donacion.getBien());
    }

    @Test
    void cambiarEstadoActualizaElEstado() {
        donacion.setEstado(EstadoDonacionIndependiente.ENTREGADA);
        assertEquals(EstadoDonacionIndependiente.ENTREGADA, donacion.getEstado());
    }

    @Test
    void cambiarEstadoRegistraElCambioEnElHistorial() {
        donacion.setEstado(EstadoDonacionIndependiente.ENTREGADA);
        assertEquals(2, donacion.getHistorialEstados().size());
    }

    @Test
    void cambiarAlMismoEstadoNoAgregaAlHistorial() {
        donacion.setEstado(EstadoDonacionIndependiente.EN_DEPOSITO);
        assertEquals(1, donacion.getHistorialEstados().size());
    }
}
