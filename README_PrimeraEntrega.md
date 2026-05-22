# Carga CSV - Uso de Optional

* Usamos Optional para no utilizar null y luego filtrar en la lista.

# Gestión de Donaciones

* **Principio de Responsabilidad Única (SRP)**: `DonacionEntrante` se encarga únicamente de modelar una donación con sus bienes asociados. La segmentación de bienes en donaciones independientes es un comportamiento separado que ocurre en el constructor. `DonacionIndependiente` gestiona el estado de un bien único y su disponibilidad, mientras que `SistemaDonaciones` orquesta la gestión del stock global.

* **Patrón Strategy**: El método `segmentarEnIndependientes()` en `DonacionEntrante` agrupa bienes según sus características (comparteSegmentoCon), permitiendo diferentes formas de segmentación sin modificar el dominio.

* **Patrón State**: `EstadoDonacionIndependiente` define los estados posibles (EN_DEPOSITO, ENTREGADA, VENCIDA) y `DonacionIndependiente` gestiona transiciones y mantenimiento de historial con `RegistroCambioEstado`.

* **Arquitectura en Capas**: La capa de dominio (`DonacionEntrante`, `DonacionIndependiente`) separa el modelado de datos del comportamiento orquestado en el `SistemaDonaciones`.

# Gestión de Personas

* **Principio de Responsabilidad Única (SRP)**: Cada clase tiene una responsabilidad clara: `Persona` define el comportamiento base, `PersonaHumana` añade atributos específicos de humanos (género, edad, dirección), y `PersonaJuridica` gestiona la información de entidades (razón social, rubro, representantes).

* **Principio de Sustitución de Liskov (LSP)**: Las subclases `PersonaHumana` y `PersonaJuridica` respetan el contrato establecido por `Persona`, permitiendo que sean usadas intercambiablemente en el sistema.

* **Patrón Template Method**: El método `actualizarDatos(Persona)` puede implementarse de manera especializada en cada subclase sin alterar el contrato general.

* **Arquitectura Polimórfica**: El diseño permite que el sistema trabaje con referencias de `Persona` sin conocer el tipo específico (humana o jurídica), facilitando extensibilidad futura.

# Gestión de Necesidades

* **Principio Abierto/Cerrado (OCP)**: `Necesidad` es una clase abstracta que establece la estructura base. `NecesidadRecurrente` (necesidades periódicas) y `NecesidadExtra` (necesidades puntuales) extienden este comportamiento sin modificar la clase base, permitiendo agregar nuevos tipos de necesidades fácilmente.

* **Principio de Responsabilidad Única (SRP)**: Cada tipo de necesidad gestiona su propia lógica de resolución. `NecesidadRecurrente` genera el siguiente período automáticamente, mientras que `NecesidadExtra` se resuelve sin generar nuevas instancias.

* **Patrón Template Method**: La clase abstracta define `resolver()` como método abstracto que cada subclase implementa según su lógica específica. Ambas también cuentan con `actualizar()` que puede ser sobrescrito.

* **Arquitectura Orientada a Objetos**: El sistema de rastreo de cantidades pendientes vs. suplidas permite una lógica compleja sin conocer el tipo específico de necesidad, promoviendo la reutilización de código.

# Gestión de Asignaciones

* **Principio de Responsabilidad Única (SRP)**: `Asignacion` gestiona la asignación de bienes a una necesidad específica. `RegistroCambioEstado` se encarga únicamente de registrar el historial de cambios. `AsignacionItem` actúa como un contenedor de datos inmutable (record) para las asociaciones.

* **Patrón State**: `EstadoAsignacion` define los estados posibles (ASIGNACION_REALIZADA, LISTA_ENTREGA, EN_TRASLADO, ENTREGADA, ENTREGA_FALLIDA) y `Asignacion.setEstado()` gestiona las transiciones y validaciones (ej: ENTREGA_FALLIDA requiere justificación).

* **Event Sourcing Simplificado**: `Asignacion` mantiene un historial completo de cambios de estado en `historialEstados`, permitiendo auditoría y trazabilidad de todas las transiciones, incluyendo fechas y justificaciones.

* **Arquitectura Basada en Eventos**: Cada cambio de estado genera un registro inmutable que documenta el antes, el después, la fecha y la razón, facilitando debugging y cumplimiento normativo.

# Componente Notificador

* **Principio de Responsabilidad Única (SRP)**: La clase `Contacto` solo se ocupa de modelar y validar datos. La lógica de envío es una responsabilidad de comportamiento distinta, que está en la capa de servicio.

* **Patrón Strategy y Principio Abierto/Cerrado (OCP)**: Usamos el patrón Strategy para encapsular los diferentes métodos de envío. Esto nos permite añadir nuevas formas de notificación (como Telegram) sin modificar el código existente, respetando el OCP.

* **Arquitectura en Capas**: Este diseño mantiene una clara separación entre la capa de dominio y la capa de servicio, asegurando que las dependencias fluyen en la dirección correcta.