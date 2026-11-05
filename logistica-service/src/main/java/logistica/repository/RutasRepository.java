package logistica.repository;

import io.github.flbulgarelli.jpa.extras.simple.WithSimplePersistenceUnit;
import logistica.domain.Camion;
import logistica.domain.Coordenadas;
import logistica.domain.Entrega;
import logistica.domain.Ruta;
import logistica.domain.EstadoRuta;

import java.util.List;
import java.util.Optional;
import logistica.grpc.UbicacionCamion;

public class RutasRepository implements WithSimplePersistenceUnit {

  public void agregar(Ruta ruta) {
    persist(ruta);
  }

  public Optional<Ruta> buscarPorId(Long id) {
    return Optional.ofNullable(find(Ruta.class, id));
  }

  public Optional<Entrega> buscarEntregaPorId(Long entregaId) {
    return Optional.ofNullable(find(Entrega.class, entregaId));
  }

  public List<Ruta> obtenerTodas() {
    return createQuery("from Ruta", Ruta.class).getResultList();
  }

  public Optional<Ruta> buscarRutaPorEntregaId(Long entregaId) {
    return createQuery("select r from Ruta r join r.entregas e where e.id = :entregaId", Ruta.class)
        .setParameter("entregaId", entregaId)
        .getResultStream()
        .findFirst();
  }

  public List<UbicacionCamion> obtenerUbicacionCamionesPorIdsDonaciones(List<Long> idsDonaciones) {
    if (idsDonaciones == null || idsDonaciones.isEmpty()) {
      return List.of();
    }

    List<Ruta> rutasEnCurso = createQuery(
        "select distinct r from Ruta r " +
            "join r.entregas e " +
            "join e.listaDonaciones d " +
            "where r.estado = :estado " +
            "and d.donacionID in :idsDonaciones", Ruta.class)
        .setParameter("estado", EstadoRuta.EN_CURSO)
        .setParameter("idsDonaciones", idsDonaciones)
        .getResultList();

    return rutasEnCurso.stream()
        .flatMap(ruta -> ruta.getEntregas().stream()
            .flatMap(entrega -> entrega.getListaDonaciones().stream())
            .filter(donacion -> idsDonaciones.contains(donacion.getDonacionID()))
            .map(donacion -> {
              Camion camion = ruta.getCamion();
              Coordenadas coords = (camion != null) ? camion.getLocalizacion() : null;

              String fechaIso = (camion != null && camion.getUltimaActualizacion() != null)
                  ? camion.getUltimaActualizacion().toString()
                  : "";

              return UbicacionCamion.newBuilder()
                  .setIdDonacion(donacion.getDonacionID())
                  .setPatente(camion != null ? camion.getPatente() : "")
                  .setLatitud(coords != null ? coords.getLatitud() : 0.0)
                  .setLongitud(coords != null ? coords.getLongitud() : 0.0)
                  .setVelocidad(camion != null ? camion.getVelocidad() : 0.0)
                  .setUltimaActualizacion(fechaIso)
                  .build();
            }))
        .toList();
  }
}