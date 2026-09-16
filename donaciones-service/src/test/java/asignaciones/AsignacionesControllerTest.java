package asignaciones;

import donaciones.controller.AsignacionesController;
import donaciones.domain.*;
import donaciones.domain.algoritmos.SugerenciaAsignacion;
import donaciones.domain.donante.*;
import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.ContactoPorEmail;
import donaciones.dto.AsignacionRequestDTO;
import donaciones.repository.DonacionRepository;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.repository.SugerenciaAsignacionRepository;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AsignacionesControllerTest {

  private DonacionRepository donacionRepository;
  private EntidadBeneficiariaRepository entidadRepository;
  private SugerenciaAsignacionRepository sugerenciaRepository;
  private AsignacionesController controller;

  @BeforeEach
  void setUp() {
    donacionRepository = new DonacionRepository();
    entidadRepository = new EntidadBeneficiariaRepository();
    sugerenciaRepository = new SugerenciaAsignacionRepository();
    sugerenciaRepository.limpiar();
    controller = new AsignacionesController(donacionRepository, entidadRepository, sugerenciaRepository);
  }

  @Test
  void procesarDonacionesEnDeposito_guardaSoloSugerenciasParaDonacionesEnDeposito() {
    PersonaHumana donante = crearDonanteHumanoSimple("Juan", "Perez");
    Donacion donacionEnDeposito = crearDonacionConBien(donante, Subcategoria.FIDEOS);
    Donacion donacionAsignada = crearDonacionConBien(donante, Subcategoria.ARROZ);
    donacionAsignada.cambiarEstado(EstadoDonacion.ASIGNADA, null);

    EntidadBeneficiaria entidadCoincidente = crearEntidadConNecesidad("Colegio Rural", "Pueyrredon 1234", "12345678", Subcategoria.FIDEOS, 10);
    EntidadBeneficiaria entidadSinNecesidad = crearEntidadSimple("Iglesia", "Belgrano 5678", "98765432");

    donacionRepository.guardar(donacionEnDeposito);
    donacionRepository.guardar(donacionAsignada);
    entidadRepository.guardar(entidadCoincidente);
    entidadRepository.guardar(entidadSinNecesidad);

    List<SugerenciaAsignacion> sugerencias = controller.procesarDonacionesEnDeposito();

    assertTrue(sugerencias.stream().anyMatch(s -> s.getID().equals(donacionEnDeposito.getId())));
    assertFalse(sugerencias.stream().anyMatch(s -> s.getID().equals(donacionAsignada.getId())));

    SugerenciaAsignacion sugerencia = sugerencias.stream()
        .filter(s -> s.getID().equals(donacionEnDeposito.getId()))
        .findFirst()
        .orElseThrow();

    assertTrue(sugerencia.tieneCoincidencias());
    assertTrue(sugerencia.getCoincidencias().contains(entidadCoincidente));
    assertFalse(sugerencia.getCoincidencias().contains(entidadSinNecesidad));

    assertTrue(controller.obtenerSugerenciasGuardadas().stream()
        .anyMatch(s -> s.getID().equals(donacionEnDeposito.getId())));
  }

  @Test
  void procesarDonacionesEnDeposito_ignoraEntidadesSinNecesidadesActivas() {
    PersonaHumana donante = crearDonanteHumanoSimple("Ana", "Gomez");
    Donacion donacionEnDeposito = crearDonacionConBien(donante, Subcategoria.ARROZ);

    EntidadBeneficiaria entidadConNecesidad = crearEntidadConNecesidad("Comedor", "Mitre 500", "44444444", Subcategoria.ARROZ, 8);
    EntidadBeneficiaria entidadSinNecesidad = crearEntidadSimple("Entidad sin necesidades", "Calle Falsa 123", "55555555");

    donacionRepository.guardar(donacionEnDeposito);
    entidadRepository.guardar(entidadConNecesidad);
    entidadRepository.guardar(entidadSinNecesidad);

    List<SugerenciaAsignacion> sugerencias = controller.procesarDonacionesEnDeposito();

    assertEquals(1, sugerencias.size());

    SugerenciaAsignacion sugerencia = sugerencias.stream()
        .filter(s -> s.getID().equals(donacionEnDeposito.getId()))
        .findFirst()
        .orElseThrow();

    assertTrue(sugerencia.getCoincidencias().contains(entidadConNecesidad));
    assertFalse(sugerencia.getCoincidencias().contains(entidadSinNecesidad));
  }

  @Test
  void getCoincidencias_devuelveLasCoincidenciasDeLaSugerenciaSolicitada() {
    PersonaHumana donante = crearDonanteHumanoSimple("Lucia", "Martinez");
    Donacion donacionEnDeposito = crearDonacionConBien(donante, Subcategoria.FIDEOS);
    EntidadBeneficiaria entidadCoincidente = crearEntidadConNecesidad("Colegio Rural", "Pueyrredon 1234", "12345678", Subcategoria.FIDEOS, 10);

    donacionRepository.guardar(donacionEnDeposito);
    entidadRepository.guardar(entidadCoincidente);

    controller.procesarDonacionesEnDeposito();

    Context ctx = mock(Context.class, RETURNS_DEEP_STUBS);
    when(ctx.pathParam("id")).thenReturn(String.valueOf(donacionEnDeposito.getId()));

    controller.getCoincidencias(ctx);

    SugerenciaAsignacion sugerenciaGuardada = controller.obtenerSugerenciasGuardadas().stream()
        .filter(s -> s.getID().equals(donacionEnDeposito.getId()))
        .findFirst()
        .orElseThrow();

    assertTrue(sugerenciaGuardada.getCoincidencias().contains(entidadCoincidente));
    assertFalse(sugerenciaGuardada.getCoincidencias().isEmpty());
  }

  @Test
  void getEntidadesPorAlgoritmo_devuelveElMapaGeneradoPorLosAlgoritmos() {
    PersonaHumana donante = crearDonanteHumanoSimple("Pedro", "Sanchez");
    Donacion donacionEnDeposito = crearDonacionConBien(donante, Subcategoria.ARROZ);
    EntidadBeneficiaria entidadCoincidente = crearEntidadConNecesidad("Comedor", "Mitre 500", "44444444", Subcategoria.ARROZ, 8);

    donacionRepository.guardar(donacionEnDeposito);
    entidadRepository.guardar(entidadCoincidente);

    controller.procesarDonacionesEnDeposito();

    Context ctx = mock(Context.class, RETURNS_DEEP_STUBS);
    when(ctx.pathParam("id")).thenReturn(String.valueOf(donacionEnDeposito.getId()));

    controller.getEntidadesPorAlgoritmo(ctx);

    SugerenciaAsignacion sugerenciaGuardada = controller.obtenerSugerenciasGuardadas().stream()
        .filter(s -> s.getID().equals(donacionEnDeposito.getId()))
        .findFirst()
        .orElseThrow();

    Map<String, List<EntidadBeneficiaria>> entidadesPorAlgoritmo = sugerenciaGuardada.getEntidadesPorAlgoritmo();

    assertTrue(entidadesPorAlgoritmo.containsKey("Compatibilidad Semantica"));
    assertTrue(entidadesPorAlgoritmo.containsKey("Prioridad Subatendidos"));
    assertFalse(entidadesPorAlgoritmo.values().stream().allMatch(List::isEmpty));
  }

  @Test
  void asignarDonacion_asignaLaEntidadCuandoPerteneceALasSugerencias() {
    PersonaHumana donante = crearDonanteHumanoSimple("Mateo", "Lopez");
    Donacion donacionEnDeposito = crearDonacionConBien(donante, Subcategoria.FIDEOS);
    EntidadBeneficiaria entidadCoincidente = crearEntidadConNecesidad("Colegio Rural", "Pueyrredon 1234", "12345678", Subcategoria.FIDEOS, 10);

    donacionRepository.guardar(donacionEnDeposito);
    entidadRepository.guardar(entidadCoincidente);

    controller.procesarDonacionesEnDeposito();

    Context ctx = mock(Context.class, RETURNS_DEEP_STUBS);
    when(ctx.pathParam("id")).thenReturn(String.valueOf(donacionEnDeposito.getId()));
    when(ctx.bodyAsClass(AsignacionRequestDTO.class)).thenReturn(new AsignacionRequestDTO(entidadCoincidente.getId(), "Colegio Rural"));

    controller.asignarDonacion(ctx);

    assertEquals(EstadoDonacion.ASIGNADA, donacionEnDeposito.getEstado());
    assertEquals(entidadCoincidente.getId(), donacionEnDeposito.getEntidadBeneficiaria().getId());
    assertTrue(controller.obtenerSugerenciasGuardadas().isEmpty());
  }

  @Test
  void asignarDonacion_lanzaExcepcionCuandoLaEntidadNoEstaEntreLasSugerencias() {
    PersonaHumana donante = crearDonanteHumanoSimple("Rosa", "Diaz");
    Donacion donacionEnDeposito = crearDonacionConBien(donante, Subcategoria.FIDEOS);
    EntidadBeneficiaria entidadCoincidente = crearEntidadConNecesidad("Colegio Rural", "Pueyrredon 1234", "12345678", Subcategoria.FIDEOS, 10);
    EntidadBeneficiaria entidadNoSugerida = crearEntidadSimple("Banco de Alimentos", "Avellaneda 10", "22222222");

    donacionRepository.guardar(donacionEnDeposito);
    entidadRepository.guardar(entidadCoincidente);
    entidadRepository.guardar(entidadNoSugerida);

    controller.procesarDonacionesEnDeposito();

    Context ctx = mock(Context.class, RETURNS_DEEP_STUBS);
    when(ctx.pathParam("id")).thenReturn(String.valueOf(donacionEnDeposito.getId()));
    when(ctx.bodyAsClass(AsignacionRequestDTO.class)).thenReturn(new AsignacionRequestDTO(entidadNoSugerida.getId(), "Banco de Alimentos"));

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> controller.asignarDonacion(ctx));

    assertEquals("La entidad seleccionada no aparece en ninguna de las listas generadas por los algoritmos", ex.getMessage());
    assertEquals(EstadoDonacion.EN_DEPOSITO, donacionEnDeposito.getEstado());
  }

  @Test
  void asignarDonacion_lanzaExcepcionCuandoFaltaIdEntidadEnElBody() {
    PersonaHumana donante = crearDonanteHumanoSimple("Laura", "Rossi");
    Donacion donacionEnDeposito = crearDonacionConBien(donante, Subcategoria.ARROZ);

    donacionRepository.guardar(donacionEnDeposito);
    controller.procesarDonacionesEnDeposito();

    Context ctx = mock(Context.class, RETURNS_DEEP_STUBS);
    when(ctx.pathParam("id")).thenReturn(String.valueOf(donacionEnDeposito.getId()));
    when(ctx.bodyAsClass(AsignacionRequestDTO.class)).thenReturn(new AsignacionRequestDTO(null, "Entidad sin id"));

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> controller.asignarDonacion(ctx));

    assertEquals("Se requiere idEntidad en el body de la request", ex.getMessage());
  }

  @Test
  void asignarDonacion_lanzaExcepcionCuandoNoExisteSugerenciaParaLaDonacion() {
    PersonaHumana donante = crearDonanteHumanoSimple("Lucia", "Fernandez");
    Donacion donacionEnDeposito = crearDonacionConBien(donante, Subcategoria.FIDEOS);

    donacionRepository.guardar(donacionEnDeposito);

    Context ctx = mock(Context.class, RETURNS_DEEP_STUBS);
    when(ctx.pathParam("id")).thenReturn(String.valueOf(donacionEnDeposito.getId()));
    when(ctx.bodyAsClass(AsignacionRequestDTO.class)).thenReturn(new AsignacionRequestDTO(1L, "Entidad sin sugerencia"));

    assertThrows(NoSuchElementException.class, () -> controller.asignarDonacion(ctx));
  }

  private PersonaHumana crearDonanteHumanoSimple(String nombre, String apellido) {
    ContactoPorEmail contacto = new ContactoPorEmail(nombre.toLowerCase() + "@example.com");
    return new PersonaHumana(nombre, apellido, 25, TipoDoc.DNI, "12345678", Genero.NO_BINARIO,
        "Avenida Siempreviva 742", List.of(contacto), contacto, null);
  }

  private Donacion crearDonacionConBien(Persona donante, Subcategoria subcategoria) {
    Bien bien = new Bien(subcategoria, 10, "paquetes", "Paquete de 500 g de " + subcategoria.name().toLowerCase(), null, null, null);
    return new Donacion(donante, bien);
  }

  private EntidadBeneficiaria crearEntidadSimple(String razonSocial, String direccion, String telefono) {
    return new EntidadBeneficiaria(razonSocial, direccion, telefono, Collections.emptyList());
  }

  private EntidadBeneficiaria crearEntidadConNecesidad(String razonSocial, String direccion, String telefono,
                                                      Subcategoria subcategoria, int cantidadRequerida) {
    EntidadBeneficiaria entidad = new EntidadBeneficiaria(razonSocial, direccion, telefono, Collections.emptyList());
    Map<Subcategoria, Integer> cantidadesRequeridas = new HashMap<>();
    cantidadesRequeridas.put(subcategoria, cantidadRequerida);
    Necesidad necesidad = new Necesidad(subcategoria.name() + " de cualquier tipo", new SinRenovacion(), cantidadesRequeridas);
    entidad.registrarNecesidad(necesidad);
    return entidad;
  }
}
