package donaciones.dto;

import java.util.List;

public record DonacionPatchDTO(
    String documentoDonante,
    List<BienDTO> bienes
) {}
