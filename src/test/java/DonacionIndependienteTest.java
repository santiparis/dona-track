import donante.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DonacionIndependienteTest {

    private Bien bien;
    private DonacionIndependiente donacion;

    @BeforeEach
    void setUp() {
        bien = new Bien(Subcategoria.FIDEOS, 50, "kg", null, null);
        donacion = new DonacionIndependiente(bien);
    }

    @Test
    void estadoInicialEsEnDeposito() {
        assertEquals(EstadoDonacionIndependiente.EN_DEPOSITO, donacion.getEstado());
    }

    @Test
    void disponibleInicialEsLaCantidadDelBien() {
        assertEquals(50, donacion.getDisponible());
    }

    @Test
    void usarDescuentaDelDisponible() {
        donacion.usar(20);
        assertEquals(30, donacion.getDisponible());
    }

    @Test
    void usarTodoElStockDejaDisponibleEnCero() {
        donacion.usar(50);
        assertEquals(0, donacion.getDisponible());
    }

    @Test
    void usarMasDeLoDisponibleLanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> donacion.usar(51));
    }

    @Test
    void agregarAsignacionLaRegistra() {
        Contacto email = new Contacto(TipoContacto.EMAIL, "test@mail.com");
        Usuario usuario = new Usuario("test", "pass");
        PersonaJuridica entidad = new PersonaJuridica(
            TipoDoc.CUIT, "30-12345678-9", "Comedor Sol", null, null,
            java.util.List.of(), java.util.List.of(email), email, usuario
        );
        Necesidad necesidad = new NecesidadExtra(entidad, "necesidad test", java.util.List.of(bien));

        donacion.agregarAsignacion(new AsignacionItem<>(necesidad, 10));
        assertEquals(1, donacion.getAsignaciones().size());
    }
}
