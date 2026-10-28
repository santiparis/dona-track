package donaciones;

import donaciones.controller.*;
import donaciones.domain.algoritmos.CompatibilidadSemantica;
import donaciones.domain.algoritmos.OrganizadorAsignaciones;
import donaciones.domain.algoritmos.PrioridadSubAtendidos;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.PersonasAdministradorasRepository;
import donaciones.repository.NotificacionRepository;
import donaciones.repository.SugerenciaAsignacionRepository;
import donaciones.retrofit_client.LogisticaAPICalls;
import donaciones.retrofit_client.RetrofitConfig;
import donaciones.web.Router;
import donaciones.web.templates.JavalinHandlebars;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import donaciones.controller.*;

import java.util.List;

public class Main {
  public static void main(String[] args) {

    PersonasAdministradorasRepository administradorasRepo = new PersonasAdministradorasRepository();
    DonacionRepository donacionesRepository = new DonacionRepository();
    RepositorioPersonas personasRepository = new RepositorioPersonas();
    EntidadBeneficiariaRepository entidadRepo = new EntidadBeneficiariaRepository();

    RetrofitConfig retrofitConfig = new RetrofitConfig();
    LogisticaAPICalls logisticaAPICalls = retrofitConfig.logisticaAPICalls();

    Notificador notificador = new Notificador(new NotificacionRepository());

    DonacionController controller = new DonacionController(donacionesRepository, personasRepository, administradorasRepo, notificador);
    DonanteController donanteController = new DonanteController(personasRepository);
    IntegracionLogisticaController integracionLogisticaController = new IntegracionLogisticaController();
    SugerenciaAsignacionRepository sugerenciasRepository = new SugerenciaAsignacionRepository();

    EntidadesBeneficiariasController entidadesController = new EntidadesBeneficiariasController(entidadRepo);

    OrganizadorAsignaciones organizadorAsignaciones = new OrganizadorAsignaciones(List.of(new CompatibilidadSemantica(), new PrioridadSubAtendidos()));

    AsignacionesController asignacionesController = new AsignacionesController(
        donacionesRepository, entidadRepo, sugerenciasRepository, logisticaAPICalls, notificador, organizadorAsignaciones);

    CamionesController camionesController = new CamionesController(logisticaAPICalls);


    Javalin app = Javalin.create(config -> {
      config.fileRenderer(new JavalinHandlebars());
      config.staticFiles.add(staticFiles -> {
        staticFiles.hostedPath = "/assets";
        staticFiles.directory = "/assets";
        staticFiles.location = Location.CLASSPATH;
      });
    }).start(8081);

    new Router(controller, donanteController, entidadesController, asignacionesController, camionesController).configure(app);
  }
}
