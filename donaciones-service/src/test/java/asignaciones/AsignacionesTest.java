package asignaciones;

import donaciones.domain.*;
import donaciones.domain.algoritmos.CompatibilidadSemantica;
import donaciones.domain.algoritmos.OrganizadorAsignaciones;
import donaciones.domain.algoritmos.PrioridadSubAtendidos;
import donaciones.domain.algoritmos.SugerenciaAsignacion;
import donaciones.domain.donante.*;
import donaciones.domain.notificacion.Contacto;
import donaciones.domain.notificacion.ContactoPorEmail;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AsignacionesTest {
  @Test
  void unOrganizadorConUnAlgoritmoDevuelveUnaSugerenciaSinCoincidencias() {
    PersonaHumana juan = crearDonanteHumanoSimple("Juan", "Perez");
    Donacion fideos = crearDonacionDeFideos(juan);
    EntidadBeneficiaria colegioRural = crearEntidadConNecesidadDeFideos("Colegio Rural", "Pueyrredon 1234", "12345678");
    EntidadBeneficiaria iglesia = crearEntidadConNecesidadDeFideos("Iglesia", "Belgrano 5678", "98765432");
    OrganizadorAsignaciones organizadorConUnAlgoritmo = new OrganizadorAsignaciones(List.of(new CompatibilidadSemantica()));
    SugerenciaAsignacion sugerencia = organizadorConUnAlgoritmo.procesarMatchmaking(fideos, List.of(colegioRural, iglesia));

    assertFalse(sugerencia.tieneCoincidencias());
    assertEquals(1, sugerencia.getEntidadesPorAlgoritmo().size());
    assertTrue(sugerencia.getEntidadesPorAlgoritmo().values().stream()
        .allMatch(lista -> lista.equals(List.of(colegioRural, iglesia))));
  }

  @Test
  void unOrganizadorConDosAlgoritmosDevuelveCoincidenciasCuandoTodosSugerencianLasMismasEntidades() {
    PersonaHumana juan = crearDonanteHumanoSimple("Juan", "Perez");
    Donacion fideos = crearDonacionDeFideos(juan);
    EntidadBeneficiaria colegioRural = crearEntidadConNecesidadDeFideos("Colegio Rural", "Pueyrredon 1234", "12345678");
    EntidadBeneficiaria iglesia = crearEntidadConNecesidadDeFideos("Iglesia", "Belgrano 5678", "98765432");

    OrganizadorAsignaciones organizador = new OrganizadorAsignaciones(List.of(new CompatibilidadSemantica(), new PrioridadSubAtendidos()));
    SugerenciaAsignacion sugerencia = organizador.procesarMatchmaking(fideos, List.of(colegioRural, iglesia));

    assertTrue(sugerencia.tieneCoincidencias());
    assertEquals(List.of(colegioRural, iglesia), sugerencia.getCoincidencias());
    assertEquals(2, sugerencia.getEntidadesPorAlgoritmo().size());
    assertTrue(sugerencia.getEntidadesPorAlgoritmo().values().stream()
        .allMatch(lista -> lista.equals(List.of(colegioRural, iglesia))));
  }

  @Test
  void unOrganizadorConDosAlgoritmosDevuelveSinCoincidenciasCuandoLasSugerenciasNoSeSolapan() {
    PersonaHumana juan = crearDonanteHumanoSimple("Juan", "Perez");
    Donacion fideos = crearDonacionDeFideos(juan);
    EntidadBeneficiaria colegioRural = crearEntidadConNecesidadDeFideos("Colegio Rural", "Pueyrredon 1234", "12345678");
    EntidadBeneficiaria iglesia = crearEntidadConNecesidadDeFideos("Iglesia", "Belgrano 5678", "98765432");

    OrganizadorAsignaciones organizador = new OrganizadorAsignaciones(List.of(
        (donacion, entidades) -> List.of(colegioRural),
        (donacion, entidades) -> List.of(iglesia)
    ));
    SugerenciaAsignacion sugerencia = organizador.procesarMatchmaking(fideos, List.of(colegioRural, iglesia));

    assertFalse(sugerencia.tieneCoincidencias());
    assertEquals(2, sugerencia.getEntidadesPorAlgoritmo().size());
    assertTrue(sugerencia.getEntidadesPorAlgoritmo().values().stream()
        .anyMatch(lista -> lista.equals(List.of(colegioRural))));
    assertTrue(sugerencia.getEntidadesPorAlgoritmo().values().stream()
        .anyMatch(lista -> lista.equals(List.of(iglesia))));
  }

  PersonaHumana crearDonanteHumanoSimple(String nombre, String apellido) {
    Contacto contacto = new ContactoPorEmail("example@example.com");
    return new PersonaHumana(nombre, apellido, 25, TipoDoc.DNI, "12345678", Genero.NO_BINARIO, "Avenida Siempreviva 742", List.of(contacto), contacto, null);
  }

  Donacion crearDonacionSimple(Persona donante, Bien bien) {
    return new Donacion(donante, bien);
  }

  Donacion crearDonacionDeFideos(Persona donante) {
    return crearDonacionConBien(donante, Subcategoria.FIDEOS);
  }

  Donacion crearDonacionDeArroz(Persona donante) {
    return crearDonacionConBien(donante, Subcategoria.ARROZ);
  }

  private Donacion crearDonacionConBien(Persona donante, Subcategoria subcategoria) {
    Bien bien = new Bien(subcategoria, 10, "paquetes", "Paquete de 500 g de " + subcategoria.nombre().toLowerCase(), null, null, null);
    return new Donacion(donante, bien);
  }

  EntidadBeneficiaria crearEntidadSimple(String razonSocial, String direccion, String telefono) {
    return new EntidadBeneficiaria(razonSocial, direccion, telefono, Collections.emptyList());
  }

  EntidadBeneficiaria crearEntidadConNecesidadDeFideos(String razonSocial, String direccion, String telefono) {
    return crearEntidadConNecesidad(razonSocial, direccion, telefono, Subcategoria.FIDEOS, 10);
  }

  EntidadBeneficiaria crearEntidadConNecesidad(String razonSocial, String direccion, String telefono, Subcategoria subcategoria, int cantidadRequerida) {
    EntidadBeneficiaria entidad = new EntidadBeneficiaria(razonSocial, direccion, telefono, Collections.emptyList());
    Map<Subcategoria, Integer> cantidadesRequeridas = new HashMap<>();
    cantidadesRequeridas.put(subcategoria, cantidadRequerida);
    Necesidad necesidad = new Necesidad(subcategoria.name() + " de cualquier tipo", new SinRenovacion(), cantidadesRequeridas);
    entidad.registrarNecesidad(necesidad);
    return entidad;
  }
}
