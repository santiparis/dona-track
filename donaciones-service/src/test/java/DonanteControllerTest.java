import donaciones.controller.DonanteController;
import donaciones.domain.donante.Contacto;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.notificacion.NotificacionPorSMS;
import donaciones.dto.DonanteRequestDTO;
import donaciones.dto.DonanteResponseDTO;
import donaciones.service.DonanteService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DonanteControllerTest {

    private DonanteService donanteService;
    private DonanteController controller;
    private Context ctx;

    @BeforeEach
    void setUp() {
        donanteService = mock(DonanteService.class);
        controller = new DonanteController(donanteService);
        ctx = mock(Context.class, RETURNS_DEEP_STUBS);
    }

    @Test
    void crearDevuelveCreatedCuandoElServicioTerminaBien() {
        DonanteRequestDTO dto = new DonanteRequestDTO(
            "HUMANA",
            "30123456",
            "Ana",
            "Perez",
            35,
            "Calle Falsa 123",
            null,
            null,
            List.of(new donaciones.dto.ContactoDTO("SMS", "111"))
        );
        when(ctx.bodyAsClass(DonanteRequestDTO.class)).thenReturn(dto);

        controller.crear(ctx);

        verify(donanteService).crearDonante(dto);
        verify(ctx, atLeastOnce()).status(HttpStatus.CREATED);
    }

    @Test
    void listarConvierteLosDonantesADTOsSeguros() {
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
        when(donanteService.listarDonantes()).thenReturn(List.of(donante));

        controller.listar(ctx);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(ctx).json(captor.capture());
        assertInstanceOf(List.class, captor.getValue());
        List<?> respuesta = (List<?>) captor.getValue();
        assertEquals(1, respuesta.size());
        assertInstanceOf(DonanteResponseDTO.class, respuesta.get(0));
        DonanteResponseDTO dto = (DonanteResponseDTO) respuesta.get(0);
        assertEquals("30123456", dto.documento());
        assertEquals("HUMANA", dto.tipo());
        assertEquals(1, dto.contactos().size());
    }
}
