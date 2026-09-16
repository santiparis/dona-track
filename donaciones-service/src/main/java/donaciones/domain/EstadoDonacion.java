package donaciones.domain;

public enum EstadoDonacion {
    EN_DEPOSITO,
    ASIGNADA,                   // Nada
    LISTA_PARA_ENTREGAR,
    EN_TRASLADO,                // Inicio de ruta: URL de mapa interactivo
    ENTREGADA,                  // Comprobante de entrega (Fecha y hora y camion responsable)
    ENTREGA_FALLIDA,            // Justificacion
    VENCIDA
}
