package donaciones.dto;

import java.util.List;

public record EntidadBeneficiariaDTO(
   String razonSocial,
   String direccion,
   String telefono,
   List<String> correosRepresentantes
) {}
