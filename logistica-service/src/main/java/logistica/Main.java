package logistica;

import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.javalin.Javalin;
import logistica.controller.CamionesController;
import logistica.controller.DonacionesAPIController;
import logistica.controller.EntregasController;
import logistica.controller.PlanificadorController;
import logistica.controller.RutasController;
import logistica.db.TransaccionPorRequest;
import logistica.grpc.DonacionesGrpcClient;
import logistica.grpc.LogisticaServiceImpl;
import logistica.notificacion.NotificadorEntregas;
import logistica.planificacion.ClientePlanificador;
import logistica.repository.CamionesRepository;
import logistica.repository.DonacionesRepository;
import logistica.repository.RutasRepository;
import logistica.repository.SeedCamiones;
import logistica.retrofit_client.RetrofitConfig;

import java.io.IOException;

public class Main {
  public static void main(String[] args) {
    // Desde IntelliJ se usa la URL del persistence.xml (localhost); en Docker, compose pasa DB_URL
    String dbUrl = System.getenv("DB_URL");
    if (dbUrl != null) {
      WithSimplePersistenceUnit.configure(properties -> properties.set("hibernate.connection.url", dbUrl));
    }

    var repositorioCamiones = new CamionesRepository();
    var repositorioDonaciones = new DonacionesRepository();
    var repositorioRutas = new RutasRepository();
    var retrofitConfig = new RetrofitConfig();
    var transaccion = new TransaccionPorRequest();

    // 1. Cliente gRPC hacia donaciones-service (puerto 9091)
    var donacionesGrpcClient = new DonacionesGrpcClient(
        System.getenv().getOrDefault("DONACIONES_GRPC_HOST", "localhost"), 9091);

    var notificadorEntregas = new NotificadorEntregas(donacionesGrpcClient);
    var donacionesController = new DonacionesAPIController(repositorioDonaciones);
    var rutasController = new RutasController(repositorioRutas, notificadorEntregas);
    var entregasController = new EntregasController(repositorioRutas, notificadorEntregas);
    var camionesController = new CamionesController(repositorioCamiones);

    var clientePlanificador = new ClientePlanificador(retrofitConfig.planificadorAPICalls());
    var planificadorController = new PlanificadorController(repositorioCamiones, repositorioRutas, repositorioDonaciones, clientePlanificador, notificadorEntregas);

    // 2. Servidor gRPC propio de logistica-service (puerto 9090)
    try {
      Server gRpcServer = ServerBuilder.forPort(9090)
          .addService(new LogisticaServiceImpl(repositorioRutas))
          .build();
      gRpcServer.start();
      System.out.println("Servidor gRPC de logistica-service escuchando en el puerto 9090...");

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Apagando servidor gRPC de logistica-service...");
        gRpcServer.shutdown();
      }));
    } catch (IOException e) {
      System.err.println("Error al iniciar el servidor gRPC de logistica-service: " + e.getMessage());
    }

    // Carga de flota inicial
    transaccion.withTransaction(() -> {
      if (repositorioCamiones.obtenerTodos().isEmpty()) {
        var camiones = SeedCamiones.camiones();
        repositorioCamiones.agregarTodos(camiones);
        SeedCamiones.rutas(camiones).forEach(repositorioRutas::agregar);
      }
    });

    // 3. Servidor Web Javalin/HTTP (puerto 7070)
    var app = Javalin.create(config -> {
      // una transaccion por pedido: se abre antes del handler y se cierra despues
      config.routes.before(ctx -> transaccion.abrir());
      config.routes.after(ctx -> transaccion.cerrar(ctx.statusCode()));

      //health
      config.routes.get("/", ctx -> ctx.result("logistica-service OK"));

      // Gestión de flota de camiones
      config.routes.get("/api/camiones", camionesController::getCamiones);
      config.routes.get("/api/camiones/{id}", camionesController::getCamion);
      config.routes.post("/api/camiones", camionesController::postCamiones);
      config.routes.delete("/api/camiones/{id}", camionesController::deleteCamion);
      config.routes.put("/api/camiones/{id}", camionesController::putCamion);

      // Actualización de la localización de un camión
      config.routes.patch("/api/camiones/{patente}/localizacion", camionesController::actualizarLocalizacion);

      // endpoints consumidos por el microservicio donaciones
      config.routes.post("/api/donaciones", donacionesController::obtenerDonaciones);

      //dispara la planificacion (lo llama el cron via HTTP, o a demanda)
      config.routes.post("/api/planificaciones", planificadorController::planificar);

      //endpoints consumidos por el planificador externo (callback con las rutas armadas)
      config.routes.post("/api/rutas", planificadorController::obtenerRutas);

      // listado general de rutas planificadas
      config.routes.get("/api/rutas", rutasController::obtenerRutas);

      //lectura de una ruta (app del chofer: consulta el recorrido antes de iniciar)
      config.routes.get("/api/rutas/{id}", rutasController::obtenerRuta);

      //endpoint que usa el chofer para iniciar su ruta
      config.routes.post("/api/rutas/{id}/inicio", rutasController::iniciarRuta);

      //lectura de una entrega (para que un objeto consulte el estado)
      config.routes.get("/api/entregas/{id}", entregasController::obtenerEntrega);

      //endpoints que usa la entidad beneficiaria para confirmar/rechazar la recepcion
      config.routes.post("/api/entregas/{id}/confirmacion", entregasController::confirmar);
      config.routes.post("/api/entregas/{id}/rechazo", entregasController::marcarNoRecibida);
      config.routes.post("/api/entregas/{id}/reingreso", entregasController::reingresarADeposito);

    }).start(7070);
  }
}