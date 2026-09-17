package donaciones.dto;

import java.util.List;

public record DonantePatchDTO(
    String documento,
    String nombre,
    String apellido,
    Integer edad,
    String direccion,
    String rubro,
    List<ContactoDTO> contactos
) {}
