package donaciones.domain;

public enum EstadoDonacion {
    EN_DEPOSITO("En depósito"),
    ASIGNADA("Asignación realizada"),                   // Nada
    LISTA_PARA_ENTREGAR("Lista para entregar"),
    EN_TRASLADO("En traslado"),                         // Inicio de ruta: URL de mapa interactivo
    ENTREGADA("Entregada"),                             // Comprobante de entrega (Fecha y hora y camion responsable)
    ENTREGA_FALLIDA("Entrega fallida"),                 // Justificacion
    VENCIDA("Vencida");

    private final String nombre;

    EstadoDonacion(String nombre) {
        this.nombre = nombre;
    }

    public String nombre() {
        return this.nombre;
    }
}
