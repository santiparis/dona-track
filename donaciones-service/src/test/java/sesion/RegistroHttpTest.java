package sesion;

import donaciones.controller.SesionController;
import donaciones.controller.DonacionController;
import donaciones.controller.EntidadesBeneficiariasController;
import donaciones.controller.NotificacionesController;
import donaciones.controller.PublicoController;
import donaciones.db.TransaccionPorRequest;
import donaciones.domain.*;
import donaciones.domain.donante.*;
import donaciones.domain.notificacion.Notificacion;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.NotificacionRepository;
import donaciones.repository.UsuarioRepository;
import donaciones.web.Router;
import donaciones.web.SesionUsuario;
import donaciones.web.templates.JavalinHandlebars;
import io.javalin.Javalin;
import org.junit.jupiter.api.*;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RegistroHttpTest {
    private static Javalin app;
    private HttpClient cliente;
    private static final UsuarioRepository usuarios = new UsuarioRepository();

    @BeforeAll
    static void iniciar() {
        SesionController controller = new SesionController(usuarios);
        TransaccionPorRequest transaccion = new TransaccionPorRequest();
        app = Javalin.create(config -> {
            config.fileRenderer(new JavalinHandlebars());
            SesionUsuario.configurar(config);
            config.showJavalinBanner = false;
        });
        app.before(ctx -> transaccion.abrir());
        app.after(ctx -> transaccion.cerrar(ctx.statusCode()));
        new Router(new DonacionController(new DonacionRepository(), new RepositorioPersonas(), null, null, null),
                org.mockito.Mockito.mock(donaciones.controller.DonanteController.class),
                new EntidadesBeneficiariasController(new EntidadBeneficiariaRepository()),
                org.mockito.Mockito.mock(donaciones.controller.AsignacionesController.class),
                new donaciones.controller.CamionesController(null, new DonacionRepository()),
                new NotificacionesController(new NotificacionRepository()), new PublicoController(), controller).configure(app);
        app.start(0);
    }

    @AfterAll
    static void detener() {
        if (app != null) app.stop();
    }

    @BeforeEach
    void clienteNuevo() {
        cliente = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .followRedirects(HttpClient.Redirect.NEVER).build();
    }

    @Test
    void muestraLosTresFormulariosYElAvisoDeCuentaCreada() throws Exception {
        for (String tipo : List.of("HUMANA", "JURIDICA", "ENTIDAD")) {
            var respuesta = get("/usuarios/nuevo?tipo=" + tipo);
            assertEquals(200, respuesta.statusCode());
            assertTrue(respuesta.body().contains("name=\"email\""));
            assertTrue(respuesta.body().contains("name=\"tipo\" value=\"" + tipo + "\""));
        }
        assertTrue(get("/login?registro=ok").body().contains("Tu cuenta fue creada."));
        assertFalse(get("/login").body().contains("Tu cuenta fue creada."));
    }

    @Test
    void registraPersonaHumanaConSusContactosYPuedeIngresar() throws Exception {
        Map<String, String> datos = formulario("HUMANA");
        datos.put("email", datos.get("email").toUpperCase());
        datos.put("telefono", "1155550000");
        datos.put("medio", "SMS");
        assertRegistroExitoso(post("/usuarios", datos));
        enBase(usuario -> {
            assertEquals(Rol.DONANTE, usuario.getRol());
            assertEquals(datos.get("email").toLowerCase(), usuario.getNombreUsuario());
            PersonaHumana persona = (PersonaHumana) new RepositorioPersonas().buscarPorUsuario(usuario.getId()).orElseThrow();
            assertEquals("Ana", persona.getNombre());
            assertEquals("Pérez", persona.getApellido());
            assertEquals(30, persona.getEdad());
            assertEquals(Genero.NO_BINARIO, persona.getGenero());
            assertEquals(2, persona.getContactos().size());
            assertEquals("1155550000", persona.getMedioPredeterminado().getValor());
        }, datos.get("email"));
        var ingreso = post("/login", Map.of("usuario", "  " + datos.get("email") + "  ", "password", "clave-segura"));
        assertEquals(303, ingreso.statusCode());
        assertEquals("/donante/donaciones", ingreso.headers().firstValue("location").orElseThrow());
    }

    @Test
    void registraPersonaJuridicaYEntidadConElUsuarioAsociado() throws Exception {
        Map<String, String> juridica = formulario("JURIDICA");
        assertRegistroExitoso(post("/usuarios", juridica));
        enBase(usuario -> {
            assertEquals(Rol.DONANTE, usuario.getRol());
            PersonaJuridica persona = (PersonaJuridica) new RepositorioPersonas().buscarPorUsuario(usuario.getId()).orElseThrow();
            assertEquals("Asociación", persona.getNombre());
            assertEquals(RazonSocial.ONG, persona.getRazonSocial());
            assertEquals("12345678", persona.getDocumento());
        }, juridica.get("email"));

        Map<String, String> entidad = formulario("ENTIDAD");
        entidad.put("correos", "REPRESENTANTE@example.org\nsegunda@example.org");
        assertRegistroExitoso(post("/usuarios", entidad));
        enBase(usuario -> {
            assertEquals(Rol.ENTIDAD, usuario.getRol());
            var beneficiaria = new EntidadBeneficiariaRepository().buscarPorUsuario(usuario.getId()).orElseThrow();
            assertEquals("Asociación", beneficiaria.getRazonSocial());
            assertEquals("Calle 123", beneficiaria.getDireccion());
            assertEquals(List.of("representante@example.org", "segunda@example.org"), beneficiaria.getCorreosRepresentantes());
            assertEquals(entidad.get("email"), beneficiaria.getMedioPredeterminado().getValor());
        }, entidad.get("email"));
        var ingreso = post("/login", Map.of("usuario", entidad.get("email"), "password", "clave-segura"));
        assertEquals("/entidad/necesidades", ingreso.headers().firstValue("location").orElseThrow());
    }

    @Test
    void validaEnServidorYNoDejaCuentasIncompletas() throws Exception {
        for (Map.Entry<String, String> invalido : Map.ofEntries(
                Map.entry("tipo", "ADMINISTRADORA"), Map.entry("nombre", " "), Map.entry("apellido", ""),
                Map.entry("documento", ""), Map.entry("email", "sin-arroba"),
                Map.entry("password", "corta"), Map.entry("password2", "distinta"), Map.entry("acepta", ""),
                Map.entry("edad", "abc"), Map.entry("genero", "INVENTADO"),
                Map.entry("tipoDoc", "CUIT"), Map.entry("medio", "SMS")).entrySet()) {
            Map<String, String> datos = formulario("HUMANA");
            datos.put(invalido.getKey(), invalido.getValue());
            var respuesta = post("/usuarios", datos);
            assertEquals(400, respuesta.statusCode(), invalido.getKey());
            assertTrue(respuesta.body().contains("role=\"alert\""));
            assertFalse(respuesta.body().contains("clave-segura"));
            assertFalse(respuesta.body().contains("value=\"distinta\""));
            assertCuentaAusente(datos.get("email"));
        }
    }

    @Test
    void conservaCamposYEscapaHtmlAlMostrarUnError() throws Exception {
        Map<String, String> datos = formulario("HUMANA");
        datos.put("nombre", "<script>alert(1)</script>");
        datos.put("password2", "otra-clave");
        var respuesta = post("/usuarios", datos);
        assertEquals(400, respuesta.statusCode());
        assertTrue(respuesta.body().contains("&lt;script&gt;"));
        assertFalse(respuesta.body().contains("<script>alert(1)</script>"));
        assertTrue(respuesta.body().contains("value=\"" + datos.get("email") + "\""));
    }

    @Test
    void rechazaCorreoDuplicadoInclusoSiCambiaElTipoOMayusculas() throws Exception {
        Map<String, String> original = formulario("HUMANA");
        assertRegistroExitoso(post("/usuarios", original));
        Map<String, String> repetido = formulario("ENTIDAD");
        repetido.put("email", original.get("email").toUpperCase());
        var respuesta = post("/usuarios", repetido);
        assertEquals(409, respuesta.statusCode());
        assertTrue(respuesta.body().contains("Ya existe una cuenta con ese correo"));
        enBase(usuario -> {
            assertEquals(Rol.DONANTE, usuario.getRol());
            assertTrue(new EntidadBeneficiariaRepository().buscarPorUsuario(usuario.getId()).isEmpty());
        }, original.get("email"));
    }

    @Test
    void elFormularioNoPuedeElegirUnRolAdministrativo() throws Exception {
        Map<String, String> datos = formulario("HUMANA");
        datos.put("rol", "ADMINISTRADORA");
        assertRegistroExitoso(post("/usuarios", datos));
        enBase(usuario -> assertEquals(Rol.DONANTE, usuario.getRol()), datos.get("email"));
    }

    private Map<String, String> formulario(String tipo) {
        return new HashMap<>(Map.ofEntries(Map.entry("tipo", tipo), Map.entry("email", UUID.randomUUID() + "@example.org"),
                Map.entry("password", "clave-segura"), Map.entry("password2", "clave-segura"), Map.entry("acepta", "on"),
                Map.entry("nombre", "Ana"), Map.entry("apellido", "Pérez"), Map.entry("edad", "30"),
                Map.entry("documento", "12345678"), Map.entry("tipoDoc", "DNI"), Map.entry("genero", "NO_BINARIO"),
                Map.entry("medio", "EMAIL"), Map.entry("razonSocial", "Asociación"), Map.entry("tipoJuridica", "ONG"),
                Map.entry("direccion", "Calle 123")));
    }

    @Test
    void cadaCuentaVeSusDatosYSoloAbreLaRecepcionQueLePertenece() throws Exception {
        String[] emails = new String[4];
        for (int i = 0; i < emails.length; i++) {
            Map<String, String> datos = formulario(i < 2 ? "HUMANA" : "ENTIDAD");
            emails[i] = datos.get("email");
            datos.put("nombre", "Donante de prueba " + i);
            assertRegistroExitoso(post("/usuarios", datos));
        }
        long[] ids = new long[4];
        usuarios.withTransaction(() -> {
            var personas = new RepositorioPersonas();
            var entidades = new EntidadBeneficiariaRepository();
            for (int i = 0; i < 2; i++) {
                var donante = personas.buscarPorUsuario(usuarios.buscarPorNombreUsuario(emails[i]).orElseThrow().getId()).orElseThrow();
                var entidad = entidades.buscarPorUsuario(usuarios.buscarPorNombreUsuario(emails[i + 2]).orElseThrow().getId()).orElseThrow();
                var bien = new Bien(i == 0 ? Subcategoria.FIDEOS : Subcategoria.ARROZ, 3, "kg", null, null, null, null);
                var donacion = new Donacion(donante, bien);
                donacion.asignarA(entidad);
                donacion.cambiarEstado(EstadoDonacion.EN_TRASLADO, "Historial privado " + i);
                new DonacionRepository().guardar(donacion);
                entidad.registrarNecesidad(new Necesidad("Necesidad privada " + i, false, null, null, Map.of(bien.getSubcategoria(), 5)));
                new NotificacionRepository().guardar(new Notificacion(donante, "Aviso donante " + i));
                new NotificacionRepository().guardar(new Notificacion(entidad, "Aviso entidad " + i));
                usuarios.entityManager().flush();
                ids[i] = donacion.getId();
                ids[i + 2] = entidad.getNecesidades().get(0).getId();
            }
        });
        usuarios.perThreadEntityManagerAccess().dispose();

        for (int i = 0; i < 2; i++) {
            int otro = 1 - i;
            String propio = "Donante de prueba " + i;
            String ajeno = "Donante de prueba " + otro;
            clienteNuevo();
            assertEquals(303, post("/login", Map.of("usuario", emails[i], "password", "clave-segura")).statusCode());
            verificarVista("/donante/donaciones?historial=" + ids[i], "Historial privado " + i, "Historial privado " + otro);
            verificarVista("/donante/donaciones?historial=" + ids[otro], i == 0 ? "Fideos" : "Arroz", "Historial privado " + otro);
            verificarVista("/donante/notificaciones", "Aviso donante " + i, "Aviso entidad " + i);
            assertFalse(get("/donante/notificaciones").body().contains("Aviso donante " + otro));

            clienteNuevo();
            assertEquals(303, post("/login", Map.of("usuario", emails[i + 2], "password", "clave-segura")).statusCode());
            verificarVista("/entidad/donaciones", propio, ajeno);
            verificarVista("/entidad/necesidades", "Necesidad privada " + i, "Necesidad privada " + otro);
            verificarVista("/entidad/notificaciones", "Aviso entidad " + i, "Aviso entidad " + otro);
            assertEquals(404, get("/entidad/necesidades?editar=" + ids[otro + 2]).statusCode());
            assertEquals(200, get("/entidad/donaciones/" + ids[i] + "/recepcion").statusCode());
            assertEquals(404, get("/entidad/donaciones/" + ids[otro] + "/recepcion").statusCode());
            assertEquals(404, post("/entidad/donaciones/" + ids[otro] + "/no-recibida", Map.of()).statusCode());
        }
    }

    private void verificarVista(String ruta, String presente, String ausente) throws Exception {
        var respuesta = get(ruta);
        assertEquals(200, respuesta.statusCode(), ruta);
        assertTrue(respuesta.body().contains(presente), presente);
        assertFalse(respuesta.body().contains(ausente), ausente);
    }

    private void assertRegistroExitoso(HttpResponse<String> respuesta) {
        assertEquals(303, respuesta.statusCode(), respuesta.body());
        assertEquals("/login?registro=ok", respuesta.headers().firstValue("location").orElseThrow());
    }

    private void enBase(Consumer<Usuario> verificar, String email) {
        usuarios.beginTransaction();
        try {
            verificar.accept(usuarios.buscarPorNombreUsuario(email).orElseThrow());
        } finally {
            usuarios.rollbackTransaction();
            usuarios.perThreadEntityManagerAccess().dispose();
        }
    }

    private void assertCuentaAusente(String email) {
        usuarios.beginTransaction();
        try {
            assertTrue(usuarios.buscarPorNombreUsuario(email).isEmpty());
        } finally {
            usuarios.rollbackTransaction();
            usuarios.perThreadEntityManagerAccess().dispose();
        }
    }

    private HttpResponse<String> post(String path, Map<String, String> datos) throws Exception {
        String body = datos.entrySet().stream().map(campo -> URLEncoder.encode(campo.getKey(), StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(campo.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
        return cliente.send(HttpRequest.newBuilder(uri(path)).timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        return cliente.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private static URI uri(String path) {
        return URI.create("http://localhost:" + app.port() + path);
    }
}
