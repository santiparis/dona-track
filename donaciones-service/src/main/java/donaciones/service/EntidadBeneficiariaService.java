package donaciones.service;

import donaciones.domain.*;
import donaciones.dto.EntidadBeneficiariaDTO;
import donaciones.dto.EntidadBeneficiariaPatchDTO;
import donaciones.dto.NecesidadDTO;
import donaciones.repository.EntidadBeneficiariaRepository;
import donaciones.service.excepcion.EntidadBeneficiariaNoEncontradaException;
import donaciones.service.excepcion.NecesidadNoEncontradaException;
import donaciones.service.excepcion.PeriodoInvalidoException;
import donaciones.service.excepcion.SubcategoriaInvalidaException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EntidadBeneficiariaService {
  private final EntidadBeneficiariaRepository entidadesRepository;

  public EntidadBeneficiariaService(EntidadBeneficiariaRepository entidadesRepository) {
    this.entidadesRepository = entidadesRepository;
  }

  public List<EntidadBeneficiaria> getEntidadesBeneficiarias() {
    return this.entidadesRepository.obtenerTodas();
  }

  public void postEntidadBeneficiaria(EntidadBeneficiariaDTO dto) {
    entidadesRepository.guardar(new EntidadBeneficiaria(dto.razonSocial(), dto.direccion(), dto.telefono(), dto.correosRepresentantes()));
  }

  public void putEntidadBeneficiaria(Long id, EntidadBeneficiariaDTO dto) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorId(id)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    entidad.actualizarDatos(dto.razonSocial(), dto.direccion(), dto.telefono(), dto.correosRepresentantes());
  }

  public void patchEntidadBeneficiaria(Long id, EntidadBeneficiariaPatchDTO dto) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorId(id)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    entidad.actualizarDatos(dto.razonSocial(), dto.direccion(), dto.telefono(), dto.correosRepresentantes());
  }

  public void deleteEntidadBeneficiaria(Long id) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorId(id)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    entidadesRepository.eliminarPorId(id);
  }

  public List<Necesidad> getNecesidades(Long id) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorId(id)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    return entidad.getNecesidades();
  }

  public void postNecesidad(Long id, NecesidadDTO dto) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorId(id)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    Necesidad necesidad = this.crearNecesidad(dto);
    entidad.registrarNecesidad(necesidad);
    entidadesRepository.guardar(entidad);
  }

  public void putNecesidad(Long idEntidad, Long idNecesidad, NecesidadDTO dto) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorId(idEntidad)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    this.buscarNecesidad(entidad, idNecesidad);
    Necesidad nuevaNecesidad = this.crearNecesidad(dto);
    entidad.actualizarNecesidadPorId(idNecesidad, nuevaNecesidad);
  }

  public void patchNecesidad(Long idEntidad, Long idNecesidad, NecesidadDTO dto) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorId(idEntidad)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    this.buscarNecesidad(entidad, idNecesidad);
    Necesidad necesidadActualizada = this.crearNecesidad(dto);
    entidad.actualizarNecesidadPorId(idNecesidad, necesidadActualizada);
  }

  private Necesidad crearNecesidad(NecesidadDTO dto) {
    Map<Subcategoria, Integer> cantidadesRequeridas = this.crearCantidadesRequeridas(dto.cantidadesRequeridas());
    PoliticaDeRenovacion renovacion = this.parsearRenovacion(dto.renovacion(), dto.fechaInicio(), dto.periodo());
    return new Necesidad(dto.descripcion(), renovacion, cantidadesRequeridas);
  }

  private Map<Subcategoria, Integer> crearCantidadesRequeridas(Map<String, Integer> cantidadesDto) {
    Map<Subcategoria, Integer> cantidadesRequeridas = new HashMap<>();

    cantidadesDto.keySet().forEach(key -> {
      Subcategoria subcat = this.parsearSubcategoria(key);
      cantidadesRequeridas.put(subcat, cantidadesDto.get(key));
    });

    return cantidadesRequeridas;
  }

  private Subcategoria parsearSubcategoria(String subcategoria) {
    try {
      return Subcategoria.valueOf(subcategoria.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new SubcategoriaInvalidaException("Subcategoria inexistente");
    }
  }

  private PoliticaDeRenovacion parsearRenovacion(boolean renovacion, String fechaInicio, String periodo) {
    if (renovacion && fechaInicio != null && periodo != null) {
      return new RenovacionPeriodica(LocalDate.parse(fechaInicio), this.parsearPeriodo(periodo));
    }

    return new SinRenovacion();
  }

  private Periodo parsearPeriodo(String periodo) {
    try {
      return Periodo.valueOf(periodo.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new PeriodoInvalidoException("Periodo incorrecto");
    }
  }

  public void deleteNecesidad(Long idEntidad, Long idNecesidad) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorId(idEntidad)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    this.buscarNecesidad(entidad, idNecesidad);
    entidad.eliminarNecesidadPorId(idNecesidad);
  }

  private Necesidad buscarNecesidad(EntidadBeneficiaria entidad, Long idNecesidad) {
    return entidad.getNecesidades().stream()
        .filter(n -> n.getId() != null && n.getId().equals(idNecesidad))
        .findFirst()
        .orElseThrow(() -> new NecesidadNoEncontradaException("No se encontró la necesidad"));
  }
}