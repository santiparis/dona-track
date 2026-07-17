import donaciones.controller.DonacionController;
import donaciones.domain.Bien;
import donaciones.domain.Donacion;
import donaciones.domain.EstadoBien;
import donaciones.domain.Subcategoria;
import donaciones.domain.donante.Contacto;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.notificacion.NotificacionPorSMS;
import donaciones.dto.DonacionResponseDTO;
import donaciones.dto.DonacionRequestDTO;
import donaciones.service.DonacionService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DonacionControllerTest {

    private DonacionService donacionService;
    private DonacionController controller;
    private Context ctx;

    @BeforeEach
    void setUp() {
        donacionService = mock(DonacionService.class);
        controller = new DonacionController(donacionService);
        ctx = mock(Context.class, RETURNS_DEEP_STUBS);
    }

    @Test
    void crearDevuelveCreatedCuandoElServicioTerminaBien() {
        DonacionRequestDTO dto = new DonacionRequestDTO("123", "desc", List.of());
        when(ctx.bodyAsClass(DonacionRequestDTO.class)).thenReturn(dto);

        controller.crear(ctx);

        verify(donacionService).crearDonacion(dto);
        verify(ctx, atLeastOnce()).status(HttpStatus.CREATED);
    }

    @Test
    void cambiarEstadoDevuelveBadRequestCuandoElServicioLanzaIllegalArgumentException() {
        when(ctx.pathParam("id")).thenReturn("1");
        when(ctx.queryParam("nuevo")).thenReturn(" ");
        when(ctx.queryParam("nombreCamion")).thenReturn(" ");
        doThrow(new IllegalArgumentException("Debe indicar el nuevo estado"))
                .when(donacionService).cambiarEstado(1, " ", " ");

        controller.cambiarEstado(ctx);

        verify(ctx, atLeastOnce()).status(HttpStatus.BAD_REQUEST);
    }

    @Test
    void crearDevuelveMensajeDeErrorCuandoElServicioFalla() {
        DonacionRequestDTO dto = new DonacionRequestDTO("123", "desc", List.of());
        when(ctx.bodyAsClass(DonacionRequestDTO.class)).thenReturn(dto);
        doThrow(new IllegalArgumentException("No se pudo crear la donación"))
                .when(donacionService).crearDonacion(dto);

        controller.crear(ctx);

        verify(ctx).status(HttpStatus.BAD_REQUEST);
    }

    @Test
    void listarConvierteLasDonacionesADTOsSeguros() {
        Contacto contacto = new Contacto(new NotificacionPorSMS(), "111");
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
        when(donacionService.listarDonaciones()).thenReturn(List.of(new Donacion(donante, bien, 10L)));

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
