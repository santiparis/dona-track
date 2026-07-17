package donaciones.dto;

import java.util.List;

public record DonacionResponseDTO(
    Long id,
    DonanteResponseDTO donante,
    BienResponseDTO bien,
    String estado,
    String fecha,
    List<RegistroCambioEstadoDTO> historialEstados
) {}
