import donaciones.controller.DonacionController;
import donaciones.dto.DonacionRequestDTO;
import donaciones.service.DonacionService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        doThrow(new IllegalArgumentException("Debe indicar el nuevo estado"))
                .when(donacionService).cambiarEstado(1, " ");

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
}
