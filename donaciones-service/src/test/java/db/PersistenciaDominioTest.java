package db;

import donaciones.domain.*;
import donaciones.domain.donante.*;
import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.ContactoPorEmail;
import donaciones.domain.notificacion.ContactoPorSMS;
import donaciones.domain.notificacion.EstadoNotificacion;
import donaciones.domain.notificacion.Notificacion;
import donaciones.domain.donante.RepositorioPersonas;
import donaciones.repository.DonacionRepository;
import io.github.flbulgarelli.jpa.extras.test.SimplePersistenceTest;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración de los mapeos JPA. SimplePersistenceTest abre una
 * transacción por test y la revierte luego; la unidad configurada usa HSQLDB en memoria.
 */
class PersistenciaDominioTest implements SimplePersistenceTest {

    @Test
    void laUnidadDePersistenciaUtilizaHsqldbEnMemoria() {
        entityManager().unwrap(Session.class).doWork(connection -> {
            assertEquals("HSQL Database Engine", connection.getMetaData().getDatabaseProductName());
            assertTrue(connection.getMetaData().getURL().startsWith("jdbc:hsqldb:mem:"));
        });
    }

    @Test
    void persisteLaJerarquiaPersonaEnTablasUnidasYConservaElSubtipo() {
        EntityManager em = entityManager();
        Contacto contacto = new ContactoPorEmail("ana@example.org");
        Usuario usuario = new Usuario("ana", "secreto", Rol.DONANTE);
        PersonaHumana humana = new PersonaHumana(
                "Ana", "Pérez", 30, TipoDoc.DNI, "12345678", Genero.FEMENINO,
                "Calle 1", List.of(contacto), contacto, usuario);
        PersonaJuridica juridica = new PersonaJuridica(
                TipoDoc.CUIT, "30-12345678-9", "Fundación X", RazonSocial.ONG,
                "Asistencia social", List.of(), List.of(contacto), contacto, null);

        em.persist(humana);
        em.persist(juridica);
        em.flush();
        Long humanaId = humana.getId();
        Long juridicaId = juridica.getId();
        Number filasHumanas = (Number) em.createNativeQuery(
                        "select count(*) from persona_humana where persona_id = ?")
                .setParameter(1, humanaId)
                .getSingleResult();
        Number filasJuridicas = (Number) em.createNativeQuery(
                        "select count(*) from persona_juridica where persona_id = ?")
                .setParameter(1, juridicaId)
                .getSingleResult();
        em.clear();

        Persona personaHumana = em.find(Persona.class, humanaId);
        Persona personaJuridica = em.find(Persona.class, juridicaId);

        assertAll(
                () -> assertInstanceOf(PersonaHumana.class, personaHumana),
                () -> assertEquals("Ana", personaHumana.getNombre()),
                () -> assertNotNull(personaHumana.getUsuario()),
                () -> assertEquals(Rol.DONANTE, personaHumana.getUsuario().getRol()),
                () -> assertEquals(1, filasHumanas.intValue()),
                () -> assertEquals(1, filasJuridicas.intValue()),
                () -> assertInstanceOf(PersonaJuridica.class, personaJuridica),
                () -> assertEquals("Asistencia social", ((PersonaJuridica) personaJuridica).getRubro())
        );
    }

    @Test
    void persisteDonacionConBienYElHistorialDeEstadosPorCascada() {
        EntityManager em = entityManager();
        PersonaHumana donante = personaHumana("donante@example.org");
        EntidadBeneficiaria entidad = new EntidadBeneficiaria(
                "Comedor Central", "Av. Siempre Viva 742", "1111", List.of("contacto@comedor.org"));
        Bien bien = new Bien(Subcategoria.LECHE, 12, "cajas", "Leche larga vida", "leche.png", null,
                LocalDate.of(2027, 6, 1));
        Donacion donacion = new Donacion(donante, bien);
        donacion.asignarA(entidad);
        donacion.cambiarEstado(EstadoDonacion.LISTA_PARA_ENTREGAR, "Ruta confirmada");

        em.persist(donante);
        em.persist(entidad);
        em.persist(donacion);
        em.flush();
        Long donacionId = donacion.getId();
        Long bienId = bien.getId();
        em.clear();

        Donacion recuperada = em.find(Donacion.class, donacionId);

        assertAll(
                () -> assertNotNull(bienId),
                () -> assertEquals(EstadoDonacion.LISTA_PARA_ENTREGAR, recuperada.getEstado()),
                () -> assertEquals(Subcategoria.LECHE, recuperada.getBien().getSubcategoria()),
                () -> assertEquals("Comedor Central", recuperada.getEntidadBeneficiaria().getRazonSocial()),
                () -> assertEquals(3, recuperada.getHistorialEstados().size()),
                () -> assertEquals(EstadoDonacion.LISTA_PARA_ENTREGAR,
                        recuperada.getHistorialEstados().get(2).estadoNuevo())
        );
    }

