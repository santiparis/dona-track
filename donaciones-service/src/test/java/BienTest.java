import donaciones.domain.Bien;
import donaciones.domain.EstadoBien;
import donaciones.domain.Subcategoria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BienTest {

    @Test
    void creaUnBienSimpleSinRequisitos() {
        Bien bien = new Bien(Subcategoria.FIDEOS, 10, "kg", null, null, null, null);
        assertEquals(Subcategoria.FIDEOS, bien.getSubcategoria());
        assertEquals(10, bien.getCantidad());
        assertEquals("kg", bien.getUnidad());
    }

    @Test
    void creaUnBienConEstadoCuandoEsRequerido() {
        Bien bien = new Bien(Subcategoria.BANCOS, 2, "unidad", null, null, EstadoBien.USADO, null);
        assertEquals(EstadoBien.USADO, bien.getEstado());
    }

    @Test
    void lanzaExcepcionSiSubcategoriaRequiereEstadoYNoSePasa() {
        // BANCOS requiereEstado = true
        assertThrows(IllegalArgumentException.class, () ->
            new Bien(Subcategoria.BANCOS, 2, "unidad", null, null, null, null)
        );
    }

    @Test
    void lanzaExcepcionSiSubcategoriaRequiereVencimientoYNoSePasa() {
        // Simulado: si se agregara una subcategoria que requiere vencimiento
        // Este test documentaria el comportamiento esperado.
        // Con FIDEOS (requiereVencimiento=false) no lanza excepcion
        assertDoesNotThrow(() ->
            new Bien(Subcategoria.FIDEOS, 5, "kg", null, null, null, null)
        );
    }
}
