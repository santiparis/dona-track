package donaciones;

import donaciones.controller.*;
import donaciones.db.TransaccionPorRequest;
import donaciones.domain.algoritmos.CompatibilidadSemantica;
import donaciones.domain.algoritmos.OrganizadorAsignaciones;
import donaciones.domain.algoritmos.PrioridadSubAtendidos;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.grpc.DonacionesServiceImpl;
import donaciones.grpc.LogisticaGrpcClient;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.PersonasAdministradorasRepository;
import donaciones.repository.NotificacionRepository;
import donaciones.repository.PersonasAdministradorasRepository;
import donaciones.repository.SugerenciaAsignacionRepository;
import donaciones.retrofit_client.LogisticaAPICalls;
import donaciones.retrofit_client.RetrofitConfig;
import donaciones.web.Router;
import donaciones.web.templates.JavalinHandlebars;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import donaciones.controller.*;

import java.io.IOException;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    // Desde IntelliJ se usa la URL del persistence.xml (localhost); en Docker, compose pasa DB_URL
    String dbUrl = System.getenv("DB_URL");
    if (dbUrl != null) {
      WithSimplePersistenceUnit.configure(properties -> properties.set("hibernate.connection.url", dbUrl));
    }

    PersonasAdministradorasRepository administradorasRepo = new PersonasAdministradorasRepository();
    DonacionRepository donacionesRepository = new DonacionRepository();
    RepositorioPersonas personasRepository = new RepositorioPersonas();
    EntidadBeneficiariaRepository entidadRepo = new EntidadBeneficiariaRepository();

    // Cliente gRPC hacia logistica-service
    LogisticaGrpcClient logisticaGrpcClient = new LogisticaGrpcClient(
        System.getenv().getOrDefault("LOGISTICA_GRPC_HOST", "localhost"), 9090);

    NotificacionRepository notificacionRepository = new NotificacionRepository();
    Notificador notificador = new Notificador(notificacionRepository);

    DonacionController controller = new DonacionController(donacionesRepository, personasRepository, administradorasRepo, notificador);
    DonanteController donanteController = new DonanteController(personasRepository);
    IntegracionLogisticaController integracionLogisticaController = new IntegracionLogisticaController();
    SugerenciaAsignacionRepository sugerenciasRepository = new SugerenciaAsignacionRepository();

    EntidadesBeneficiariasController entidadesController = new EntidadesBeneficiariasController(entidadRepo);
    TransaccionPorRequest transaccion = new TransaccionPorRequest();

    OrganizadorAsignaciones organizadorAsignaciones = new OrganizadorAsignaciones(List.of(new CompatibilidadSemantica(), new PrioridadSubAtendidos()));

    AsignacionesController asignacionesController = new AsignacionesController(
        donacionesRepository, entidadRepo, sugerenciasRepository, logisticaGrpcClient, notificador, organizadorAsignaciones);

    CamionesController camionesController = new CamionesController(logisticaGrpcClient, donacionesRepository);
    NotificacionesController notificacionesController = new NotificacionesController(notificacionRepository);
    PublicoController publicoController = new PublicoController();
    SesionController sesionController = new SesionController();

    // Servidor gRPC propio de donaciones-service (puerto 9091)
    try {
      Server gRpcServer = ServerBuilder.forPort(9091)
          .addService(new DonacionesServiceImpl(donacionesRepository))
          .build();
      gRpcServer.start();
      System.out.println("Servidor gRPC escuchando en puerto 9091...");

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Apagando servidor gRPC...");
        gRpcServer.shutdown();
      }));
    } catch (IOException e) {
      System.err.println("Error al iniciar el servidor gRPC: " + e.getMessage());
    }

    // Servidor web HTTP / Javalin (puerto 8081)
    Javalin app = Javalin.create(config -> {
      config.fileRenderer(new JavalinHandlebars());
      config.staticFiles.add(staticFiles -> {
        staticFiles.hostedPath = "/assets";
        staticFiles.directory = "/assets";
        staticFiles.location = Location.CLASSPATH;
      });
    })
        .before(ctx -> transaccion.abrir())
        .after(ctx -> transaccion.cerrar(ctx.statusCode()))
        .start(8081);

    new Router(controller, donanteController, entidadesController, asignacionesController, camionesController,
        notificacionesController, publicoController, sesionController).configure(app);
  }
}
