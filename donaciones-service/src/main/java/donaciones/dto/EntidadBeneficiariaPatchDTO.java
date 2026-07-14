package donaciones.dto;

import java.util.List;

public record EntidadBeneficiariaPatchDTO(
    String razonSocial,
    String direccion,
    String telefono,
    List<String> correosRepresentantes
) {}
