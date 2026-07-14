import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.Necesidad;
import donaciones.domain.SinRenovacion;
import donaciones.domain.Subcategoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EntidadBeneficiariaTest {

    private EntidadBeneficiaria entidad;
    private Necesidad necesidad;

    @BeforeEach
    void setUp() {
        entidad = new EntidadBeneficiaria(1L, "Comedor Sol", "Calle 1", "1111-1111", List.of("sol@comedor.org"));
        necesidad = new Necesidad("necesitan fideos", new SinRenovacion(), Map.of(Subcategoria.FIDEOS, 10));
    }

    @Test
    void registrarNecesidadLaAgregaALaLista() {
        entidad.registrarNecesidad(necesidad);
        assertEquals(1, entidad.getNecesidades().size());
    }

    @Test
    void noRegistraNecesidadDuplicada() {
        entidad.registrarNecesidad(necesidad);
        entidad.registrarNecesidad(necesidad);
        assertEquals(1, entidad.getNecesidades().size());
    }

    @Test
    void registraMultiplesNecesidadesDistintas() {
        Necesidad otra = new Necesidad("necesitan ropa", new SinRenovacion(), Map.of(Subcategoria.REMERAS, 5));
        entidad.registrarNecesidad(necesidad);
        entidad.registrarNecesidad(otra);
        assertEquals(2, entidad.getNecesidades().size());
    }
}