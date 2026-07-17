import donaciones.domain.EntidadBeneficiaria;
import donaciones.dto.EntidadBeneficiariaDTO;
import donaciones.dto.EntidadBeneficiariaPatchDTO;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.service.EntidadBeneficiariaService;
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
        entidad.setId(1L);
        when(repository.buscarPorId(0L)).thenReturn(Optional.of(entidad));

        service.patchEntidadBeneficiaria(0L, new EntidadBeneficiariaPatchDTO("Nueva", null, null, null));

        assertEquals("Nueva", entidad.getRazonSocial());
        assertEquals("Dir vieja", entidad.getDireccion());
        assertEquals(List.of("a@b.com"), entidad.getCorreosRepresentantes());
    }

    @Test
    void putEntidadBeneficiariaSobrescribeLosDatosCompletos() {
        EntidadBeneficiaria entidad = new EntidadBeneficiaria("Vieja", "Dir vieja", "111", List.of("a@b.com"));
        entidad.setId(1L);
        when(repository.buscarPorId(2L)).thenReturn(Optional.of(entidad));

        service.putEntidadBeneficiaria(2L, new EntidadBeneficiariaDTO("Nueva", "Dir nueva", "222", List.of("b@c.com")));

        assertEquals("Nueva", entidad.getRazonSocial());
        assertEquals("Dir nueva", entidad.getDireccion());
        assertEquals("222", entidad.getTelefono());
    }
    @Test
    void deleteEntidadBeneficiariaLanzaExcepcionSiNoExiste() {
        when(repository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(EntidadBeneficiariaNoEncontradaException.class, () -> service.deleteEntidadBeneficiaria(99L));
    }
}
