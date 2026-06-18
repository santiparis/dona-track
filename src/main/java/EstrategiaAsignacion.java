import java.util.List;

public interface EstrategiaAsignacion {
  List<EntidadBeneficiaria> sugerirEntidades(
      DonacionIndependiente donacion,
      List<EntidadBeneficiaria> todasLasEntidades
  );
}