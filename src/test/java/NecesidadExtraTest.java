import donante.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class NecesidadExtraTest {

    private PersonaJuridica entidad;

    @BeforeEach
    void setUp() {
        Contacto email = new Contacto(TipoContacto.EMAIL, "sol@comedor.org");
        Usuario usuario = new Usuario("sol", "pass");
        entidad = new PersonaJuridica(
            TipoDoc.CUIT, "30-12345678-9", "Comedor Sol", null, null,
            java.util.List.of(), java.util.List.of(email), email, usuario
        );
    }

    @Test
    void getCantidadesDevuelveUnMapConLaSubcategoriaYCantidad() {
        Bien bien = new Bien(Subcategoria.FIDEOS, 20, "kg", null, null);
        NecesidadExtra necesidad = new NecesidadExtra(entidad, "necesitan fideos", List.of(bien));

        var cantidades = necesidad.getCantidades();
        assertEquals(1, cantidades.size());
        assertEquals(20, cantidades.get(Subcategoria.FIDEOS));
    }

    @Test
    void getCantidadesConMultiplesBienesDevuelveTodos() {
        Bien fideos = new Bien(Subcategoria.FIDEOS, 10, "kg", null, null);
        Bien bancos = new Bien(Subcategoria.BANCOS, 3, "unidad", EstadoBien.USADO, null);
        NecesidadExtra necesidad = new NecesidadExtra(entidad, "necesitan varias cosas", List.of(fideos, bancos));

        var cantidades = necesidad.getCantidades();
        assertEquals(2, cantidades.size());
        assertEquals(10, cantidades.get(Subcategoria.FIDEOS));
        assertEquals(3, cantidades.get(Subcategoria.BANCOS));
    }

    @Test
    void getEntidadDevuelveLaEntidadCorrecta() {
        NecesidadExtra necesidad = new NecesidadExtra(entidad, "desc", List.of(
            new Bien(Subcategoria.FIDEOS, 5, "kg", null, null)
        ));
        assertEquals(entidad, necesidad.getEntidad());
    }
}
