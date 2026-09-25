package db;

import donaciones.domain.*;
import donaciones.domain.donante.*;
import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.ContactoId;
import donaciones.domain.notificacion.ContactoPorEmail;
import donaciones.domain.notificacion.EstadoNotificacion;
import donaciones.domain.notificacion.Notificacion;
import donaciones.domain.donante.RepositorioPersonas;
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
    void persisteLaJerarquiaPersonaEnUnaSolaTablaYConservaElSubtipo() {
        EntityManager em = entityManager();
        Contacto contacto = new ContactoPorEmail("ana@example.org");
        Usuario usuario = new Usuario("ana", "secreto");
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
        em.clear();

        Persona personaHumana = em.find(Persona.class, humanaId);
        Persona personaJuridica = em.find(Persona.class, juridicaId);

        assertAll(
                () -> assertInstanceOf(PersonaHumana.class, personaHumana),
                () -> assertEquals("Ana", personaHumana.getNombre()),
                () -> assertNotNull(personaHumana.getUsuario()),
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
    void persisteNecesidadConCantidadesDeClaveCompuestaYPoliticaDeRenovacion() {
        EntityManager em = entityManager();
        Necesidad necesidad = new Necesidad(
                "Alimentos para junio",
                new RenovacionPeriodica(LocalDate.of(2026, 6, 1), Periodo.MENSUAL),
                Map.of(Subcategoria.ARROZ, 10, Subcategoria.FIDEOS, 20));
        necesidad.registrarSuplido(new Bien(Subcategoria.ARROZ, 4, "kg", null, null, null, null));
        EntidadBeneficiaria entidad = new EntidadBeneficiaria("Hogar", "Calle 2", "2222", List.of());
        entidad.registrarNecesidad(necesidad);

        em.persist(entidad);
        em.flush();
        Long necesidadId = necesidad.getId();
        em.clear();

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
                () -> assertTrue(recuperada.getRenovacion().seRenueva()),
                () -> assertFalse(recuperada.estaSatisfecha())
        );
    }

    @Test
    void unaNecesidadSinRenovacionPersisteNulosEnLasColumnasDeRenovacion() {
        EntityManager em = entityManager();
        Necesidad necesidad = new Necesidad("Donación única", new SinRenovacion(), Map.of(Subcategoria.ROPA_INFANTIL, 5));

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
                () -> assertFalse(recuperada.getRenovacion().seRenueva())
        );
    }

    @Test
    void persisteContactoConLaClaveCompuestaDelDerYRecuperaSuSubtipo() {
        EntityManager em = entityManager();
        ContactoPorEmail contacto = new ContactoPorEmail("notificaciones@example.org");
        ContactoId id = new ContactoId(99L, "PERSONA");
        contacto.identificarNotificable(99L, "PERSONA");

        em.persist(contacto);
        em.flush();
        em.clear();

        Contacto recuperado = em.find(Contacto.class, id);

        assertAll(
                () -> assertInstanceOf(ContactoPorEmail.class, recuperado),
                () -> assertEquals("notificaciones@example.org", recuperado.getValor())
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
        void reconstruyeContactosDeUnaPersonaDesdeLaClaveDelNotificable() {
                EntityManager em = entityManager();
                PersonaHumana persona = personaHumana("reconstruida@example.org");
                em.persist(persona);
                em.flush();

                Contacto contacto = new ContactoPorEmail("reconstruida@example.org");
                contacto.identificarNotificable(persona.getId(), "PERSONA");
                em.persist(contacto);
                em.flush();
                em.clear();

                Persona recuperada = new RepositorioPersonas(em).buscarPorId(persona.getId()).orElseThrow();

                assertAll(
                                () -> assertEquals(1, recuperada.getContactos().size()),
                                () -> assertEquals("reconstruida@example.org", recuperada.getContactos().get(0).getValor()),
                                () -> assertEquals(recuperada.getContactos().get(0), recuperada.getMedioPredeterminado())
                );
        }

    private PersonaHumana personaHumana(String email) {
        Contacto contacto = new ContactoPorEmail(email);
        return new PersonaHumana("Donante", "Prueba", 25, TipoDoc.DNI, "40000000", Genero.NO_BINARIO,
                "Calle 3", List.of(contacto), contacto, null);
    }
}
