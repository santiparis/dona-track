package sesion;

import donaciones.controller.*;
import donaciones.db.TransaccionPorRequest;
import donaciones.domain.donante.Rol;
import donaciones.domain.donante.Usuario;
import donaciones.repository.UsuarioRepository;
import donaciones.web.Router;
import donaciones.web.SesionUsuario;
import donaciones.web.templates.JavalinHandlebars;
import io.javalin.Javalin;
import org.junit.jupiter.api.*;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Usa las rutas reales; solo reemplaza las operaciones de negocio ajenas al login. */
class AccesoHttpTest {
    private static Javalin app;
    private static final DonacionController donaciones = mock(DonacionController.class);
    private static final DonanteController donantes = mock(DonanteController.class);
    private static final EntidadesBeneficiariasController entidades = mock(EntidadesBeneficiariasController.class);
    private static final AsignacionesController asignaciones = mock(AsignacionesController.class);
    private static final CamionesController camiones = mock(CamionesController.class);
    private static final NotificacionesController notificaciones = mock(NotificacionesController.class);
    private static final Map<Rol, String> inicios = Map.of(
            Rol.DONANTE, "/donante/donaciones", Rol.ENTIDAD, "/entidad/necesidades",
            Rol.ADMINISTRADORA, "/admin/donaciones");
    private HttpClient cliente;
    private CookieManager cookies;

    @BeforeAll
    static void iniciar() {
        enTransaccion(repo -> {
            for (Rol rol : Rol.values()) {
                repo.persist(new Usuario(rol.name(), "clave-de-prueba", rol));
            }
        });
        when(donaciones.misDonaciones(any())).thenReturn(modelo("donante"));
        when(donaciones.deposito(any())).thenReturn(modelo("admin"));
        when(entidades.necesidades(any())).thenReturn(modelo("entidad"));

        TransaccionPorRequest transaccion = new TransaccionPorRequest();
        app = Javalin.create(config -> {
            config.fileRenderer(new JavalinHandlebars());
            SesionUsuario.configurar(config);
            config.showJavalinBanner = false;
        });
        app.before(ctx -> transaccion.abrir());
        app.after(ctx -> transaccion.cerrar(ctx.statusCode()));
        new Router(donaciones, donantes, entidades, asignaciones, camiones, notificaciones,
                new PublicoController(), new SesionController(new UsuarioRepository())).configure(app);
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
                .connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build();
        clearInvocations(donaciones, donantes, entidades, asignaciones, camiones, notificaciones);
    }

    @Test
    void anonimoNoPuedeLeerNiEjecutarOperacionesPrivadas() throws Exception {
        for (String ruta : new String[]{"/donante/donaciones", "/donante/entidades/1",
                "/entidad/necesidades", "/entidad/donaciones/1/recepcion", "/admin/donaciones",
                "/admin/importacion", "/admin/asignaciones/1", "/admin/seguimiento"}) {
            assertRedirect(get(ruta), "/login");
        }
        for (String ruta : new String[]{"/admin/donantes", "/admin/donaciones", "/admin/importacion",
                "/admin/asignaciones/ejecutar", "/admin/camiones/1/eliminar",
                "/entidad/necesidades", "/entidad/necesidades/1/eliminar",
                "/entidad/donaciones/1/recepcion", "/entidad/donaciones/1/no-recibida"}) {
            assertRedirect(post(ruta, ""), "/login");
        }
        verifyNoInteractions(donaciones, donantes, entidades, asignaciones, camiones, notificaciones);
    }

    @Test
    void cadaRolSoloAccedeASuSeccionYLoginLoDevuelveASuInicio() throws Exception {
        for (Rol rol : Rol.values()) {
            clienteNuevo();
            ingresar(rol.name());
            for (Rol destino : Rol.values()) {
                var respuesta = get(inicios.get(destino));
                assertEquals(rol == destino ? 200 : 403, respuesta.statusCode(), rol + " -> " + destino);
                assertEquals("no-store", respuesta.headers().firstValue("Cache-Control").orElseThrow());
            }
            assertRedirect(get("/login"), inicios.get(rol));
        }
    }

    @Test
    void rolIncorrectoNoEjecutaPostsAunqueDeclareOtroRolEnElFormulario() throws Exception {
        ingresar("DONANTE");
        assertEquals(403, post("/admin/donaciones", "rol=ADMINISTRADORA&usuarioId=1").statusCode());
        assertEquals(403, post("/entidad/necesidades", "rol=ENTIDAD&usuarioId=1").statusCode());
        assertEquals(403, get("/admin/donaciones?rol=ADMINISTRADORA&usuarioId=1").statusCode());
        verifyNoInteractions(donaciones, donantes, entidades, asignaciones, camiones, notificaciones);
    }

    @Test
    void laApiAdministrativaTambienExigeSesionYRol() throws Exception {
        assertEquals(401, get("/api/donantes").statusCode());
        assertEquals(401, post("/api/donantes", "").statusCode());
        ingresar("DONANTE");
        assertEquals(403, get("/api/donantes").statusCode());
        assertEquals(403, post("/api/donantes", "").statusCode());
        verifyNoInteractions(donantes);
        ingresar("ADMINISTRADORA");
        assertEquals(200, get("/api/donantes").statusCode());
        verify(donantes).listar(any());
        verifyNoMoreInteractions(donantes);
    }

