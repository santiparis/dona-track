package donaciones.service;

import donaciones.domain.Bien;
import donaciones.domain.Categoria;
import donaciones.domain.Donacion;
import donaciones.domain.Subcategoria;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.dto.BienDTO;
import donaciones.dto.DonacionRequestDTO;
import donaciones.repository.DonacionRepository;
import donaciones.service.excepcion.CategoriaInvalidaException;
import donaciones.service.excepcion.DonanteNoEncontradoException;
import donaciones.service.excepcion.EstadoBienInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DonacionServiceTest {

    private DonacionRepository donacionesRepository;
    private RepositorioPersonas personasRepository;
    private DonacionService donacionService;

    @BeforeEach
    void setUp() {
        donacionesRepository = mock(DonacionRepository.class);
        personasRepository = mock(RepositorioPersonas.class);
        donacionService = new DonacionService(donacionesRepository, personasRepository);
    }

    @Test
    void crearDonacionGuardaUnaDonacionCuandoElDonanteExiste() {
        Persona donante = mock(Persona.class);
        when(personasRepository.buscarPorDocumento("123")).thenReturn(Optional.of(donante));

        DonacionRequestDTO dto = new DonacionRequestDTO(
                "123",
                "donación de prueba",
                List.of(new BienDTO("ALIMENTOS", false, false, "Fideos", 5, "kg", "descripción", null, null, null))
        );

        donacionService.crearDonacion(dto);

        verify(donacionesRepository).guardar(any(Donacion.class));
    }

    @Test
    void crearDonacionLanzaExcepcionSiElDonanteNoExiste() {
        when(personasRepository.buscarPorDocumento("404")).thenReturn(Optional.empty());

        DonacionRequestDTO dto = new DonacionRequestDTO(
                "404",
                "donación inválida",
                List.of(new BienDTO("ALIMENTOS", false, false, "Fideos", 1, "kg", "desc", null, null, null))
        );

        assertThrows(DonanteNoEncontradoException.class, () -> donacionService.crearDonacion(dto));
    }

    @Test
    void parsearCategoriaLanzaExcepcionParaValorInvalido() {
        assertThrows(CategoriaInvalidaException.class, () -> donacionService.parsearCategoria("NO_EXISTE"));
    }

    @Test
    void parsearEstadoLanzaExcepcionParaValorInvalido() {
        assertThrows(EstadoBienInvalidoException.class, () -> donacionService.parsearEstado("MALO"));
    }

    @Test
    void crearSubcategoriaConstruyeUnObjetoConLaCategoriaEsperada() {
        Subcategoria subcategoria = donacionService.crearSubcategoria("ALIMENTOS", false, false, "Fideos");

        assertEquals(Categoria.ALIMENTOS, subcategoria.getCategoria());
        assertEquals("Fideos", subcategoria.nombre());
    }
}
