import donaciones.domain.donante.Persona;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.dto.ContactoDTO;
import donaciones.dto.DonanteRequestDTO;
import donaciones.service.DonanteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DonanteServiceTest {

    private RepositorioPersonas repository;
    private DonanteService service;

    @BeforeEach
    void setUp() {
        repository = new RepositorioPersonas();
        service = new DonanteService(repository);
    }

    @Test
    void crearDonanteGuardaUnaPersonaCuandoTieneContactos() {
        var dto = new DonanteRequestDTO(
                "HUMANA",
                "30123456",
                "Ana",
                "Perez",
                35,
                "Calle Falsa 123",
                null,
                null,
                List.of(
                        new ContactoDTO("EMAIL", "ana.perez@example.com"),
                        new ContactoDTO("WHATSAPP", "+5491122334455")
                )
        );

        service.crearDonante(dto);

        assertEquals(1, repository.obtenerTodas().size());
        assertEquals("30123456", repository.buscarPorDocumento("30123456").get().getDocumento());
    }

    @Test
    void crearDonanteLanzaExcepcionSiLaListaDeContactosVieneVacia() {
        var dto = new DonanteRequestDTO(
                "HUMANA",
                "30123456",
                "Ana",
                "Perez",
                35,
                "Calle Falsa 123",
                null,
                null,
                List.of()
        );

        assertThrows(IllegalArgumentException.class, () -> service.crearDonante(dto));
    }
}
