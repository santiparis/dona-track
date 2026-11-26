package donaciones.db;

import donaciones.domain.Bien;
import donaciones.domain.Donacion;
import donaciones.domain.EntidadBeneficiaria;
import donaciones.domain.EstadoBien;
import donaciones.domain.EstadoDonacion;
import donaciones.domain.Necesidad;
import donaciones.domain.Periodo;
import donaciones.domain.PersonaAdministradora;
import donaciones.domain.Subcategoria;
import donaciones.domain.algoritmos.SugerenciaAsignacion;
import donaciones.domain.donante.Genero;
import donaciones.domain.donante.PersonaHumana;
import donaciones.domain.donante.PersonaJuridica;
import donaciones.domain.donante.RazonSocial;
import donaciones.domain.donante.Rol;
import donaciones.domain.donante.TipoDoc;
import donaciones.domain.donante.Usuario;
import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.ContactoPorEmail;
import donaciones.domain.notificacion.ContactoPorWhatsApp;
import donaciones.domain.notificacion.Notificacion;
import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    PersonaAdministradora laura = new PersonaAdministradora("Laura Méndez", List.of(correoLaura), correoLaura,
        new Usuario("laura.admin@donatrack.org", "laura123", Rol.ADMINISTRADORA));
    persist(laura);

    // Donantes
    Contacto correoAna = new ContactoPorEmail("ana@mail.com");
    PersonaHumana ana = new PersonaHumana("Ana", "Pérez", 34, TipoDoc.DNI, "12345678", Genero.FEMENINO,
        "Av. Corrientes 1234, CABA",
        List.of(correoAna, new ContactoPorWhatsApp("+54 11 5555-5555")), correoAna,
        new Usuario("ana@mail.com", "ana123", Rol.DONANTE));

    Contacto correoJuan = new ContactoPorEmail("juan.gomez@mail.com");
    PersonaHumana juan = new PersonaHumana("Juan", "Gómez", 52, TipoDoc.DNI, "20111222", Genero.MASCULINO,
        "Belgrano 450, Escobar",
        List.of(correoJuan), correoJuan,
        new Usuario("juan.gomez@mail.com", "juan123", Rol.DONANTE));

    Contacto correoArcos = new ContactoPorEmail("contacto@empresa.com");
    PersonaJuridica arcos = new PersonaJuridica(TipoDoc.CUIT, "30-12345678-9", "Arcos Plateados S.A.",
        RazonSocial.EMPRESA, "Mueblería", new ArrayList<>(List.of(juan)),
        List.of(correoArcos, new ContactoPorWhatsApp("+54 11 4444-4444")), correoArcos,
        new Usuario("contacto@empresa.com", "arcos123", Rol.DONANTE));

    Contacto correoPastas = new ContactoPorEmail("ventas@pastasdelsur.com");
    PersonaJuridica pastas = new PersonaJuridica(TipoDoc.CUIT, "30-87654321-0", "Pastas del Sur",
        RazonSocial.EMPRESA, "Alimentos", new ArrayList<>(),
        List.of(correoPastas), correoPastas,
        new Usuario("ventas@pastasdelsur.com", "pastas123", Rol.DONANTE));

    persist(ana);
    persist(juan);
    persist(arcos);
    persist(pastas);

    // Donante con 25 días sin actividad: lo avisa el cron NotificarPersonasInactivas
    Contacto correoCarlos = new ContactoPorEmail("juanignaciopereyra01@gmail.com");
    PersonaHumana carlos = new PersonaHumana("Carlos Inactivo", "Pérez", 45, TipoDoc.DNI, "11111111",
        Genero.MASCULINO, "Av. Siempre Viva 742", List.of(correoCarlos), correoCarlos, null);
    carlos.setUltimaInteraccion(LocalDateTime.now().minusDays(25));
    persist(carlos);

    // Entidades beneficiarias con sus necesidades, una por subcategoría como en la UI.
    // Lo suplido se carga con registrarSuplido, como si ya hubieran recibido parte.
    LocalDate vencimientoLeche = LocalDate.now().plusMonths(1);

    Contacto correoEscuela = new ContactoPorEmail("direccion@escuelarural10.edu.ar");
    EntidadBeneficiaria escuela = new EntidadBeneficiaria("Escuela Rural N°10", "Ruta 5 km 32, San Andrés de Giles",
        "(02325) 44-1234",
        new ArrayList<>(List.of("direccion@escuelarural10.edu.ar", "cooperadora@escuelarural10.edu.ar")),
        List.of(correoEscuela), correoEscuela,
        new Usuario("direccion@escuelarural10.edu.ar", "escuela123", Rol.ENTIDAD));
    Necesidad bancosEscuela = new Necesidad("Reposición tras inundación", false, LocalDate.now(), null, Map.of(Subcategoria.BANCOS, 30));
    bancosEscuela.registrarSuplido(new Bien(Subcategoria.BANCOS, 10, "unidades", null, null, EstadoBien.NUEVO, null));
    Necesidad sillasEscuela = new Necesidad("Reposición tras inundación", false, LocalDate.now(), null, Map.of(Subcategoria.SILLAS, 30));
    sillasEscuela.registrarSuplido(new Bien(Subcategoria.SILLAS, 6, "unidades", null, null, EstadoBien.USADO, null));
    Necesidad mesasEscuela = new Necesidad(null, false, LocalDate.now(), null, Map.of(Subcategoria.MESAS, 10));
    Necesidad lecheEscuela = new Necesidad("Desayuno escolar",
        true, LocalDate.now(), Periodo.MENSUAL, Map.of(Subcategoria.LECHE, 50));
    lecheEscuela.registrarSuplido(new Bien(Subcategoria.LECHE, 40, "litros", null, null, null, vencimientoLeche));
    Necesidad camperasEscuela = new Necesidad(null, false, LocalDate.now(), null, Map.of(Subcategoria.CAMPERAS, 40));
    camperasEscuela.registrarSuplido(new Bien(Subcategoria.CAMPERAS, 5, "unidades", null, null, EstadoBien.USADO, null));
    escuela.registrarNecesidad(bancosEscuela);
    escuela.registrarNecesidad(sillasEscuela);
    escuela.registrarNecesidad(mesasEscuela);
    escuela.registrarNecesidad(lecheEscuela);
    escuela.registrarNecesidad(camperasEscuela);

    Contacto correoComedor = new ContactoPorEmail("escobarsonrisas@gmail.com");
    EntidadBeneficiaria comedor = new EntidadBeneficiaria("Comedor Escobar Sonrisas", "Av. Tapia de Cruz 1200, Escobar",
        "(0348) 442-5678", new ArrayList<>(List.of("escobarsonrisas@gmail.com")),
        List.of(correoComedor), correoComedor);
    Necesidad fideosComedor = new Necesidad(null,
        true, LocalDate.now(), Periodo.SEMANAL, Map.of(Subcategoria.FIDEOS, 100));
    fideosComedor.registrarSuplido(new Bien(Subcategoria.FIDEOS, 60, "kg", null, null, null, null));
    comedor.registrarNecesidad(fideosComedor);

    Contacto correoTutorias = new ContactoPorEmail("tutoriasbarrionorte@gmail.com");
    EntidadBeneficiaria tutorias = new EntidadBeneficiaria("Espacio de Tutorías Barrio Norte", "Calle 14 N°560, La Plata",
        "(0221) 455-0000", new ArrayList<>(List.of("tutoriasbarrionorte@gmail.com")),
        List.of(correoTutorias), correoTutorias);
    tutorias.registrarNecesidad(new Necesidad(null, false, LocalDate.now(), null, Map.of(Subcategoria.MESAS, 5)));

    // Las tres que aparecen en los rankings de Elegir entidad final
    Contacto correoBarrioSur = new ContactoPorEmail("comedorbarriosur@gmail.com");
    EntidadBeneficiaria barrioSur = new EntidadBeneficiaria("Comedor Barrio Sur", "Av. Mitre 2450, Avellaneda",
        "(011) 4201-3344", new ArrayList<>(List.of("comedorbarriosur@gmail.com")),
        List.of(correoBarrioSur), correoBarrioSur);
    barrioSur.registrarNecesidad(new Necesidad(null, true, LocalDate.now(), Periodo.SEMANAL, Map.of(Subcategoria.FIDEOS, 50)));

    Contacto correoSanJose = new ContactoPorEmail("hogarsanjose@gmail.com");
    EntidadBeneficiaria sanJose = new EntidadBeneficiaria("Hogar San José", "Belgrano 780, Luján",
        "(02323) 42-5566", new ArrayList<>(List.of("hogarsanjose@gmail.com")),
        List.of(correoSanJose), correoSanJose);
    sanJose.registrarNecesidad(new Necesidad(null, false, LocalDate.now(), null, Map.of(Subcategoria.CAMPERAS, 20)));

    Contacto correoLosPibes = new ContactoPorEmail("merenderolospibes@gmail.com");
    EntidadBeneficiaria losPibes = new EntidadBeneficiaria("Merendero Los Pibes", "Calle 9 N°1120, Florencio Varela",
        "(011) 4255-7788", new ArrayList<>(List.of("merenderolospibes@gmail.com")),
        List.of(correoLosPibes), correoLosPibes);
    losPibes.registrarNecesidad(new Necesidad(null, true, LocalDate.now(), Periodo.MENSUAL, Map.of(Subcategoria.FIDEOS, 100)));

    persist(escuela);
    persist(comedor);
    persist(tutorias);
    persist(barrioSur);
    persist(sanJose);
    persist(losPibes);

    // Donaciones en todos los estados
    LocalDate hoy = LocalDate.now();

    Donacion fideosPastas = new Donacion(pastas, new Bien(Subcategoria.FIDEOS, 100, "kg", "Fideos tirabuzón 500 g", null, null, null));
    persist(fideosPastas);
    persist(new Donacion(pastas, new Bien(Subcategoria.LECHE, 30, "litros", "Leche entera larga vida", null, null, hoy.plusDays(3))));
    persist(new Donacion(ana, new Bien(Subcategoria.LECHE, 20, "litros", "Leche descremada", null, null, hoy.plusMonths(2))));
    Donacion sillasArcos = new Donacion(arcos, new Bien(Subcategoria.SILLAS, 6, "unidades", "Sillas plásticas", null, EstadoBien.USADO, null));
    persist(sillasArcos);
    Donacion camperasAna = new Donacion(ana, new Bien(Subcategoria.CAMPERAS, 12, "unidades", "Camperas de abrigo talle M", null, EstadoBien.USADO, null));
    persist(camperasAna);

    // Sugerencias de asignación de las donaciones en depósito, con el ranking de cada algoritmo
    Map<String, List<EntidadBeneficiaria>> rankingFideos = new LinkedHashMap<>();
    rankingFideos.put("Compatibilidad Semantica", List.of(comedor, barrioSur, sanJose, losPibes));
    rankingFideos.put("Prioridad Subatendidos", List.of(losPibes, escuela, comedor, tutorias));
    persist(new SugerenciaAsignacion(fideosPastas.getId(), fideosPastas, new ArrayList<>(List.of(comedor, losPibes)), rankingFideos));

    Map<String, List<EntidadBeneficiaria>> rankingSillas = new LinkedHashMap<>();
    rankingSillas.put("Compatibilidad Semantica", List.of(escuela, tutorias, sanJose));
    rankingSillas.put("Prioridad Subatendidos", List.of(losPibes, escuela, barrioSur));
    persist(new SugerenciaAsignacion(sillasArcos.getId(), sillasArcos, new ArrayList<>(List.of(escuela)), rankingSillas));

    Map<String, List<EntidadBeneficiaria>> rankingCamperas = new LinkedHashMap<>();
    rankingCamperas.put("Compatibilidad Semantica", List.of(barrioSur, sanJose));
    rankingCamperas.put("Prioridad Subatendidos", List.of(escuela, tutorias));
    persist(new SugerenciaAsignacion(camperasAna.getId(), camperasAna, new ArrayList<>(), rankingCamperas));

    Donacion mesa = new Donacion(arcos, new Bien(Subcategoria.MESAS, 1, "unidades", "Mesa de madera", null, EstadoBien.USADO, null));
    mesa.asignarA(escuela);
    persist(mesa);

    Donacion bancos = new Donacion(arcos, new Bien(Subcategoria.BANCOS, 10, "unidades", "Bancos escolares", null, EstadoBien.NUEVO, null));
    bancos.asignarA(escuela);
    bancos.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    persist(bancos);

    // Las dos en traslado de Ana coinciden con las rutas del seed de logística (SeedCamiones)
    Donacion sillas = new Donacion(ana, new Bien(Subcategoria.SILLAS, 6, "unidades", "Sillas de madera", null, EstadoBien.USADO, null));
    sillas.asignarA(escuela);
    sillas.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    sillas.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    persist(sillas);

    Donacion fideosEnTraslado = new Donacion(ana, new Bien(Subcategoria.FIDEOS, 100, "kg", "Fideos mostachol 500 g", null, null, null));
    fideosEnTraslado.asignarA(comedor);
    fideosEnTraslado.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    fideosEnTraslado.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    persist(fideosEnTraslado);

    Donacion fideosEntregados = new Donacion(ana, new Bien(Subcategoria.FIDEOS, 40, "kg", "Fideos spaghetti", null, null, null));
    fideosEntregados.asignarA(comedor);
    fideosEntregados.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    fideosEntregados.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    fideosEntregados.cambiarEstado(EstadoDonacion.ENTREGADA, null);
    persist(fideosEntregados);

    Donacion lecheFallida = new Donacion(juan, new Bien(Subcategoria.LECHE, 24, "litros", "Leche entera", null, null, hoy.plusDays(20)));
    lecheFallida.asignarA(comedor);
    lecheFallida.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    lecheFallida.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    lecheFallida.cambiarEstado(EstadoDonacion.ENTREGA_FALLIDA, "Tocamos timbre pero nadie respondió");
    persist(lecheFallida);

    Donacion lecheVencida = new Donacion(juan, new Bien(Subcategoria.LECHE, 12, "litros", "Leche chocolatada", null, null, hoy.minusDays(2)));
    lecheVencida.cambiarEstado(EstadoDonacion.VENCIDA, null);
    persist(lecheVencida);

    // Notificaciones de la bandeja, con el mismo texto que arma el Notificador: en cada perfil una ya leída y el resto sin leer
    String firma = "\n\n\nGracias,\nde parte de todo el equipo de DonaTrack";
    String urlPublica = Objects.requireNonNullElse(System.getenv("DONACIONES_URL_PUBLICA"), "http://localhost:8081");
    String sillasDeAna = "Donación: 6 unidades de Sillas (Sillas de madera)\nDonada por: Ana\nPara: Escuela Rural N°10\nDirección: Ruta 5 km 32, San Andrés de Giles";
    String fideosDeAna = "Donación: 40 kg de Fideos (Fideos spaghetti)\nDonada por: Ana\nPara: Comedor Escobar Sonrisas\nDirección: Av. Tapia de Cruz 1200, Escobar";
    String lecheDeJuan = "Donación: 24 litros de Leche (Leche entera)\nDonada por: Juan\nPara: Comedor Escobar Sonrisas\nDirección: Av. Tapia de Cruz 1200, Escobar";
    String mesaDeArcos = "Donación: 1 unidades de Mesas (Mesa de madera)\nDonada por: Arcos Plateados S.A.\nPara: Escuela Rural N°10\nDirección: Ruta 5 km 32, San Andrés de Giles";
    String bancosDeArcos = "Donación: 10 unidades de Bancos (Bancos escolares)\nDonada por: Arcos Plateados S.A.\nPara: Escuela Rural N°10\nDirección: Ruta 5 km 32, San Andrés de Giles";

    Notificacion sillasAsignadasAna = new Notificacion(ana, "Hola, Ana:\n\n¡Tu donación ya tiene destino!\n\n" + sillasDeAna
        + "\n\nTe vamos a avisar cuando el camión salga a entregarla." + firma);
    sillasAsignadasAna.marcarComoLeida();
    persist(sillasAsignadasAna);
    persist(new Notificacion(ana, "Hola, Ana:\n\n¡La donación llegó a destino!\n\n" + fideosDeAna
        + "\nComprobante: ENT-1, 22/11/2026 15:40, AB123CD\n\n¡Gracias por hacer posible esta ayuda!" + firma));
    persist(new Notificacion(ana, "Hola, Ana:\n\n¡La donación está en camino!\n\n" + sillasDeAna
        + "\n\nSeguí el recorrido del camión en tiempo real:\n" + urlPublica + "/donante/seguimiento" + firma));

    Notificacion lecheAsignadaJuan = new Notificacion(juan, "Hola, Juan:\n\n¡Tu donación ya tiene destino!\n\n" + lecheDeJuan
        + "\n\nTe vamos a avisar cuando el camión salga a entregarla." + firma);
    lecheAsignadaJuan.marcarComoLeida();
    persist(lecheAsignadaJuan);
    persist(new Notificacion(juan, "Hola, Juan:\n\nNo pudimos completar una entrega.\n\n" + lecheDeJuan
        + "\nMotivo: Tocamos timbre pero nadie respondió\n\nLa donación vuelve al depósito para volver a entregarla." + firma));

    Notificacion mesaAsignadaArcos = new Notificacion(arcos, "Hola, Arcos Plateados S.A.:\n\n¡Tu donación ya tiene destino!\n\n" + mesaDeArcos
        + "\n\nTe vamos a avisar cuando el camión salga a entregarla." + firma);
    mesaAsignadaArcos.marcarComoLeida();
    persist(mesaAsignadaArcos);
    persist(new Notificacion(arcos, "Hola, Arcos Plateados S.A.:\n\n¡Tu donación ya tiene destino!\n\n" + bancosDeArcos
        + "\n\nTe vamos a avisar cuando el camión salga a entregarla." + firma));

    Notificacion bienvenidaPastas = new Notificacion(pastas, "Hola, Pastas del Sur:\n\n¡Te damos la bienvenida a DonaTrack!\n\n"
        + "Para tu primer acceso, ingresá con estos datos:\nUsuario: \"ventas@pastasdelsur.com\"\nContraseña provisoria: \"pastas123\"" + firma);
    bienvenidaPastas.marcarComoLeida();
    persist(bienvenidaPastas);
    persist(new Notificacion(pastas, "Hola, Pastas del Sur:\n\nHace más de 20 días que no te vemos por DonaTrack.\n\n"
        + "Hay entidades que siguen necesitando ayuda: te invitamos a hacer una nueva donación en la plataforma." + firma));

    Notificacion mesaAsignadaEscuela = new Notificacion(escuela, "Hola, Escuela Rural N°10:\n\n¡Les asignamos una nueva donación!\n\n" + mesaDeArcos
        + "\n\nLes vamos a avisar cuando el camión salga a entregarla." + firma);
    mesaAsignadaEscuela.marcarComoLeida();
    persist(mesaAsignadaEscuela);
    persist(new Notificacion(escuela, "Hola, Escuela Rural N°10:\n\n¡Les asignamos una nueva donación!\n\n" + bancosDeArcos
        + "\n\nLes vamos a avisar cuando el camión salga a entregarla." + firma));
    persist(new Notificacion(escuela, "Hola, Escuela Rural N°10:\n\n¡La donación está en camino!\n\n" + sillasDeAna
        + "\n\nSeguí el recorrido del camión en tiempo real:\n" + urlPublica + "/entidad/seguimiento" + firma));

    // Un intento anterior de entregar las sillas de Ana, que después se volvieron a despachar
    Notificacion sillasFallidaLaura = new Notificacion(laura, "Hola, Laura Méndez:\n\nNo pudimos completar una entrega.\n\n" + sillasDeAna
        + "\nMotivo: No había nadie para recibirla\n\nLa donación vuelve al depósito para volver a entregarla." + firma);
    sillasFallidaLaura.marcarComoLeida();
    persist(sillasFallidaLaura);
    persist(new Notificacion(laura, "Hola, Laura Méndez:\n\nNo pudimos completar una entrega.\n\n" + lecheDeJuan
        + "\nMotivo: Tocamos timbre pero nadie respondió\n\nLa donación vuelve al depósito para volver a entregarla." + firma));

    // Entidades y entregas del mapa público de Donaciones entregadas (DatosMock.donacionesEntregadas).
    // Van al final para no cambiar los ids de las donaciones 8 y 9 que usa el seed de logística
    Contacto correoMitai = new ContactoPorEmail("merenderomitai@gmail.com");
    EntidadBeneficiaria mitai = new EntidadBeneficiaria("Merendero Mitaí", "Av. Uruguay 3150, Posadas, Misiones",
        "(0376) 443-2211", new ArrayList<>(List.of("merenderomitai@gmail.com")),
        List.of(correoMitai), correoMitai);
    mitai.registrarNecesidad(new Necesidad(null, true, LocalDate.now(), Periodo.MENSUAL, Map.of(Subcategoria.LECHE, 100)));

    Contacto correoBariloche = new ContactoPorEmail("hogarescuelabariloche@gmail.com");
    EntidadBeneficiaria bariloche = new EntidadBeneficiaria("Hogar Escuela Bariloche", "Onelli 1450, San Carlos de Bariloche, Río Negro",
        "(0294) 442-8899", new ArrayList<>(List.of("hogarescuelabariloche@gmail.com")),
        List.of(correoBariloche), correoBariloche);
    bariloche.registrarNecesidad(new Necesidad("Invierno", false, LocalDate.now(), null, Map.of(Subcategoria.CAMPERAS, 60)));

    Contacto correoChanguitos = new ContactoPorEmail("comedorloschanguitos@gmail.com");
    EntidadBeneficiaria changuitos = new EntidadBeneficiaria("Comedor Los Changuitos", "Pasaje Güemes 210, Salta",
        "(0387) 421-5577", new ArrayList<>(List.of("comedorloschanguitos@gmail.com")),
        List.of(correoChanguitos), correoChanguitos);
    changuitos.registrarNecesidad(new Necesidad(null, true, LocalDate.now(), Periodo.SEMANAL, Map.of(Subcategoria.FIDEOS, 80)));

    persist(mitai);
    persist(bariloche);
    persist(changuitos);

    Donacion bancosEntregados = new Donacion(arcos, new Bien(Subcategoria.BANCOS, 30, "unidades", "Bancos escolares", null, EstadoBien.NUEVO, null));
    bancosEntregados.asignarA(escuela);
    bancosEntregados.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    bancosEntregados.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    bancosEntregados.cambiarEstado(EstadoDonacion.ENTREGADA, null);
    persist(bancosEntregados);

    Donacion sillasEntregadas = new Donacion(arcos, new Bien(Subcategoria.SILLAS, 30, "unidades", "Sillas escolares", null, EstadoBien.NUEVO, null));
    sillasEntregadas.asignarA(escuela);
    sillasEntregadas.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    sillasEntregadas.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    sillasEntregadas.cambiarEstado(EstadoDonacion.ENTREGADA, null);
    persist(sillasEntregadas);

    Donacion lecheEntregadaComedor = new Donacion(pastas, new Bien(Subcategoria.LECHE, 50, "litros", "Leche entera larga vida", null, null, hoy.plusMonths(1)));
    lecheEntregadaComedor.asignarA(comedor);
    lecheEntregadaComedor.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    lecheEntregadaComedor.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    lecheEntregadaComedor.cambiarEstado(EstadoDonacion.ENTREGADA, null);
    persist(lecheEntregadaComedor);

    Donacion lecheEntregadaMitai = new Donacion(juan, new Bien(Subcategoria.LECHE, 60, "litros", "Leche entera", null, null, hoy.plusMonths(1)));
    lecheEntregadaMitai.asignarA(mitai);
    lecheEntregadaMitai.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    lecheEntregadaMitai.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    lecheEntregadaMitai.cambiarEstado(EstadoDonacion.ENTREGADA, null);
    persist(lecheEntregadaMitai);

    Donacion camperasEntregadas = new Donacion(ana, new Bien(Subcategoria.CAMPERAS, 40, "unidades", "Camperas de abrigo", null, EstadoBien.USADO, null));
    camperasEntregadas.asignarA(bariloche);
    camperasEntregadas.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    camperasEntregadas.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    camperasEntregadas.cambiarEstado(EstadoDonacion.ENTREGADA, null);
    persist(camperasEntregadas);

    Donacion fideosEntregadosChanguitos = new Donacion(pastas, new Bien(Subcategoria.FIDEOS, 80, "kg", "Fideos guiseros 500 g", null, null, null));
    fideosEntregadosChanguitos.asignarA(changuitos);
    fideosEntregadosChanguitos.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    fideosEntregadosChanguitos.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    fideosEntregadosChanguitos.cambiarEstado(EstadoDonacion.ENTREGADA, null);
    persist(fideosEntregadosChanguitos);

    // Segunda en traslado para la Escuela (id 19, también en SeedCamiones): una se confirma y la otra se marca no recibida
    Donacion camperasEnTraslado = new Donacion(juan, new Bien(Subcategoria.CAMPERAS, 10, "unidades", "Camperas de abrigo talle S", null, EstadoBien.USADO, null));
    camperasEnTraslado.asignarA(escuela);
    camperasEnTraslado.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, null);
    camperasEnTraslado.cambiarEstado(EstadoDonacion.EN_TRASLADO, null);
    persist(camperasEnTraslado);
  }
}
