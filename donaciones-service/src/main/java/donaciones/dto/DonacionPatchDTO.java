package donaciones.dto;

import java.util.List;

public record DonacionPatchDTO(
    Long idDonante,
    List<BienDTO> bienes
) {}
