import Donante.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class SistemaDonacionesTest {

    private SistemaDonaciones sistema;
    private Donante donante;
    private EntidadBeneficiaria entidad;

    @BeforeEach
    void setUp() {
        sistema = new SistemaDonaciones();

        Contacto email = new Contacto(TipoContacto.EMAIL, "donante@mail.com");
        donante = new PersonaHumana(
            "Maria", "Lopez", 40,
            TipoDoc.DNI, "99999999",
            Genero.FEMININO, "Calle Falsa 123",
            List.of(email), email, new Usuario("mlopez", "1234")
        );

        entidad = new EntidadBeneficiaria("Comedor Los Pinos", "Av. Siempre 1", "555-1234", List.of("contacto@comedorpinos.org"));
    }

    // ---- ingresarDonacion ----

    @Test
    void ingresarDonacionAgregaAlStock() {
        Bien bien = new Bien(Subcategoria.FIDEOS, 30, "kg", null, null);
        DonacionEntrante donacionEntrante = new DonacionEntrante(donante, "primera donacion", List.of(bien));

        sistema.ingresarDonacion(donacionEntrante);

        // Verificamos indirectamente: si podemos suplir una necesidad de 30 fideos, el stock entró bien
        Necesidad necesidad = new NecesidadExtra(entidad, "necesitan fideos", List.of(
            new Bien(Subcategoria.FIDEOS, 30, "kg", null, null)
        ));
        sistema.agregarNecesidad(necesidad);
        sistema.actualizarNecesidades();

        assertTrue(sistema.getNecesidades().isEmpty(), "La necesidad deberia haber sido suplida");
    }

    @Test
    void ingresarMultiplesDonacionesAcumulaStock() {
        Bien bien1 = new Bien(Subcategoria.FIDEOS, 10, "kg", null, null);
        Bien bien2 = new Bien(Subcategoria.FIDEOS, 25, "kg", null, null);

        sistema.ingresarDonacion(new DonacionEntrante(donante, "primera", List.of(bien1)));
        sistema.ingresarDonacion(new DonacionEntrante(donante, "segunda", List.of(bien2)));

        // Necesidad de 35 = 10 + 25 acumulados
        Necesidad necesidad = new NecesidadExtra(entidad, "necesitan fideos", List.of(
            new Bien(Subcategoria.FIDEOS, 35, "kg", null, null)
        ));
        sistema.agregarNecesidad(necesidad);
        sistema.actualizarNecesidades();

        assertTrue(sistema.getNecesidades().isEmpty());
    }

    // ---- actualizarNecesidades ----

    @Test
    void noSupleNecesidadSiStockInsuficiente() {
        Bien bienDonado = new Bien(Subcategoria.FIDEOS, 5, "kg", null, null);
        sistema.ingresarDonacion(new DonacionEntrante(donante, "donacion chica", List.of(bienDonado)));

        Necesidad necesidad = new NecesidadExtra(entidad, "necesitan mucho", List.of(
            new Bien(Subcategoria.FIDEOS, 100, "kg", null, null)
        ));
        sistema.agregarNecesidad(necesidad);
        sistema.actualizarNecesidades();

        assertFalse(sistema.getNecesidades().isEmpty(), "La necesidad no deberia haber sido suplida");
    }

    @Test
    void supleNecesidadConBienesDeMultiplesDonaciones() {
        // FIFO: consume primero la donacion mas vieja
        Bien bien1 = new Bien(Subcategoria.FIDEOS, 10, "kg", null, null);
        Bien bien2 = new Bien(Subcategoria.FIDEOS, 10, "kg", null, null);
        sistema.ingresarDonacion(new DonacionEntrante(donante, "primera", List.of(bien1)));
        sistema.ingresarDonacion(new DonacionEntrante(donante, "segunda", List.of(bien2)));

        Necesidad necesidad = new NecesidadExtra(entidad, "necesitan 15", List.of(
            new Bien(Subcategoria.FIDEOS, 15, "kg", null, null)
        ));
        sistema.agregarNecesidad(necesidad);
        sistema.actualizarNecesidades();

        assertTrue(sistema.getNecesidades().isEmpty());
    }

    @Test
    void actualizarNecesidadesNoLanzaConcurrentModificationException() {
        Bien bien = new Bien(Subcategoria.FIDEOS, 100, "kg", null, null);
        sistema.ingresarDonacion(new DonacionEntrante(donante, "donacion grande", List.of(bien)));

        // Agrego varias necesidades para que se resuelvan todas en el mismo loop
        sistema.agregarNecesidad(new NecesidadExtra(entidad, "n1", List.of(new Bien(Subcategoria.FIDEOS, 5, "kg", null, null))));
        sistema.agregarNecesidad(new NecesidadExtra(entidad, "n2", List.of(new Bien(Subcategoria.FIDEOS, 5, "kg", null, null))));
        sistema.agregarNecesidad(new NecesidadExtra(entidad, "n3", List.of(new Bien(Subcategoria.FIDEOS, 5, "kg", null, null))));

        assertDoesNotThrow(() -> sistema.actualizarNecesidades());
    }
}
