import donaciones.domain.Bien;
import donaciones.domain.DonacionEntrante;
import donaciones.domain.DonacionIndependiente;
import donaciones.domain.EstadoBien;
import donaciones.domain.Subcategoria;
import donaciones.domain.donante.Contacto;
import donaciones.domain.donante.Genero;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.TipoDoc;
import donaciones.domain.donante.Usuario;
import donaciones.domain.notificacion.NotificacionPorEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class DonacionEntranteTest {

    private Persona donante;
    private Bien bien1;
    private Bien bien2;

    @BeforeEach
    void setUp() {
        NotificacionPorEmail estrategiaMock = mock(NotificacionPorEmail.class);
        Contacto email = new Contacto(estrategiaMock, "test@mail.com");
        donante = new PersonaHumana(
            "Juan", "Perez", 30,
            TipoDoc.DNI, "12345678",
            Genero.MASCULINO, "Av. Siempreviva 123",
            List.of(email), email, new Usuario("juanp", "pass")
        );
        bien1 = new Bien(Subcategoria.FIDEOS, 10, "kg", null, null, null, null);
        bien2 = new Bien(Subcategoria.REMERAS, 5, "unidades", null, null, EstadoBien.USADO, null);
    }

    @Test
    void creaLasDonacionesIndependientesAlCrearse() {
        DonacionEntrante donacionEntrante = new DonacionEntrante(donante, "donacion test", List.of(bien1, bien2));
        assertEquals(2, donacionEntrante.getDonacionesIndependientes().size());
    }

    @Test
    void cadaDonacionIndependienteTieneElBienCorrecto() {
        DonacionEntrante donacionEntrante = new DonacionEntrante(donante, "donacion test", List.of(bien1));
        DonacionIndependiente ind = donacionEntrante.getDonacionesIndependientes().get(0);
        assertEquals(bien1, ind.getBien());
    }

    @Test
    void lanzaExcepcionSiListaDeBienesEsVacia() {
        assertThrows(IllegalArgumentException.class, () ->
            new DonacionEntrante(donante, "donacion test", List.of())
        );
    }

    @Test
    void lanzaExcepcionSiDescripcionEsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
            new DonacionEntrante(donante, "  ", List.of(bien1))
        );
    }
}
