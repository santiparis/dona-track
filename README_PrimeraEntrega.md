# Carga CSV - Uso de Optional

* Usamos Optional para no utilizar null y luego filtrar en la lista.

# Componente Notificador

* Principio de Responsabilidad Única (SRP): La clase Contacto solo se ocupa de modelar y validar datos. La lógica de envío es una responsabilidad de comportamiento distinta, que está en la capa de servicio.

* Patrón Strategy y Principio Abierto/Cerrado (OCP): Usamos el patrón Strategy para encapsular los diferentes métodos de envío. Esto nos permite añadir nuevas formas de notificación (como Telegram) sin modificar el código existente, respetando el OCP.

* Arquitectura en Capas: Este diseño mantiene una clara separación entre la capa de dominio y la capa de servicio, asegurando que las dependencias fluyan en la dirección correcta.

[_README_](./README.md)