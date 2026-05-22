import donante.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SistemaDonacionesTest {

    @Test
    void donacionEntranteSegmentaUnBienEnUnaDonacionIndependiente() {
        Bien fideos = bienFideos(10);
        DonacionEntrante donacionEntrante = new DonacionEntrante(donante(), "Alimentos secos", List.of(fideos));

        assertEquals(1, donacionEntrante.getDonacionesIndependientes().size(),
                "Debería crear una donación independiente por cada bien recibido.");
        assertSame(fideos, donacionEntrante.getDonacionesIndependientes().get(0).getBien(),
                "La donación independiente debería conservar el bien original.");
        assertEquals(EstadoDonacionIndependiente.EN_DEPOSITO, donacionEntrante.getDonacionesIndependientes().get(0).getEstado(),
                "La donación independiente debería comenzar en depósito.");
    }

    @Test
    void donacionEntranteAgrupaBienesDelMismoSegmento() {
        DonacionEntrante donacionEntrante = new DonacionEntrante(
                donante(),
                "Alimentos secos",
                List.of(bienFideos(10), bienFideos(5))
        );

        assertEquals(1, donacionEntrante.getDonacionesIndependientes().size(),
                "Los bienes equivalentes deberían agruparse en una sola donación independiente.");
        assertEquals(15, donacionEntrante.getDonacionesIndependientes().get(0).getBien().getCantidad(),
                "La donación independiente agrupada debería sumar las cantidades.");
    }

    @Test
    void donacionEntranteSeparaPerecederosConDistintoVencimiento() {
        DonacionEntrante donacionEntrante = new DonacionEntrante(
                donante(),
                "Lácteos",
                List.of(
                        bienLeche(new Date(System.currentTimeMillis() + 60_000)),
                        bienLeche(new Date(System.currentTimeMillis() + 120_000))
                )
        );

        assertEquals(2, donacionEntrante.getDonacionesIndependientes().size(),
                "Los bienes perecederos con distinto vencimiento deberían segmentarse por separado.");
    }

    @Test
    void bienGuardaDescripcionYFotoOpcional() {
        Bien bien = new Bien(
                Subcategoria.REMERAS,
                2,
                "unidades",
                EstadoBien.USADO,
                null,
                "Remeras de adulto",
                "remeras.jpg"
        );

        assertEquals("Remeras de adulto", bien.getDescripcion(), "El bien debería guardar su descripción.");
        assertEquals("remeras.jpg", bien.getFoto(), "El bien debería guardar la foto opcional.");
    }

    @Test
    void donacionEntranteSinBienesLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DonacionEntrante(donante(), "Sin bienes", List.of());
        }, "Una donación entrante sin bienes no debería ser válida.");
    }

    @Test
    void bienDeSubcategoriaQueRequiereEstadoSinEstadoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Bien(Subcategoria.BANCOS, 2, "unidades", null, null);
        }, "Los bienes de subcategorías que requieren estado deberían validarlo.");
    }

    @Test
    void actualizarNecesidadesAsignaStockDisponibleAUnaNecesidad() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante donacionEntrante = new DonacionEntrante(donante(), "Fideos", List.of(bienFideos(10)));
        DonacionIndependiente donacion = donacionEntrante.getDonacionesIndependientes().get(0);
        Necesidad necesidad = necesidad("Comedor", bienFideos(4));

        sistema.cargarDonacion(donacionEntrante);
        registrarNecesidad(sistema, necesidad);
        sistema.actualizarNecesidades();

        assertEquals(1, sistema.getAsignaciones().size(), "Debería registrar una asignación realizada.");
        assertSame(necesidad, sistema.getAsignaciones().get(0).getNecesidad(),
                "La asignación debería quedar asociada a la necesidad suplida.");
        assertEquals(6, donacion.getDisponible(), "La donación debería descontar la cantidad asignada.");
        assertEquals(1, donacion.getAsignaciones().size(), "La donación debería registrar el detalle asignado.");
        assertEquals(4, donacion.getAsignaciones().get(0).cantidad(),
                "El detalle de asignación debería reflejar la cantidad usada.");
    }

    @Test
    void actualizarNecesidadesPuedeConsumirVariasDonacionesParaUnaNecesidad() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante primeraDonacion = new DonacionEntrante(donante(), "Fideos 1", List.of(bienFideos(3)));
        DonacionEntrante segundaDonacion = new DonacionEntrante(donante(), "Fideos 2", List.of(bienFideos(2)));
        Necesidad necesidad = necesidad("Entrega completa", bienFideos(5));

        sistema.cargarDonacion(primeraDonacion);
        sistema.cargarDonacion(segundaDonacion);
        registrarNecesidad(sistema, necesidad);

        assertTimeoutPreemptively(Duration.ofSeconds(1), sistema::actualizarNecesidades,
                "Consumir exactamente todo el stock disponible no debería dejar al sistema en loop.");

        DonacionIndependiente primeraIndependiente = primeraDonacion.getDonacionesIndependientes().get(0);
        DonacionIndependiente segundaIndependiente = segundaDonacion.getDonacionesIndependientes().get(0);

        assertEquals(0, primeraIndependiente.getDisponible(), "La primera donación debería quedar sin disponible.");
        assertEquals(0, segundaIndependiente.getDisponible(), "La segunda donación debería quedar sin disponible.");
        assertEquals(EstadoDonacionIndependiente.ENTREGADA, primeraIndependiente.getEstado(),
                "Una donación completamente usada debería quedar entregada.");
        assertEquals(EstadoDonacionIndependiente.ENTREGADA, segundaIndependiente.getEstado(),
                "Una donación completamente usada debería quedar entregada.");
        assertEquals(1, sistema.getAsignaciones().size(), "La necesidad debería resolverse con una asignación.");
    }

    @Test
    void donacionParcialmenteUtilizadaPermaneceEnDeposito() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante donacionEntrante = new DonacionEntrante(donante(), "Fideos", List.of(bienFideos(5)));
        DonacionIndependiente donacion = donacionEntrante.getDonacionesIndependientes().get(0);

        sistema.cargarDonacion(donacionEntrante);
        registrarNecesidad(sistema, necesidad("Comedor", bienFideos(2)));
        sistema.actualizarNecesidades();

        assertEquals(3, donacion.getDisponible(), "La donación debería conservar disponible no utilizado.");
        assertEquals(EstadoDonacionIndependiente.EN_DEPOSITO, donacion.getEstado(),
                "Una donación con cantidad disponible debería seguir en depósito.");
    }

    @Test
    void actualizarDonacionesVencidasMarcaVencidasLasDonacionesConFechaPasada() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante donacionEntrante = new DonacionEntrante(
                donante(),
                "Leche vencida",
                List.of(bienLeche(new Date(System.currentTimeMillis() - 1_000)))
        );
        DonacionIndependiente donacion = donacionEntrante.getDonacionesIndependientes().get(0);

        sistema.cargarDonacion(donacionEntrante);
        sistema.actualizarDonacionesVencidas();

        assertEquals(EstadoDonacionIndependiente.VENCIDA, donacion.getEstado(),
                "Una donación con vencimiento anterior a la fecha actual debería quedar vencida.");
        assertEquals(1, sistema.getDonacionesVencidas().size(),
                "La donación vencida debería moverse a la lista de donaciones vencidas.");
        assertSame(donacion, sistema.getDonacionesVencidas().get(0),
                "La lista de donaciones vencidas debería conservar la donación original.");
    }

    @Test
    void actualizarDonacionesVencidasNoMarcaDonacionesConFechaFutura() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante donacionEntrante = new DonacionEntrante(
                donante(),
                "Leche vigente",
                List.of(bienLeche(new Date(System.currentTimeMillis() + 60_000)))
        );
        DonacionIndependiente donacion = donacionEntrante.getDonacionesIndependientes().get(0);

        sistema.cargarDonacion(donacionEntrante);
        sistema.actualizarDonacionesVencidas();

        assertEquals(EstadoDonacionIndependiente.EN_DEPOSITO, donacion.getEstado(),
                "Una donación con vencimiento futuro debería seguir en depósito.");
        assertTrue(sistema.getDonacionesVencidas().isEmpty(),
                "Una donación vigente no debería moverse a donaciones vencidas.");
    }

    @Test
    void actualizarDonacionesVencidasIgnoraSubcategoriasSinVencimiento() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante donacionEntrante = new DonacionEntrante(donante(), "Fideos", List.of(bienFideos(2)));
        DonacionIndependiente donacion = donacionEntrante.getDonacionesIndependientes().get(0);

        sistema.cargarDonacion(donacionEntrante);
        sistema.actualizarDonacionesVencidas();

        assertEquals(EstadoDonacionIndependiente.EN_DEPOSITO, donacion.getEstado(),
                "Las subcategorías sin vencimiento no deberían cambiar de estado.");
        assertTrue(sistema.getDonacionesVencidas().isEmpty(),
                "Las subcategorías sin vencimiento no deberían moverse a donaciones vencidas.");
    }

    @Test
    void actualizarNecesidadesNoUtilizaDonacionesMovidasAVencidas() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante donacionEntrante = new DonacionEntrante(
                donante(),
                "Leche vencida",
                List.of(bienLeche(new Date(System.currentTimeMillis() - 1_000)))
        );

        sistema.cargarDonacion(donacionEntrante);
        sistema.actualizarDonacionesVencidas();
        registrarNecesidad(sistema, necesidad("Comedor", bienLeche(new Date(System.currentTimeMillis() + 60_000))));
        sistema.actualizarNecesidades();

        assertTrue(sistema.getAsignaciones().isEmpty(),
                "El sistema no debería suplir necesidades con donaciones vencidas.");
        assertEquals(1, sistema.getNecesidades().size(),
                "La necesidad debería quedar pendiente si sólo hay donaciones vencidas.");
    }

    @Test
    void necesidadRecurrenteVencidaNoConsumeStockYCreaLaSiguiente() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante donacionEntrante = new DonacionEntrante(donante(), "Fideos", List.of(bienFideos(2)));
        DonacionIndependiente donacion = donacionEntrante.getDonacionesIndependientes().get(0);
        Date fechaInicio = new Date(System.currentTimeMillis() - 15 * 24 * 60 * 60 * 1000L);
        Date fechaFin = new Date(System.currentTimeMillis() - 8 * 24 * 60 * 60 * 1000L);
        NecesidadRecurrente necesidad = necesidadRecurrente(
                "Necesidad recurrente vencida",
                List.of(bienFideos(2)),
                fechaInicio,
                fechaFin,
                Periodo.SEMANAL
        );

        sistema.cargarDonacion(donacionEntrante);
        registrarNecesidad(sistema, necesidad);
        sistema.actualizarNecesidades();

        assertEquals(2, donacion.getDisponible(), "Una necesidad vencida no debería consumir stock.");
        assertEquals(EstadoDonacionIndependiente.EN_DEPOSITO, donacion.getEstado(),
                "La donación no utilizada debería mantenerse en depósito.");
        assertTrue(sistema.getAsignaciones().isEmpty(), "Una necesidad vencida no debería generar asignación.");
        assertEquals(1, sistema.getNecesidades().size(), "Debería crear la necesidad del siguiente período.");
        NecesidadRecurrente siguiente = (NecesidadRecurrente) sistema.getNecesidades().get(0);
        assertEquals(new Date(fechaInicio.getTime() + 7 * 24 * 60 * 60 * 1000L), siguiente.getFechaInicio(),
                "La siguiente necesidad semanal debería comenzar una semana después.");
        assertEquals(new Date(fechaFin.getTime() + 7 * 24 * 60 * 60 * 1000L), siguiente.getFechaFin(),
                "La siguiente necesidad semanal debería vencer una semana después.");
    }

    @Test
    void necesidadRecurrenteSuplidaCreaLaSiguienteAunqueNoHayaVencido() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante donacionEntrante = new DonacionEntrante(donante(), "Fideos", List.of(bienFideos(2)));
        Date fechaInicio = new Date(System.currentTimeMillis());
        Date fechaFin = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L);
        NecesidadRecurrente necesidad = necesidadRecurrente(
                "Necesidad recurrente activa",
                List.of(bienFideos(2)),
                fechaInicio,
                fechaFin,
                Periodo.SEMANAL
        );

        sistema.cargarDonacion(donacionEntrante);
        registrarNecesidad(sistema, necesidad);
        sistema.actualizarNecesidades();

        assertEquals(1, sistema.getAsignaciones().size(), "La necesidad activa debería suplirse con el stock disponible.");
        assertEquals(1, sistema.getNecesidades().size(), "Debería registrar una nueva necesidad recurrente.");
        NecesidadRecurrente siguiente = (NecesidadRecurrente) sistema.getNecesidades().get(0);
        assertEquals(necesidad.getBienes(), siguiente.getBienes(), "La siguiente necesidad debería conservar los mismos bienes.");
        assertEquals(new Date(fechaInicio.getTime() + 7 * 24 * 60 * 60 * 1000L), siguiente.getFechaInicio(),
                "La siguiente necesidad semanal debería iniciar una semana después de la fecha original.");
        assertEquals(new Date(fechaFin.getTime() + 7 * 24 * 60 * 60 * 1000L), siguiente.getFechaFin(),
                "La siguiente necesidad semanal debería mantener la misma duración.");
    }

    @Test
    void necesidadExtraSuplidaNoCreaNuevaNecesidad() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante donacionEntrante = new DonacionEntrante(donante(), "Fideos", List.of(bienFideos(2)));

        sistema.cargarDonacion(donacionEntrante);
        registrarNecesidad(sistema, new NecesidadExtra(entidad(), Subcategoria.FIDEOS, 2, "Necesidad extraordinaria"));
        sistema.actualizarNecesidades();

        assertEquals(1, sistema.getAsignaciones().size(), "La necesidad extraordinaria debería suplirse.");
        assertTrue(sistema.getNecesidades().isEmpty(),
                "Resolver una necesidad extraordinaria no debería crear una nueva necesidad.");
    }

    @Test
    void actualizarNecesidadesAsignaParcialmenteSiElStockEsInsuficiente() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante donacionEntrante = new DonacionEntrante(donante(), "Fideos", List.of(bienFideos(2)));
        DonacionIndependiente donacion = donacionEntrante.getDonacionesIndependientes().get(0);
        Necesidad necesidad = necesidad("Comedor", bienFideos(5));

        sistema.cargarDonacion(donacionEntrante);
        registrarNecesidad(sistema, necesidad);
        sistema.actualizarNecesidades();

        assertEquals(1, sistema.getAsignaciones().size(), "Debería registrar una asignación parcial.");
        assertEquals(0, donacion.getDisponible(), "El stock disponible debería consumirse.");
        assertEquals(3, necesidad.getCantidadPendiente(bienFideos(5)),
                "La necesidad debería quedar pendiente por la cantidad faltante.");
        assertEquals(1, sistema.getNecesidades().size(), "La necesidad parcial no debería eliminarse.");
    }

    @Test
    void actualizarNecesidadesNoFallaSiFaltaLaSubcategoriaSolicitada() {
        SistemaDonaciones sistema = new SistemaDonaciones();

        registrarNecesidad(sistema, necesidad("Comedor", bienFideos(1)));

        assertDoesNotThrow(sistema::actualizarNecesidades,
                "Una necesidad sin stock de su subcategoría no debería romper el proceso.");
        assertTrue(sistema.getAsignaciones().isEmpty(), "No debería crear asignaciones sin stock.");
    }

    @Test
    void actualizarNecesidadesPuedeResolverMasDeUnaNecesidadEnLaMismaActualizacion() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante donacionEntrante = new DonacionEntrante(donante(), "Fideos", List.of(bienFideos(10)));
        DonacionIndependiente donacion = donacionEntrante.getDonacionesIndependientes().get(0);

        sistema.cargarDonacion(donacionEntrante);
        registrarNecesidad(sistema, necesidad("Primer comedor", bienFideos(2)));
        registrarNecesidad(sistema, necesidad("Segundo comedor", bienFideos(3)));
        sistema.actualizarNecesidades();

        assertEquals(2, sistema.getAsignaciones().size(), "Debería poder resolver varias necesidades en una actualización.");
        assertEquals(5, donacion.getDisponible(), "El stock debería descontar ambas asignaciones.");
        assertEquals(2, donacion.getAsignaciones().size(), "La donación debería registrar ambas asignaciones realizadas.");
    }

    @Test
    void cambiarEstadoAsignacionModificaLaAsignacionIndicada() {
        SistemaDonaciones sistema = sistemaConUnaAsignacion();

        sistema.cambiarEstadoAsignacion(0, EstadoAsignacion.LISTA_ENTREGA);

        assertEquals(EstadoAsignacion.LISTA_ENTREGA, sistema.getAsignaciones().get(0).getEstado(),
                "Debería cambiar el estado de la asignación indicada por índice.");
        assertEquals(2, sistema.getAsignaciones().get(0).getHistorialEstados().size(),
                "La asignación debería auditar los cambios de estado.");
    }

    @Test
    void cambiarEstadoAsignacionAEntregaFallidaRequiereYGuardaJustificacion() {
        SistemaDonaciones sistema = sistemaConUnaAsignacion();

        sistema.cambiarEstadoAsignacion(0, EstadoAsignacion.ENTREGA_FALLIDA, "No había nadie para recibir");

        assertEquals(EstadoAsignacion.ENTREGA_FALLIDA, sistema.getAsignaciones().get(0).getEstado(),
                "La asignación debería quedar con entrega fallida.");
        assertEquals("No había nadie para recibir", sistema.getAsignaciones().get(0).getJustificacionEntregaFallida(),
                "La asignación debería guardar la justificación de entrega fallida.");
    }

    @Test
    void cambiarEstadoAsignacionAEntregaFallidaSinJustificacionLanzaExcepcion() {
        SistemaDonaciones sistema = sistemaConUnaAsignacion();

        assertThrows(IllegalArgumentException.class, () -> {
            sistema.cambiarEstadoAsignacion(0, EstadoAsignacion.ENTREGA_FALLIDA);
        }, "La entrega fallida debería requerir una justificación.");
    }

    @Test
    void sistemaGestionaEntidadesBeneficiariasYNecesidades() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        PersonaJuridica entidad = entidad();
        Necesidad necesidad = new NecesidadExtra(entidad, "Necesidad múltiple", List.of(bienFideos(2), bienFideos(3)));

        sistema.registrarEntidadBeneficiaria(entidad);
        registrarNecesidad(sistema, necesidad);

        assertEquals(1, sistema.getEntidadesBeneficiarias().size(), "El sistema debería registrar entidades beneficiarias.");
        assertEquals(1, entidad.getNecesidades().size(), "La entidad debería conservar sus necesidades registradas.");
        assertEquals(5, necesidad.getCantidades().get(Subcategoria.FIDEOS),
                "Las necesidades con múltiples bienes deberían acumular cantidades por subcategoría.");
    }

    @Test
    void cambiarEstadoAsignacionConIndiceInvalidoLanzaExcepcion() {
        SistemaDonaciones sistema = sistemaConUnaAsignacion();

        assertThrows(IndexOutOfBoundsException.class, () -> {
            sistema.cambiarEstadoAsignacion(1, EstadoAsignacion.ENTREGADA);
        }, "Un índice inexistente debería lanzar la excepción estándar de lista.");
    }

    @Test
    void cambiarEstadoAsignacionAEntregadaCierraRecurrenteSiSiguePendiente() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        Date fechaInicio = new Date(System.currentTimeMillis());
        Date fechaFin = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L);
        NecesidadRecurrente necesidad = necesidadRecurrente(
                "Necesidad recurrente en entrega",
                List.of(bienFideos(2)),
                fechaInicio,
                fechaFin,
                Periodo.SEMANAL
        );

        registrarNecesidad(sistema, necesidad);
        sistema.getAsignaciones().add(new Asignacion(necesidad, EstadoAsignacion.LISTA_ENTREGA));
        sistema.cambiarEstadoAsignacion(0, EstadoAsignacion.ENTREGADA);

        assertEquals(EstadoAsignacion.ENTREGADA, sistema.getAsignaciones().get(0).getEstado(),
                "La asignación debería quedar entregada.");
        assertEquals(1, sistema.getNecesidades().size(),
                "La recurrente entregada debería reemplazarse por la siguiente necesidad.");
        NecesidadRecurrente siguiente = (NecesidadRecurrente) sistema.getNecesidades().get(0);
        assertNotSame(necesidad, siguiente, "La necesidad actual debería eliminarse de la lista.");
        assertEquals(new Date(fechaInicio.getTime() + 7 * 24 * 60 * 60 * 1000L), siguiente.getFechaInicio(),
                "La siguiente necesidad debería iniciar en el próximo período.");
    }

    @Test
    void donacionAsignadaPeroNoEntregadaQueSeVenceDesasignaYReestableceLaNecesidad() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante donacionEntrante = new DonacionEntrante(
                donante(),
                "Leche",
                List.of(bienLeche(new Date(System.currentTimeMillis() - 1_000)))
        );
        DonacionIndependiente donacion = donacionEntrante.getDonacionesIndependientes().get(0);
        Necesidad necesidad = necesidad("Comedor", bienLeche(new Date(System.currentTimeMillis() + 60_000)));

        sistema.cargarDonacion(donacionEntrante);
        registrarNecesidad(sistema, necesidad);
        sistema.actualizarNecesidades();

        assertEquals(1, sistema.getAsignaciones().size(),
                "Debería haber una asignación realizada.");
        assertEquals(EstadoAsignacion.ASIGNACION_REALIZADA, sistema.getAsignaciones().get(0).getEstado(),
                "La asignación debería estar en estado ASIGNACION_REALIZADA.");
        assertEquals(0, necesidad.getCantidadesPendientes().getOrDefault(Subcategoria.LECHE, 0),
                "La necesidad debería estar completamente satisfecha.");
        assertEquals(0, donacion.getDisponible(),
                "La donación debería estar completamente usada.");

        // Simular vencimiento de la donación
        sistema.actualizarDonacionesVencidas();

        assertEquals(EstadoDonacionIndependiente.VENCIDA, donacion.getEstado(),
                "La donación debería estar vencida.");
        assertEquals(0, sistema.getAsignaciones().size(),
                "La asignación debería haber sido eliminada después del vencimiento.");
        assertEquals(1, necesidad.getCantidadesPendientes().getOrDefault(Subcategoria.LECHE, 0),
                "La necesidad debería volver a estar pendiente con la cantidad original.");
    }

    @Test
    void donacionAsignadaParcialmentePeroNoEntregadaQueSeVenceDesasignaLaParte() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante donacionEntrante = new DonacionEntrante(
                donante(),
                "Leche",
                List.of(bienLeche(new Date(System.currentTimeMillis() - 1_000)))
        );
        DonacionIndependiente donacion = donacionEntrante.getDonacionesIndependientes().get(0);

        sistema.cargarDonacion(donacionEntrante);
        registrarNecesidad(sistema, necesidad("Comedor", bienLeche(new Date(System.currentTimeMillis() + 60_000))));
        sistema.actualizarNecesidades();

        assertEquals(1, sistema.getAsignaciones().size(), "Debería haber una asignación.");
        assertEquals(0, donacion.getDisponible(), "La donación debería estar completamente usada.");

        // Actualizar donaciones vencidas
        sistema.actualizarDonacionesVencidas();

        assertEquals(EstadoDonacionIndependiente.VENCIDA, donacion.getEstado(),
                "La donación vencida debería estar marcada como VENCIDA.");
        assertEquals(1, sistema.getDonacionesVencidas().size(),
                "La donación debería moverse a donaciones vencidas.");
        assertEquals(0, sistema.getAsignaciones().size(),
                "La asignación debería haber sido eliminada.");
    }

    @Test
    void donacionEntregadaQueSeVenceNoDesasigna() {
        SistemaDonaciones sistema = new SistemaDonaciones();
        DonacionEntrante donacionEntrante = new DonacionEntrante(
                donante(),
                "Leche",
                List.of(bienLeche(new Date(System.currentTimeMillis() - 1_000)))
        );
        Necesidad necesidad = necesidad("Comedor", bienLeche(new Date(System.currentTimeMillis() + 60_000)));

        sistema.cargarDonacion(donacionEntrante);
        registrarNecesidad(sistema, necesidad);
        sistema.actualizarNecesidades();

        sistema.cambiarEstadoAsignacion(0, EstadoAsignacion.ENTREGADA);

        assertEquals(EstadoAsignacion.ENTREGADA, sistema.getAsignaciones().get(0).getEstado(),
                "La asignación debería estar entregada.");

        sistema.actualizarDonacionesVencidas();

        assertEquals(1, sistema.getAsignaciones().size(),
                "La asignación entregada no debería desasignarse al vencerse la donación.");
        assertEquals(EstadoAsignacion.ENTREGADA, sistema.getAsignaciones().get(0).getEstado(),
                "El estado de la asignación no debería cambiar.");
    }

    private static Persona donante() {
        Contacto email = new Contacto(TipoContacto.EMAIL, "ana@example.com");
        return new PersonaHumana(
                "Ana",
                "Perez",
                30,
                TipoDoc.DNI,
                "12345678",
                Genero.FEMENINO,
                "Calle falsa 123",
                List.of(email),
                email,
                new Usuario("ana", "1234")
        );
    }

    private static Bien bienFideos(Integer cantidad) {
        return new Bien(Subcategoria.FIDEOS, cantidad, "paquetes", null, null);
    }

    private static Bien bienLeche(Date vencimiento) {
        return new Bien(Subcategoria.LECHE, 1, "litros", null, vencimiento);
    }

    private static Necesidad necesidad(String descripcion, Bien bien) {
        return new Necesidad(entidad(), descripcion, List.of(bien)) {};
    }

    private static NecesidadRecurrente necesidadRecurrente(
            String descripcion,
            List<Bien> bienes,
            Date fechaInicio,
            Date fechaFin,
            Periodo periodo
    ) {
        return new NecesidadRecurrente(
                entidad(),
                descripcion,
                bienes,
                fechaInicio,
                fechaFin,
                periodo
        );
    }

    private static PersonaJuridica entidad() {
        Contacto email = new Contacto(TipoContacto.EMAIL, "contacto@fundacion.org");
        return new PersonaJuridica(
                TipoDoc.CUIT,
                "30-12345678-9",
                "Fundacion",
                RazonSocial.ONG,
                "Asistencia social",
                "Av Siempre Viva 742",
                "1111-1111",
                List.of("contacto@fundacion.org"),
                List.of(),
                List.of(email),
                email,
                new Usuario("fundacion", "1234")
        );
    }

    private static void registrarNecesidad(SistemaDonaciones sistema, Necesidad necesidad) {
        necesidad.getEntidad().registrarNecesidad(sistema, necesidad);
    }

    private static SistemaDonaciones sistemaConUnaAsignacion() {
        SistemaDonaciones sistema = new SistemaDonaciones();

        sistema.cargarDonacion(new DonacionEntrante(donante(), "Fideos", List.of(bienFideos(3))));
        registrarNecesidad(sistema, necesidad("Comedor", bienFideos(2)));
        sistema.actualizarNecesidades();

        return sistema;
    }
}
