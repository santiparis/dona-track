package sesion;

import donaciones.controller.SesionController;
import donaciones.db.TransaccionPorRequest;
import donaciones.domain.donante.Rol;
import donaciones.domain.donante.Usuario;
import donaciones.repository.UsuarioRepository;
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
import java.util.EnumMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class LoginHttpTest {
    private static Javalin app;
    private static final Map<Rol, Long> ids = new EnumMap<>(Rol.class);
    private HttpClient cliente;
    private CookieManager cookies;

    @BeforeAll
    static void iniciar() {
        UsuarioRepository usuarios = new UsuarioRepository();
        usuarios.beginTransaction();
        for (Rol rol : Rol.values()) {
            Usuario usuario = new Usuario(rol.name(), "clave-de-prueba", rol);
            usuarios.entityManager().persist(usuario);
            ids.put(rol, usuario.getId());
        }
        usuarios.commitTransaction();
        usuarios.perThreadEntityManagerAccess().dispose();

        TransaccionPorRequest transaccion = new TransaccionPorRequest();
        SesionController controller = new SesionController(usuarios);
        app = Javalin.create(config -> {
            config.fileRenderer(new JavalinHandlebars());
            SesionUsuario.configurar(config);
            config.showJavalinBanner = false;
        });
        app.before(ctx -> transaccion.abrir());
        app.after(ctx -> transaccion.cerrar(ctx.statusCode()));
        app.get("/login", ctx -> ctx.render("sesion/login", controller.login()));
        app.post("/login", controller::ingresar);
        // Rutas exclusivas del test para comprobar la sesión en una segunda petición.
        app.get("/test/sesion", ctx -> {
            var sesion = ctx.req().getSession(false);
            if (sesion == null) {
                ctx.status(401);
            } else {
                ctx.result(sesion.getAttribute(SesionUsuario.USUARIO_ID) + ":" + sesion.getAttribute(SesionUsuario.ROL));
            }
        });
        app.get("/test/prelogin", ctx -> ctx.sessionAttribute("temporal", true));
        app.start(0);
    }

    @AfterAll
    static void detener() {
        if (app != null) app.stop();
    }

    @BeforeEach
    void clienteNuevo() {
        cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cliente = HttpClient.newBuilder().cookieHandler(cookies)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER).build();
    }

    @Test
    void muestraElFormulario() throws Exception {
        var respuesta = get("/login");
        assertEquals(200, respuesta.statusCode());
        assertTrue(respuesta.body().contains("action=\"/login\""));
        assertFalse(respuesta.body().contains("Usuario o contraseña incorrectos."));
    }

    @Test
    void autenticaLosTresRolesYPersisteLaSesionPorCookie() throws Exception {
        Map<Rol, String> destinos = Map.of(Rol.DONANTE, "/donante/donaciones",
                Rol.ENTIDAD, "/entidad/necesidades", Rol.ADMINISTRADORA, "/admin/donaciones");
        for (Rol rol : Rol.values()) {
            clienteNuevo();
            var respuesta = post("usuario=" + rol.name() + "&password=clave-de-prueba");
            assertEquals(303, respuesta.statusCode());
            assertEquals(destinos.get(rol), respuesta.headers().firstValue("location").orElseThrow());
            String cookie = respuesta.headers().firstValue("set-cookie").orElseThrow().toLowerCase();
            assertTrue(cookie.contains("httponly"));
            assertTrue(cookie.contains("samesite=lax"));
            var sesion = get("/test/sesion");
            assertEquals(200, sesion.statusCode());
            assertEquals(ids.get(rol) + ":" + rol.name(), sesion.body());
        }
    }

    @Test
    void rechazaUsuarioInexistenteClaveIncorrectaYCamposFaltantes() throws Exception {
        for (String form : new String[]{"usuario=no-existe&password=clave-de-prueba",
                "usuario=DONANTE&password=incorrecta", "usuario=DONANTE", "password=clave-de-prueba", ""}) {
            clienteNuevo();
            var respuesta = post(form);
            assertEquals(401, respuesta.statusCode());
            assertTrue(respuesta.body().contains("Usuario o contraseña incorrectos."));
            assertEquals(401, get("/test/sesion").statusCode());
        }
    }

    @Test
    void escapaElNombreYNoDevuelveLaPasswordEnElError() throws Exception {
        String nombre = "<script>alert(1)</script>";
        var respuesta = post("usuario=" + URLEncoder.encode(nombre, StandardCharsets.UTF_8)
                + "&password=clave-que-no-debe-volver");
        assertEquals(401, respuesta.statusCode());
        assertFalse(respuesta.body().contains(nombre));
        assertFalse(respuesta.body().contains("clave-que-no-debe-volver"));
        assertTrue(respuesta.body().contains("&lt;script&gt;"));
    }

    @Test
    void reemplazaLaSesionPreviaAlAutenticarse() throws Exception {
        get("/test/prelogin");
        String anterior = cookies.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue();
        assertEquals(303, post("usuario=DONANTE&password=clave-de-prueba").statusCode());
        String actual = cookies.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue();
        assertNotEquals(anterior, actual);
        assertEquals(ids.get(Rol.DONANTE) + ":DONANTE", get("/test/sesion").body());
        HttpClient sinCookies = HttpClient.newHttpClient();
        var viejo = sinCookies.send(HttpRequest.newBuilder(uri("/test/sesion"))
                .header("Cookie", "JSESSIONID=" + anterior).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(401, viejo.statusCode());
    }

    private HttpResponse<String> post(String body) throws Exception {
        return cliente.send(HttpRequest.newBuilder(uri("/login")).timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        return cliente.send(HttpRequest.newBuilder(uri(path)).timeout(Duration.ofSeconds(10)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private static URI uri(String path) {
        return URI.create("http://localhost:" + app.port() + path);
    }
}

