import donaciones.controller.DonacionController;
import donaciones.controller.Notificador;
import donaciones.domain.Bien;
import donaciones.domain.Categoria;
import donaciones.domain.Donacion;
import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.EstadoDonacion;
import donaciones.domain.Subcategoria;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.domain.notificacion.ContactoPorSMS;
import donaciones.dto.BienDTO;
import donaciones.dto.DonacionPatchDTO;
import donaciones.dto.DonacionRequestDTO;
import donaciones.dto.DonacionResponseDTO;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.PersonasAdministradorasRepository;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DonacionControllerTest implements SimplePersistenceTest {
    private DonacionRepository donacionesRepository;
    private RepositorioPersonas personasRepository;
    private DonacionController controller;
    private Context ctx;

    @BeforeEach
    void setUp() {
        donacionesRepository = new DonacionRepository(entityManager());
        personasRepository = new RepositorioPersonas(entityManager());
        controller = new DonacionController(
                donacionesRepository,
                personasRepository,
                new PersonasAdministradorasRepository(entityManager()),
                new Notificador());
        ctx = mock(Context.class, RETURNS_DEEP_STUBS);
    }

    @Test
    void crearDevuelveCreatedCuandoLaDonacionSeGuarda() {
        PersonaHumana donante = guardarDonante();
        when(ctx.bodyAsClass(DonacionRequestDTO.class)).thenReturn(dto(donante.getId(), "Fideos"));

        controller.crear(ctx);

        assertEquals(1, donacionesRepository.obtenerTodas().size());
        verify(ctx, atLeastOnce()).status(HttpStatus.CREATED);
    }

    @Test
    void crearDevuelveErrorCuandoLaSubcategoriaEsInvalida() {
        when(ctx.bodyAsClass(DonacionRequestDTO.class)).thenReturn(dto(123L, "NO_EXISTE"));

        controller.crear(ctx);

        verify(ctx, atLeastOnce()).status(HttpStatus.BAD_REQUEST);
    }

    @Test
    void confirmarEntregaDevuelveNotFoundCuandoNoExisteLaDonacion() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.bodyAsClass(DonacionPatchDTO.class))
                .thenReturn(new DonacionPatchDTO(null, null, "ENTREGADA", "AB123CD"));

        controller.actualizarParcial(ctx);

        verify(ctx, atLeastOnce()).status(HttpStatus.NOT_FOUND);
    }

    @Test
    void marcarEnTrasladoActualizaUnaDonacionPersistida() {
        PersonaHumana donante = guardarDonante();
        EntidadBeneficiaria entidad = new EntidadBeneficiaria("Comedor", "Calle 1", "123", List.of());
        new EntidadBeneficiariaRepository(entityManager()).guardar(entidad);
        Donacion donacion = new Donacion(donante, new Bien(Subcategoria.FIDEOS, 1, "kg", "desc", null, null, null));
        donacion.asignarA(entidad);
        donacionesRepository.guardar(donacion);
        when(ctx.pathParam("id")).thenReturn(donacion.getId().toString());
        when(ctx.bodyAsClass(DonacionPatchDTO.class))
                .thenReturn(new DonacionPatchDTO(null, null, "EN_TRASLADO", "https://donatrack.org/mapa/1"));

        controller.actualizarParcial(ctx);

        assertEquals(EstadoDonacion.EN_TRASLADO, donacion.getEstado());
    }

    @Test
    void crearDonacionGuardaUnaDonacionCuandoElDonanteExiste() {
        PersonaHumana donante = guardarDonante();

        controller.crearDonacion(dto(donante.getId(), "Fideos"));

        assertEquals(1, donacionesRepository.obtenerTodas().size());
    }

    @Test
    void crearDonacionLanzaExcepcionSiElDonanteNoExiste() {
        assertThrows(IllegalArgumentException.class, () -> controller.crearDonacion(dto(404L, "Fideos")));
    }

    @Test
    void parsearCategoriaLanzaExcepcionParaValorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> controller.parsearSubcategoria("NO_EXISTE"));
    }

    @Test
    void parsearEstadoLanzaExcepcionParaValorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> controller.parsearEstado("MALO"));
    }

    @Test
    void crearSubcategoriaConstruyeUnObjetoConLaCategoriaEsperada() {
        Subcategoria subcategoria = controller.parsearSubcategoria("ALIMENTOS");

        assertEquals(Categoria.ALIMENTOS, subcategoria.getCategoria());
        assertEquals("Fideos", subcategoria.nombre());
    }

    @Test
    void listarConvierteDonacionesPersistidasADTOsSeguros() {
        PersonaHumana donante = guardarDonante();
        Donacion donacion = new Donacion(donante, new Bien(Subcategoria.FIDEOS, 3, "kg", "Fideos", null, null, null));
        donacionesRepository.guardar(donacion);

        controller.listar(ctx);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(ctx).json(captor.capture());
        List<?> respuesta = (List<?>) captor.getValue();
        assertEquals(1, respuesta.size());
        DonacionResponseDTO dto = (DonacionResponseDTO) respuesta.get(0);
        assertEquals(donacion.getId(), dto.id());
        assertEquals("30123456", dto.donante().documento());
        assertEquals("FIDEOS", dto.bien().subcategoria());
    }

    private PersonaHumana guardarDonante() {
        ContactoPorSMS contacto = new ContactoPorSMS("111");
        PersonaHumana donante = new PersonaHumana(
                "Ana", "Perez", 35, null, "30123456", null, "Calle 123", List.of(contacto), contacto, null);
        personasRepository.agregar(donante);
        return donante;
    }

    private DonacionRequestDTO dto(Long idDonante, String subcategoria) {
        return new DonacionRequestDTO(idDonante, "desc",
                List.of(new BienDTO(false, false, subcategoria, 5, "kg", "descripción", null, null, null)));
    }
}