    @Test
    void buscarDonacionParaActualizarNoAdquiereLockPesimista() {
        EntityManager em = entityManager();
        PersonaHumana donante = personaHumana("lock@example.org");
        Donacion donacion = new Donacion(
                donante,
                new Bien(Subcategoria.FIDEOS, 1, "unidad", null, null, null, null));
        em.persist(donante);
        em.persist(donacion);
        em.flush();
        Long donacionId = donacion.getId();
        em.clear();

        Donacion donacionActualizable = new DonacionRepository().buscarPorIdParaActualizar(donacionId).orElseThrow();

        javax.persistence.LockModeType lockMode = em.getLockMode(donacionActualizable);
        assertNotEquals(javax.persistence.LockModeType.PESSIMISTIC_READ, lockMode);
        assertNotEquals(javax.persistence.LockModeType.PESSIMISTIC_WRITE, lockMode);
    }

    @Test
    void persisteLosResultadosDeCadaAlgoritmo() {
        EntityManager em = entityManager();
        PersonaHumana donante = personaHumana("algoritmos@example.org");
        Donacion donacion = new Donacion(
                donante,
                new Bien(Subcategoria.FIDEOS, 1, "unidad", null, null, null, null));
        EntidadBeneficiaria entidad = new EntidadBeneficiaria("Comedor", "Calle 1", "1111", List.of());
        em.persist(donante);
        em.persist(donacion);
        em.persist(entidad);
        em.flush();

        donaciones.domain.algoritmos.SugerenciaAsignacion sugerencia =
                new donaciones.domain.algoritmos.SugerenciaAsignacion(
                        donacion.getId(),
                        donacion,
                        List.of(entidad),
                        Map.of("Compatibilidad", List.of(entidad), "Prioridad", List.of()));
        em.persist(sugerencia);
        em.flush();
        Long donacionId = donacion.getId();
        em.clear();

        donaciones.domain.algoritmos.SugerenciaAsignacion recuperada =
                em.find(donaciones.domain.algoritmos.SugerenciaAsignacion.class, donacionId);

        assertEquals(List.of(entidad.getId()),
                recuperada.getEntidadesPorAlgoritmo().get("Compatibilidad").stream()
                        .map(EntidadBeneficiaria::getId).toList());
        assertTrue(recuperada.getEntidadesPorAlgoritmo().containsKey("Prioridad"));
        assertTrue(recuperada.getEntidadesPorAlgoritmo().get("Prioridad").isEmpty());
    }

    @Test
    void persisteNecesidadConCantidadesYDatosDeRenovacionEnLaMismaEntidad() {
        EntityManager em = entityManager();
        Necesidad necesidad = new Necesidad(
                "Alimentos para junio",
                true,
                LocalDate.of(2026, 6, 1),
                Periodo.MENSUAL,
                Map.of(Subcategoria.ARROZ, 10, Subcategoria.FIDEOS, 20));
        necesidad.registrarSuplido(new Bien(Subcategoria.ARROZ, 4, "kg", null, null, null, null));
        EntidadBeneficiaria entidad = new EntidadBeneficiaria("Hogar", "Calle 2", "2222", List.of());
        entidad.registrarNecesidad(necesidad);

        em.persist(entidad);
        em.flush();
        Long necesidadId = necesidad.getId();
        em.clear();

        Object[] columnasRenovacion = (Object[]) em.createNativeQuery(
                        "select fecha_inicio, fecha_fin, periodo from necesidad where necesidad_id = ?")
                .setParameter(1, necesidadId)
                .getSingleResult();
        Necesidad recuperada = em.find(Necesidad.class, necesidadId);
        Number requeridas = (Number) em.createNativeQuery(
                        "select count(*) from cantidad_requerida where necesidad_id = ?")
                .setParameter(1, necesidadId)
                .getSingleResult();
        Number suplidas = (Number) em.createNativeQuery(
                        "select cantidad_suplida from cantidad_suplida where necesidad_id = ? and subcategoria_id = ?")
                .setParameter(1, necesidadId)
                .setParameter(2, "ARROZ")
                .getSingleResult();

        assertAll(
                () -> assertEquals(2, requeridas.intValue()),
                () -> assertEquals(4, suplidas.intValue()),
                () -> assertEquals(LocalDate.of(2026, 6, 1), ((java.sql.Date) columnasRenovacion[0]).toLocalDate()),
                () -> assertEquals(LocalDate.of(2026, 7, 1), ((java.sql.Date) columnasRenovacion[1]).toLocalDate()),
                () -> assertEquals("MENSUAL", columnasRenovacion[2]),
                () -> assertEquals(LocalDate.of(2026, 6, 1), recuperada.getFechaInicio()),
                () -> assertEquals(LocalDate.of(2026, 7, 1), recuperada.getFechaFin()),
                () -> assertEquals(Periodo.MENSUAL, recuperada.getPeriodo()),
                () -> assertTrue(recuperada.seRenueva()),
                () -> assertFalse(recuperada.estaSatisfecha())
        );
    }

