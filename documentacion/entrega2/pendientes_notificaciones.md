# Lista de Pendientes: Disparadores de Notificaciones (Entrega 2)

## 1. Módulo de Donaciones y Asignaciones

- [ ] **Donación asignada (Entidad Beneficiaria):**
  - **Cuándo ocurre:** Al confirmarse el destino final de una donación que estaba en estado *"En Depósito"*.
  - **Acción sugerida:** `entidadBeneficiaria.notificar("Se le ha asignado satisfactoriamente una nueva donación.");`

- [ ] **Donación asignada (Persona Donante):**
  - **Cuándo ocurre:** Simultáneamente al confirmarse la asignación del punto anterior.
  - **Acción sugerida:** `donante.notificar("Su donación ha sido asignada a la entidad: " + entidadBeneficiaria.getRazonSocial());`

---

## 2. Módulo de Logística y Trazabilidad

- [ ] **Inicio de ruta:**
  - **Cuándo ocurre:** Cuando el chofer informa en el sistema el comienzo de su recorrido (cambio a estado *"En traslado"*).
  - **Acción sugerida:** Notificar a todas las entidades y donantes que forman parte de la ruta adjuntando el enlace al mapa de seguimiento en tiempo real.

- [ ] **Entrega realizada con éxito:**
  - **Cuándo ocurre:** Cuando la entidad beneficiaria confirma la recepción en la plataforma (cambio a estado *"Entregada"*).
  - **Acción sugerida:** Notificar a la entidad y al donante enviando el comprobante de entrega (indicando fecha, hora y camión responsable).

- [ ] **Entrega no satisfactoria:**
  - **Cuándo ocurre:** Cuando una entrega falla (por vencimiento, incidente logístico, ausencia, etc.).
  - **Acción sugerida:** Notificar a la entidad beneficiaria, a la persona donante y a la administración del sistema incluyendo la justificación del incidente.

---