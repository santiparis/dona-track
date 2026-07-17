import donaciones.domain.Categoria;
import donaciones.domain.Donacion;
import donaciones.domain.Subcategoria;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.dto.BienDTO;
import donaciones.dto.DonacionRequestDTO;
import donaciones.repository.DonacionRepository;
import donaciones.domain.eventos.PublicadorDeEventos;
import donaciones.service.DonacionService;
import donaciones.service.excepcion.CategoriaInvalidaException;
import donaciones.service.excepcion.DonanteNoEncontradoException;
import donaciones.service.excepcion.EstadoBienInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        donacionService = new DonacionService(donacionesRepository, personasRepository, new PublicadorDeEventos());
    }

    @Test
    void crearDonacionGuardaUnaDonacionCuandoElDonanteExiste() {
        Persona donante = mock(Persona.class);
        when(personasRepository.buscarPorId(123L)).thenReturn(Optional.of(donante));

        DonacionRequestDTO dto = new DonacionRequestDTO(
                123L,
                "donación de prueba",
                List.of(new BienDTO(false, false, "Fideos", 5, "kg", "descripción", null, null, null))
        );

        donacionService.crearDonacion(dto);

        verify(donacionesRepository).guardar(any(Donacion.class));
    }

    @Test
    void crearDonacionLanzaExcepcionSiElDonanteNoExiste() {
        when(personasRepository.buscarPorId(404L)).thenReturn(Optional.empty());

        DonacionRequestDTO dto = new DonacionRequestDTO(
                404L,
                "donación inválida",
                List.of(new BienDTO(false, false, "Fideos", 1, "kg", "desc", null, null, null))
        );

        assertThrows(DonanteNoEncontradoException.class, () -> donacionService.crearDonacion(dto));
    }

    @Test
    void parsearCategoriaLanzaExcepcionParaValorInvalido() {
        assertThrows(CategoriaInvalidaException.class, () -> donacionService.parsearSubcategoria("NO_EXISTE"));
    }

    @Test
    void parsearEstadoLanzaExcepcionParaValorInvalido() {
        assertThrows(EstadoBienInvalidoException.class, () -> donacionService.parsearEstado("MALO"));
    }

    @Test
    void crearSubcategoriaConstruyeUnObjetoConLaCategoriaEsperada() {
        Subcategoria subcategoria = donacionService.parsearSubcategoria("ALIMENTOS");

        assertEquals(Categoria.ALIMENTOS, subcategoria.getCategoria());
        assertEquals("Fideos", subcategoria.nombre());
    }
}
