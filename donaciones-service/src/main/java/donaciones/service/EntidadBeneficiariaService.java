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
    entidadesRepository.guardar(new EntidadBeneficiaria(new Random().nextLong(), dto.razonSocial(), dto.direccion(), dto.telefono(), dto.correosRepresentantes()));
  }

  public void putEntidadBeneficiaria(int id, EntidadBeneficiariaDTO dto) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorPosicion(id)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    entidad.actualizarDatos(dto.razonSocial(), dto.direccion(), dto.telefono(), dto.correosRepresentantes());
  }

  public void patchEntidadBeneficiaria(int id, EntidadBeneficiariaPatchDTO dto) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorPosicion(id)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    entidad.actualizarDatos(dto.razonSocial(), dto.direccion(), dto.telefono(), dto.correosRepresentantes());
  }

  public void deleteEntidadBeneficiaria(int id) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorPosicion(id)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    entidadesRepository.eliminarPorPosicion(id);
  }

  public List<Necesidad> getNecesidades(int id) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorPosicion(id)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    return entidad.getNecesidades();
  }

  public void postNecesidad(int id, NecesidadDTO dto) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorPosicion(id)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    Necesidad necesidad = this.crearNecesidad(dto);
    entidad.registrarNecesidad(necesidad);
  }

  public void putNecesidad(int idEntidad, int idNecesidad, NecesidadDTO dto) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorPosicion(idEntidad)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    Necesidad necesidad = this.buscarNecesidad(entidad, idNecesidad);
    Necesidad nuevaNecesidad = this.crearNecesidad(dto);
    entidad.actualizarNecesidad(idNecesidad, nuevaNecesidad);
  }

  public void patchNecesidad(int idEntidad, int idNecesidad, NecesidadDTO dto) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorPosicion(idEntidad)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    Necesidad necesidadActual = this.buscarNecesidad(entidad, idNecesidad);
    Necesidad necesidadActualizada = this.crearNecesidad(dto);
    entidad.actualizarNecesidad(idNecesidad, necesidadActualizada);
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

  public void deleteNecesidad(int idEntidad, int idNecesidad) {
    EntidadBeneficiaria entidad = entidadesRepository.buscarPorPosicion(idEntidad)
        .orElseThrow(() -> new EntidadBeneficiariaNoEncontradaException("No se encontró la entidad beneficiaria"));

    if (idNecesidad < 0 || idNecesidad >= entidad.getNecesidades().size()) {
      throw new NecesidadNoEncontradaException("No se encontró la necesidad");
    }

    entidad.eliminarNecesidad(idNecesidad);
  }

  private Necesidad buscarNecesidad(EntidadBeneficiaria entidad, int idNecesidad) {
    if (idNecesidad < 0 || idNecesidad >= entidad.getNecesidades().size()) {
      throw new NecesidadNoEncontradaException("No se encontró la necesidad");
    }
    return entidad.getNecesidades().get(idNecesidad);
  }
}