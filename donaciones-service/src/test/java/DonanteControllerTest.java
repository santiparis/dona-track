import donaciones.controller.DonanteController;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.notificacion.ContactoPorSMS;
import donaciones.dto.DonanteRequestDTO;
import donaciones.dto.DonanteResponseDTO;
import donaciones.domain.donante.RepositorioPersonas;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DonanteControllerTest implements SimplePersistenceTest {

    private RepositorioPersonas personasRepository;
    private DonanteController controller;
    private Context ctx;

    @BeforeEach
    void setUp() {
        personasRepository = new RepositorioPersonas(entityManager());
        controller = new DonanteController(personasRepository);
        ctx = mock(Context.class, RETURNS_DEEP_STUBS);
    }

    @Test
    void crearRegistraElDonanteYDevuelveCreated() {
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

        assertEquals(1, personasRepository.obtenerTodas().size());
        verify(ctx, atLeastOnce()).status(HttpStatus.CREATED);
    }

    @Test
    void listarConvierteLosDonantesADTOsSeguros() {
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
        personasRepository.agregar(donante);

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
