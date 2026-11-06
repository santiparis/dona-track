package donaciones.db;

import donaciones.domain.Bien;
import donaciones.domain.Donacion;
import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.EstadoBien;
import donaciones.domain.EstadoDonacion;
import donaciones.domain.Necesidad;
import donaciones.domain.Periodo;
import donaciones.domain.PersonaAdministradora;
import donaciones.domain.RenovacionPeriodica;
import donaciones.domain.SinRenovacion;
import donaciones.domain.Subcategoria;
import donaciones.domain.donante.Genero;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.PersonaJuridica;
import donaciones.domain.donante.RazonSocial;
import donaciones.domain.donante.TipoDoc;
import donaciones.domain.donante.Usuario;
import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.ContactoPorEmail;
import donaciones.domain.notificacion.ContactoPorWhatsApp;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Datos de prueba para la UI. Se cargan solo si la base no tiene personas. */
public class Seed implements WithSimplePersistenceUnit {

  public void cargarSiEstaVacia() {
    withTransaction(() -> {
      Long personas = createQuery("select count(p) from Persona p", Long.class).getSingleResult();
      if (personas == 0) {
        cargar();
      }
    });
  }

  private void cargar() {
    // Administradora
    Contacto correoLaura = new ContactoPorEmail("laura.admin@donatrack.org");
    persist(new PersonaAdministradora("Laura Méndez", List.of(correoLaura), correoLaura));

    // Donantes
    Contacto correoAna = new ContactoPorEmail("ana@mail.com");
    PersonaHumana ana = new PersonaHumana("Ana", "Pérez", 34, TipoDoc.DNI, "12345678", Genero.FEMENINO,
        "Av. Corrientes 1234, CABA",
        List.of(correoAna, new ContactoPorWhatsApp("+54 11 5555-5555")), correoAna,
        new Usuario("ana@mail.com", "ana123"));

    Contacto correoJuan = new ContactoPorEmail("juan.gomez@mail.com");
    PersonaHumana juan = new PersonaHumana("Juan", "Gómez", 52, TipoDoc.DNI, "20111222", Genero.MASCULINO,
        "Belgrano 450, Escobar",
        List.of(correoJuan), correoJuan,
        new Usuario("juan.gomez@mail.com", "juan123"));

    Contacto correoArcos = new ContactoPorEmail("contacto@empresa.com");
    PersonaJuridica arcos = new PersonaJuridica(TipoDoc.CUIT, "30-12345678-9", "Arcos Plateados S.A.",
        RazonSocial.EMPRESA, "Mueblería", new ArrayList<>(List.of(juan)),
        List.of(correoArcos, new ContactoPorWhatsApp("+54 11 4444-4444")), correoArcos,
        new Usuario("contacto@empresa.com", "arcos123"));

    Contacto correoPastas = new ContactoPorEmail("ventas@pastasdelsur.com");
    PersonaJuridica pastas = new PersonaJuridica(TipoDoc.CUIT, "30-87654321-0", "Pastas del Sur",
        RazonSocial.EMPRESA, "Alimentos", new ArrayList<>(),
        List.of(correoPastas), correoPastas,
        new Usuario("ventas@pastasdelsur.com", "pastas123"));

    persist(ana);
    persist(juan);
    persist(arcos);
    persist(pastas);

    // Entidades beneficiarias con sus necesidades
    Contacto correoEscuela = new ContactoPorEmail("direccion@escuelarural10.edu.ar");
    EntidadBeneficiaria escuela = new EntidadBeneficiaria("Escuela Rural N°10", "Ruta 5 km 32, San Andrés de Giles",
        "(02325) 44-1234",
        new ArrayList<>(List.of("direccion@escuelarural10.edu.ar", "cooperadora@escuelarural10.edu.ar")),
        List.of(correoEscuela), correoEscuela);
    escuela.registrarNecesidad(new Necesidad("Bancos y sillas tras la inundación", new SinRenovacion(),
        Map.of(Subcategoria.BANCOS, 30, Subcategoria.SILLAS, 30)));

    Contacto correoComedor = new ContactoPorEmail("contacto@escobarsonrisas.org");
    EntidadBeneficiaria comedor = new EntidadBeneficiaria("Comedor Escobar Sonrisas", "Rivadavia 980, Escobar",
        "(0348) 442-5566", new ArrayList<>(List.of("contacto@escobarsonrisas.org")),
        List.of(correoComedor), correoComedor);
    comedor.registrarNecesidad(new Necesidad("Fideos para el almuerzo",
        new RenovacionPeriodica(LocalDate.now(), Periodo.SEMANAL), Map.of(Subcategoria.FIDEOS, 100)));

    Contacto correoMerendero = new ContactoPorEmail("merenderomitai@gmail.com");
    EntidadBeneficiaria merendero = new EntidadBeneficiaria("Merendero Mitaí", "Calle 115 N°2340, Posadas, Misiones",
        "(0376) 445-7788", new ArrayList<>(List.of("merenderomitai@gmail.com")),
        List.of(correoMerendero), correoMerendero);
    merendero.registrarNecesidad(new Necesidad("Leche para la merienda",
        new RenovacionPeriodica(LocalDate.now(), Periodo.SEMANAL), Map.of(Subcategoria.LECHE, 60)));

    persist(escuela);
    persist(comedor);
    persist(merendero);

    // Donaciones en todos los estados
    LocalDate hoy = LocalDate.now();

    persist(new Donacion(pastas, new Bien(Subcategoria.FIDEOS, 100, "paquetes", "Fideos tirabuzón 500 g", null, null, null)));
    persist(new Donacion(pastas, new Bien(Subcategoria.LECHE, 30, "litros", "Leche entera larga vida", null, null, hoy.plusDays(3))));
    persist(new Donacion(ana, new Bien(Subcategoria.LECHE, 20, "litros", "Leche descremada", null, null, hoy.plusMonths(2))));
    persist(new Donacion(ana, new Bien(Subcategoria.CAMPERAS, 5, "unidades", "Camperas de abrigo talle M", null, EstadoBien.USADO, null)));

    Donacion mesa = new Donacion(arcos, new Bien(Subcategoria.MESAS, 1, "unidades", "Mesa de madera", null, EstadoBien.USADO, null));
    mesa.asignarA(escuela);
    persist(mesa);

    Donacion bancos = new Donacion(arcos, new Bien(Subcategoria.BANCOS, 10, "unidades", "Bancos escolares", null, EstadoBien.NUEVO, null));
    bancos.asignarA(escuela);
    bancos.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    persist(bancos);

    Donacion sillas = new Donacion(arcos, new Bien(Subcategoria.SILLAS, 6, "unidades", "Sillas de madera", null, EstadoBien.USADO, null));
    sillas.asignarA(escuela);
    sillas.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    sillas.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    persist(sillas);

    Donacion fideosEntregados = new Donacion(ana, new Bien(Subcategoria.FIDEOS, 40, "paquetes", "Fideos spaghetti", null, null, null));
    fideosEntregados.asignarA(comedor);
    fideosEntregados.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    fideosEntregados.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    fideosEntregados.cambiarEstado(EstadoDonacion.ENTREGADA, null);
    persist(fideosEntregados);

    Donacion lecheFallida = new Donacion(juan, new Bien(Subcategoria.LECHE, 24, "litros", "Leche entera", null, null, hoy.plusDays(20)));
    lecheFallida.asignarA(merendero);
    lecheFallida.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    lecheFallida.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    lecheFallida.cambiarEstado(EstadoDonacion.ENTREGA_FALLIDA, "Tocamos timbre pero nadie respondió");
    persist(lecheFallida);

    Donacion lecheVencida = new Donacion(juan, new Bien(Subcategoria.LECHE, 12, "litros", "Leche chocolatada", null, null, hoy.minusDays(2)));
    lecheVencida.cambiarEstado(EstadoDonacion.VENCIDA, null);
    persist(lecheVencida);
  }
}