    @Test
    void muestraLaCuentaRealEnPaginasPrivadasYPublicas() throws Exception {
        ingresar("DONANTE");
        for (String ruta : new String[]{"/donante/donaciones", "/", "/legal"}) {
            var respuesta = get(ruta);
            assertEquals(200, respuesta.statusCode());
            assertTrue(respuesta.body().contains("<span>donante</span>"));
            assertFalse(respuesta.body().contains("Usuario de ejemplo"));
            assertTrue(respuesta.body().contains("action=\"/logout\" method=\"post\""));
            assertTrue(respuesta.body().contains("href=\"/donante/donaciones\">Mi cuenta"));
            assertEquals("no-store", respuesta.headers().firstValue("Cache-Control").orElseThrow());
        }
    }

    @Test
    void logoutInvalidaLaSesionInclusoSiSeReutilizaLaCookieAnterior() throws Exception {
        ingresar("DONANTE");
        String cookieAnterior = cookieSesion();
        assertRedirect(post("/logout", ""), "/login");
        assertRedirect(get("/donante/donaciones"), "/login");
        var repetida = HttpClient.newHttpClient().send(solicitud("/donante/donaciones")
                .header("Cookie", "JSESSIONID=" + cookieAnterior).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertRedirect(repetida, "/login");
        assertFalse(get("/").body().contains("action=\"/logout\""));
        assertRedirect(post("/logout", ""), "/login");
    }

    @Test
    void unGetNoCierraLaSesion() throws Exception {
        ingresar("DONANTE");
        assertEquals(404, get("/logout").statusCode());
        assertEquals(200, get("/donante/donaciones").statusCode());
    }

    @Test
    void noAceptaElIdentificadorDeSesionEnLaUrl() throws Exception {
        ingresar("DONANTE");
        var respuesta = HttpClient.newHttpClient().send(solicitud(
                "/donante/donaciones;jsessionid=" + cookieSesion()).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertNotEquals(200, respuesta.statusCode());
    }

    @Test
    void codificarElPrefijoNoPermiteEntrarSinSesion() throws Exception {
        for (String ruta : new String[]{"/a%64min/donaciones", "/%65ntidad/necesidades", "/donante%2Fdonaciones"}) {
            assertNotEquals(200, get(ruta).statusCode(), ruta);
        }
        verifyNoInteractions(donaciones, donantes, entidades, asignaciones, camiones, notificaciones);
    }

    @Test
    void consultaElRolActualYRechazaUnaCuentaEliminada() throws Exception {
        enTransaccion(repo -> repo.persist(new Usuario("cuenta-modificada", "clave-de-prueba", Rol.DONANTE)));
        ingresar("cuenta-modificada");
        enTransaccion(repo -> repo.entityManager().createQuery(
                "update Usuario u set u.rol = :rol where u.nombreUsuario = :nombre")
                .setParameter("rol", Rol.ENTIDAD).setParameter("nombre", "cuenta-modificada").executeUpdate());
        assertEquals(403, get("/donante/donaciones").statusCode());
        assertEquals(200, get("/entidad/necesidades").statusCode());
        assertRedirect(get("/login"), "/entidad/necesidades");
        enTransaccion(repo -> repo.entityManager().createQuery(
                "delete from Usuario u where u.nombreUsuario = :nombre")
                .setParameter("nombre", "cuenta-modificada").executeUpdate());
        assertRedirect(get("/entidad/necesidades"), "/login");
        assertEquals(200, get("/login").statusCode());
    }

    private void ingresar(String usuario) throws Exception {
        assertEquals(303, post("/login", "usuario=" + usuario + "&password=clave-de-prueba").statusCode());
    }

    private String cookieSesion() {
        return cookies.getCookieStore().getCookies().stream().filter(c -> c.getName().equals("JSESSIONID"))
                .findFirst().orElseThrow().getValue();
    }

    private HttpResponse<String> get(String ruta) throws Exception {
        return cliente.send(solicitud(ruta).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String ruta, String body) throws Exception {
        return cliente.send(solicitud(ruta).header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpRequest.Builder solicitud(String ruta) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + app.port() + ruta))
                .header("Accept", "text/html").timeout(Duration.ofSeconds(10));
    }

    private static void assertRedirect(HttpResponse<String> respuesta, String destino) {
        assertEquals(303, respuesta.statusCode());
        assertEquals(destino, respuesta.headers().firstValue("location").orElseThrow());
    }

    private static Map<String, Object> modelo(String rol) {
        return Map.of("titulo", "Prueba de acceso", "rol", rol,
                "usuario", Map.of("nombre", "Usuario de ejemplo"));
    }

    private static void enTransaccion(Consumer<UsuarioRepository> accion) {
        UsuarioRepository repo = new UsuarioRepository();
        repo.beginTransaction();
        try {
            accion.accept(repo);
            repo.commitTransaction();
        } catch (RuntimeException e) {
            repo.rollbackTransaction();
            throw e;
        } finally {
            repo.perThreadEntityManagerAccess().dispose();
        }
    }
}