    @Test
    void unaNecesidadSinRenovacionPersisteNulosEnLasColumnasDeRenovacion() {
        EntityManager em = entityManager();
        Necesidad necesidad = new Necesidad("Donación única", false, null, null, Map.of(Subcategoria.ROPA_INFANTIL, 5));

        em.persist(necesidad);
        em.flush();
        Long necesidadId = necesidad.getId();
        em.clear();

        Object[] columnasRenovacion = (Object[]) em.createNativeQuery(
                        "select fecha_inicio, fecha_fin, periodo from necesidad where necesidad_id = ?")
                .setParameter(1, necesidadId)
                .getSingleResult();
        Necesidad recuperada = em.find(Necesidad.class, necesidadId);

        assertAll(
                () -> assertNull(columnasRenovacion[0]),
                () -> assertNull(columnasRenovacion[1]),
                () -> assertNull(columnasRenovacion[2]),
                () -> assertNull(recuperada.getFechaInicio()),
                () -> assertNull(recuperada.getFechaFin()),
                () -> assertNull(recuperada.getPeriodo()),
                () -> assertFalse(recuperada.seRenueva())
        );
    }

    @Test
    void persisteMultiplesContactosConElDonanteYRecuperaElMedioPredeterminado() {
        EntityManager em = entityManager();
        ContactoPorEmail email = new ContactoPorEmail("notificaciones@example.org");
        ContactoPorSMS sms = new ContactoPorSMS("+5491112345678");
        PersonaHumana persona = new PersonaHumana(
                "Ana", "Pérez", 30, TipoDoc.DNI, "87654321", Genero.FEMENINO,
                "Calle 1", List.of(email, sms), sms, null);

        em.persist(persona);
        em.flush();
        Long personaId = persona.getId();
        em.clear();

        Persona recuperada = em.find(Persona.class, personaId);

        assertAll(
                () -> assertEquals(2, recuperada.getContactos().size()),
                () -> assertInstanceOf(ContactoPorEmail.class, recuperada.getContactos().get(0)),
                () -> assertEquals("notificaciones@example.org", recuperada.getContactos().get(0).getValor()),
                () -> assertInstanceOf(ContactoPorSMS.class, recuperada.getMedioPredeterminado()),
                () -> assertEquals("+5491112345678", recuperada.getMedioPredeterminado().getValor())
        );
    }

    @Test
    void aislaContactosPorTipoDeNotificableAunqueLosIdsCoincidan() {
        EntityManager em = entityManager();
        PersonaHumana persona = personaHumana("persona@example.org");
        EntidadBeneficiaria entidad = new EntidadBeneficiaria(
                "Comedor", "Calle 1", "111", List.of(),
                List.of(new ContactoPorEmail("entidad@example.org")), null);
        PersonaAdministradora administradora = new PersonaAdministradora(
                "Admin", List.of(new ContactoPorEmail("admin@example.org")), null);

        em.persist(persona);
        em.persist(entidad);
        em.persist(administradora);
        em.flush();
        Long personaId = persona.getId();
        Long entidadId = entidad.getId();
        Long administradoraId = administradora.getId();
        em.clear();

        Persona recuperadaPersona = em.find(Persona.class, personaId);
        EntidadBeneficiaria recuperadaEntidad = em.find(EntidadBeneficiaria.class, entidadId);
        PersonaAdministradora recuperadaAdministradora = em.find(PersonaAdministradora.class, administradoraId);

        assertAll(
                () -> assertEquals("persona@example.org", recuperadaPersona.getContactos().get(0).getValor()),
                () -> assertEquals("entidad@example.org", recuperadaEntidad.getContactos().get(0).getValor()),
                () -> assertEquals("admin@example.org", recuperadaAdministradora.getContactos().get(0).getValor())
        );
    }

