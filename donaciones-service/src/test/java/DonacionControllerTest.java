import donaciones.controller.DonacionController;
import donaciones.controller.Notificador;
import donaciones.domain.Bien;
import donaciones.domain.Categoria;
import donaciones.domain.Donacion;
import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.EstadoDonacion;
import donaciones.domain.Subcategoria;
import donaciones.domain.donante.Persona;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.domain.notificacion.ContactoPorSMS;
import donaciones.dto.BienDTO;
import donaciones.dto.DonacionResponseDTO;
import donaciones.dto.DonacionRequestDTO;
import donaciones.repository.DonacionRepository;
import donaciones.repository.PersonasAdministradorasRepository;
import donaciones.service.excepcion.CategoriaInvalidaException;
import donaciones.service.excepcion.DonanteNoEncontradoException;
import donaciones.service.excepcion.EstadoBienInvalidoException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DonacionControllerTest {

    private DonacionRepository donacionesRepository;
    private RepositorioPersonas personasRepository;
    private DonacionController controller;
    private Context ctx;

    @BeforeEach
    void setUp() {
        donacionesRepository = mock(DonacionRepository.class);
        personasRepository = mock(RepositorioPersonas.class);
        controller = new DonacionController(
                donacionesRepository,
                personasRepository,
                mock(PersonasAdministradorasRepository.class),
                new Notificador()
        );
        ctx = mock(Context.class, RETURNS_DEEP_STUBS);
    }

    @Test
    void crearDevuelveCreatedCuandoLaDonacionSeGuarda() {
        when(personasRepository.buscarPorId(123L)).thenReturn(Optional.of(mock(Persona.class)));
        DonacionRequestDTO dto = new DonacionRequestDTO(
                123L,
                "desc",
                List.of(new BienDTO(false, false, "Fideos", 5, "kg", "descripción", null, null, null))
        );
        when(ctx.bodyAsClass(DonacionRequestDTO.class)).thenReturn(dto);

        controller.crear(ctx);

        verify(donacionesRepository).guardar(any(Donacion.class));
        verify(ctx, atLeastOnce()).status(HttpStatus.CREATED);
    }

    @Test
    void crearDevuelveErrorCuandoLaSubcategoriaEsInvalida() {
        DonacionRequestDTO dto = new DonacionRequestDTO(
                123L,
                "desc",
                List.of(new BienDTO(false, false, "NO_EXISTE", 5, "kg", "descripción", null, null, null))
        );
        when(ctx.bodyAsClass(DonacionRequestDTO.class)).thenReturn(dto);

        controller.crear(ctx);

        verify(ctx, atLeastOnce()).status(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void confirmarEntregaDevuelveNotFoundCuandoNoExisteLaDonacion() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(donacionesRepository.buscarPorId(1L)).thenReturn(Optional.empty());

        controller.confirmarEntrega(ctx);

        verify(ctx, atLeastOnce()).status(HttpStatus.NOT_FOUND);
    }
    @Test
    void marcarEnTrasladoCambiaElEstadoYAvisaAlDonanteYALaEntidad() {
        Donacion donacion = mock(Donacion.class);
        Persona donante = mock(Persona.class);
        EntidadBeneficiaria entidad = mock(EntidadBeneficiaria.class);
        when(donacion.getDonante()).thenReturn(donante);
        when(donacion.getEntidadBeneficiaria()).thenReturn(entidad);
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.queryParam("urlMapa")).thenReturn("https://donatrack.org/mapa/1");
        when(donacionesRepository.buscarPorId(1L)).thenReturn(Optional.of(donacion));

        controller.marcarEnTraslado(ctx);

        verify(donacion).cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
        verify(donante).notificar(contains("https://donatrack.org/mapa/1"));
        verify(entidad).notificar(contains("https://donatrack.org/mapa/1"));
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

        controller.crearDonacion(dto);

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

        assertThrows(DonanteNoEncontradoException.class, () -> controller.crearDonacion(dto));
    }

    @Test
    void parsearCategoriaLanzaExcepcionParaValorInvalido() {
        assertThrows(CategoriaInvalidaException.class, () -> controller.parsearSubcategoria("NO_EXISTE"));
    }

    @Test
    void parsearEstadoLanzaExcepcionParaValorInvalido() {
        assertThrows(EstadoBienInvalidoException.class, () -> controller.parsearEstado("MALO"));
    }

    @Test
    void crearSubcategoriaConstruyeUnObjetoConLaCategoriaEsperada() {
        Subcategoria subcategoria = controller.parsearSubcategoria("ALIMENTOS");

        assertEquals(Categoria.ALIMENTOS, subcategoria.getCategoria());
        assertEquals("Fideos", subcategoria.nombre());
    }

    @Test
    void listarConvierteLasDonacionesADTOsSeguros() {
        ContactoPorSMS contacto = new ContactoPorSMS("111");
        PersonaHumana donante = new PersonaHumana(
            "Ana",
            "Perez",
            35,
            null,
            "30123456",
            null,
            "Calle 123",
            List.of(contacto),
            contacto,
            null
        );
        Bien bien = new Bien(Subcategoria.FIDEOS, 3, "kg", "Fideos", null, null, null);
        Donacion donacion = new Donacion(donante, bien);
        donacion.setId(10L);
        when(donacionesRepository.obtenerTodas()).thenReturn(List.of(donacion));

        controller.listar(ctx);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(ctx).json(captor.capture());
        assertInstanceOf(List.class, captor.getValue());
        List<?> respuesta = (List<?>) captor.getValue();
        assertEquals(1, respuesta.size());
        assertInstanceOf(DonacionResponseDTO.class, respuesta.get(0));
        DonacionResponseDTO dto = (DonacionResponseDTO) respuesta.get(0);
        assertEquals(10L, dto.id());
        assertEquals("30123456", dto.donante().documento());
        assertEquals("FIDEOS", dto.bien().subcategoria());
    }
}
