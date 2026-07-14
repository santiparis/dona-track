package donaciones.service;

import donaciones.domain.EntidadBeneficiaria;
import donaciones.dto.EntidadBeneficiariaDTO;
import donaciones.dto.EntidadBeneficiariaPatchDTO;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.service.excepcion.EntidadBeneficiariaNoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntidadBeneficiariaServiceTest {

    private EntidadBeneficiariaRepository repository;
    private EntidadBeneficiariaService service;

    @BeforeEach
    void setUp() {
        repository = mock(EntidadBeneficiariaRepository.class);
        service = new EntidadBeneficiariaService(repository);
    }

    @Test
    void putEntidadBeneficiariaParcialModificaSoloLosCamposRecibidos() {
        EntidadBeneficiaria entidad = new EntidadBeneficiaria("Vieja", "Dir vieja", "111", List.of("a@b.com"));
        when(repository.buscarPorPosicion(0)).thenReturn(Optional.of(entidad));

        service.patchEntidadBeneficiaria(0, new EntidadBeneficiariaPatchDTO("Nueva", null, null, null));

        assertEquals("Nueva", entidad.getRazonSocial());
        assertEquals("Dir vieja", entidad.getDireccion());
        assertEquals(List.of("a@b.com"), entidad.getCorreosRepresentantes());
    }

    @Test
    void putEntidadBeneficiariaSobrescribeLosDatosCompletos() {
        EntidadBeneficiaria entidad = new EntidadBeneficiaria("Vieja", "Dir vieja", "111", List.of("a@b.com"));
        when(repository.buscarPorPosicion(2)).thenReturn(Optional.of(entidad));

        service.putEntidadBeneficiaria(2, new EntidadBeneficiariaDTO("Nueva", "Dir nueva", "222", List.of("b@c.com")));

        assertEquals("Nueva", entidad.getRazonSocial());
        assertEquals("Dir nueva", entidad.getDireccion());
        assertEquals("222", entidad.getTelefono());
    }
    @Test
    void deleteEntidadBeneficiariaLanzaExcepcionSiNoExiste() {
        when(repository.buscarPorPosicion(99)).thenReturn(Optional.empty());

        assertThrows(EntidadBeneficiariaNoEncontradaException.class, () -> service.deleteEntidadBeneficiaria(99));
    }
}
