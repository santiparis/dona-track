import donaciones.domain.Bien;
import donaciones.domain.EstadoBien;
import donaciones.domain.Necesidad;
import donaciones.domain.Periodo;
import donaciones.domain.RenovacionPeriodica;
import donaciones.domain.SinRenovacion;
import donaciones.domain.Subcategoria;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class NecesidadTest {

    @Test
    void nuevaNecesidadNoEstaSatisfecha() {
        Necesidad necesidad = new Necesidad(
                "necesitan fideos",
                new SinRenovacion(),
                Map.of(Subcategoria.FIDEOS, 20)
        );
        assertFalse(necesidad.estaSatisfecha());
    }

    @Test
    void necesidadEstaSatisfechaCuandoSeSuplenTodasLasCantidades() {
        Necesidad necesidad = new Necesidad(
                "necesitan fideos",
                new SinRenovacion(),
                Map.of(Subcategoria.FIDEOS, 20)
        );
        necesidad.registrarSuplido(new Bien(Subcategoria.FIDEOS, 20, "kg", null, null, null, null));
        assertTrue(necesidad.estaSatisfecha());
    }

    @Test
    void registrarSuplidoParcialNoSatisfaceLaNecesidad() {
        Necesidad necesidad = new Necesidad(
                "necesitan fideos",
                new SinRenovacion(),
                Map.of(Subcategoria.FIDEOS, 20)
        );
        necesidad.registrarSuplido(new Bien(Subcategoria.FIDEOS, 10, "kg", null, null, null, null));
        assertFalse(necesidad.estaSatisfecha());
    }

    @Test
    void registrarSuplidoNoExcedeLaCantidadRequerida() {
        Necesidad necesidad = new Necesidad(
                "necesitan fideos",
                new SinRenovacion(),
                Map.of(Subcategoria.FIDEOS, 10)
        );
        necesidad.registrarSuplido(new Bien(Subcategoria.FIDEOS, 50, "kg", null, null, null, null));
        assertTrue(necesidad.estaSatisfecha());
    }

    @Test
    void registrarSuplidoConCantidadCeroNoModificaElEstado() {
        Necesidad necesidad = new Necesidad(
                "necesitan fideos",
                new SinRenovacion(),
                Map.of(Subcategoria.FIDEOS, 10)
        );
        necesidad.registrarSuplido(new Bien(Subcategoria.FIDEOS, 0, "kg", null, null, null, null));
        assertFalse(necesidad.estaSatisfecha());
    }

    @Test
    void necesidadConMultiplesSubcategoriasRequiereTodas() {
        Necesidad necesidad = new Necesidad(
                "necesitan varias cosas",
                new SinRenovacion(),
                Map.of(Subcategoria.FIDEOS, 10, Subcategoria.BANCOS, 3)
        );
        necesidad.registrarSuplido(new Bien(Subcategoria.FIDEOS, 10, "kg", null, null, null, null));
        assertFalse(necesidad.estaSatisfecha());

        necesidad.registrarSuplido(new Bien(Subcategoria.BANCOS, 3, "unidad", null, null, EstadoBien.USADO, null));
        assertTrue(necesidad.estaSatisfecha());
    }

    // --- Política de renovación ---

    @Test
    void sinRenovacionNoSeRenueva() {
        Necesidad necesidad = new Necesidad("desc", new SinRenovacion(), Map.of(Subcategoria.FIDEOS, 1));
        assertFalse(necesidad.getRenovacion().seRenueva());
    }

    @Test
    void sinRenovacionNuncaEstaVencida() {
        Necesidad necesidad = new Necesidad("desc", new SinRenovacion(), Map.of(Subcategoria.FIDEOS, 1));
        assertFalse(necesidad.getRenovacion().estaVencida());
    }

    @Test
    void renovacionPeriodicaSeRenueva() {
        Necesidad necesidad = new Necesidad("desc", new RenovacionPeriodica(LocalDate.now(), Periodo.SEMANAL), Map.of(Subcategoria.FIDEOS, 1));
        assertTrue(necesidad.getRenovacion().seRenueva());
    }

    @Test
    void renovacionPeriodicaNoEstaVencidaSiFechaFinEsFutura() {
        Necesidad necesidad = new Necesidad("desc", new RenovacionPeriodica(LocalDate.now(), Periodo.SEMANAL), Map.of(Subcategoria.FIDEOS, 1));
        assertFalse(necesidad.getRenovacion().estaVencida());
    }

    @Test
    void renovacionPeriodicaEstaVencidaSiFechaFinEsPasada() {
        Necesidad necesidad = new Necesidad("desc", new RenovacionPeriodica(LocalDate.now().minusDays(10), Periodo.SEMANAL), Map.of(Subcategoria.FIDEOS, 1));
        assertTrue(necesidad.getRenovacion().estaVencida());
    }
}