        @Test
        void persisteNotificacionConReferenciaPolimorficaAlReceptor() {
                EntityManager em = entityManager();
                PersonaHumana receptor = personaHumana("receptor@example.org");
                em.persist(receptor);
                em.flush();

                Notificacion notificacion = new Notificacion(receptor, "Mensaje persistido");
                notificacion.marcarComoCompletada();
                em.persist(notificacion);
                em.flush();
                Long notificacionId = notificacion.getId();
                em.clear();

                Notificacion recuperada = em.find(Notificacion.class, notificacionId);

                assertAll(
                                () -> assertEquals(receptor.getId(), recuperada.getReceptorId()),
                                () -> assertEquals("PERSONA", recuperada.getTipoReceptor()),
                                () -> assertEquals("Mensaje persistido", recuperada.getMensaje()),
                                () -> assertEquals(EstadoNotificacion.COMPLETADA, recuperada.getEstado()),
                                () -> assertNotNull(recuperada.getFecha())
                );
        }

        @Test
        void cargaContactosDePersonaDesdeLaRelacionJPA() {
                    EntityManager em = entityManager();
                    PersonaHumana persona = personaHumana("reconstruida@example.org");
                    em.persist(persona);
                    em.flush();
                    Long personaId = persona.getId();
                    em.clear();

                    Persona recuperada = new RepositorioPersonas().buscarPorId(personaId).orElseThrow();

                assertAll(
                                () -> assertEquals(1, recuperada.getContactos().size()),
                                () -> assertEquals("reconstruida@example.org", recuperada.getContactos().get(0).getValor()),
                                () -> assertEquals(recuperada.getContactos().get(0), recuperada.getMedioPredeterminado())
                );
        }

    @Test
    void persisteUsuariosDeEntidadYAdministradoraPorCascada() {
        EntityManager em = entityManager();
        EntidadBeneficiaria entidad = new EntidadBeneficiaria(
                "Comedor", "Calle 1", "111", List.of("comedor@example.org"),
                new Usuario("comedor@example.org", "secreto", Rol.ENTIDAD));
        PersonaAdministradora administradora = new PersonaAdministradora(
                "Admin", List.of(), null,
                new Usuario("admin@example.org", "secreto", Rol.ADMINISTRADORA));

        em.persist(entidad);
        em.persist(administradora);
        em.flush();
        Long entidadId = entidad.getId();
        Long administradoraId = administradora.getId();
        Long usuarioEntidadId = entidad.getUsuario().getId();
        Long usuarioAdministradoraId = administradora.getUsuario().getId();
        em.clear();

        Usuario usuarioEntidad = em.find(EntidadBeneficiaria.class, entidadId).getUsuario();
        Usuario usuarioAdministradora = em.find(PersonaAdministradora.class, administradoraId).getUsuario();

        assertAll(
                () -> assertNotNull(usuarioEntidadId),
                () -> assertNotNull(usuarioAdministradoraId),
                () -> assertNotEquals(usuarioEntidadId, usuarioAdministradoraId),
                () -> assertEquals(usuarioEntidadId, usuarioEntidad.getId()),
                () -> assertEquals("comedor@example.org", usuarioEntidad.getNombreUsuario()),
                () -> assertEquals(Rol.ENTIDAD, usuarioEntidad.getRol()),
                () -> assertEquals(usuarioAdministradoraId, usuarioAdministradora.getId()),
                () -> assertEquals("admin@example.org", usuarioAdministradora.getNombreUsuario()),
                () -> assertEquals(Rol.ADMINISTRADORA, usuarioAdministradora.getRol())
        );
    }

    private PersonaHumana personaHumana(String email) {
        Contacto contacto = new ContactoPorEmail(email);
        return new PersonaHumana("Donante", "Prueba", 25, TipoDoc.DNI, "40000000", Genero.NO_BINARIO,
                "Calle 3", List.of(contacto), contacto, null);
    }
}
