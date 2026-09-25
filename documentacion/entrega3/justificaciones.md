# Justificaciones de Diseño — Entrega 3: Persistencia

Alcance de este documento: microservicio de **Donaciones**. El mapeo se hace con **JPA (Hibernate)** más `jpa-extras`, sobre HSQLDB en memoria para testing.

Ver diagramas: [diagrama_clases.puml](./diagrama_clases.puml) (completo), [dc_donante.puml](./dc_donante.puml), [dc_notificacion.puml](./dc_notificacion.puml), [dc_donacion.puml](./dc_donacion.puml), [dc_entidad_necesidad.puml](./dc_entidad_necesidad.puml) y el modelo físico en [DER_donaciones.puml](./DER_donaciones.puml).

## Esquema propio por servicio

* Cada microservicio tiene su propia unidad de persistencia y su propio esquema: Donaciones no comparte entidades ni tablas con Logística.
* Donde Logística necesita una donación, guarda su propia referencia por id, no la entidad `Donacion`. No hay FK entre esquemas.

## Identidad

* Las entidades con identidad propia usan `id: Long` y, cuando corresponde, `GenerationType.IDENTITY`: la base genera el valor y el dominio no arrastra lógica de numeración. `Contacto` usa una clave compuesta y `SugerenciaAsignacion` usa como id el de su donación.
* Hasta la entrega 2 la identidad de objeto alcanzaba para el dominio, pero los recursos REST ya exponían ids. Con el ORM el id pasa a ser el mismo en ambos lados, en vez de una secuencia propia del repositorio en memoria.
* `Notificacion` y `PersonaAdministradora` se persisten porque el historial de avisos y sus destinatarios deben sobrevivir a un reinicio. `SugerenciaAsignacion` también se persiste para permitir la revisión administrativa; el mapa completo de entidades por algoritmo permanece transitorio. Las estrategias de asignación no se persisten porque son reglas de cálculo, no datos del negocio.

## Herencia de `Persona`: tabla única

* Estrategia `SINGLE_TABLE` con discriminador `tipo` (`HUMANA` / `JURIDICA`).
* **Por qué:** las subclases agregan pocas columnas y las dos se consultan siempre juntas — la importación masiva de CSV recorre donantes sin distinguir el tipo, y `Donacion.donante_id` apunta a cualquiera de los dos. Con una sola tabla no hace falta ni un join ni un union por lectura.
* **Alternativas descartadas:**
  * `JOINED`: normaliza mejor (sin columnas nullables), pero paga un join en cada lectura de donante, incluido el alta masiva de 20.000 filas.
  * `TABLE_PER_CLASS`: rompe la FK única de `donacion.donante_id`, porque no habría una sola tabla a la cual apuntar.
* **Costo asumido:** `apellido`, `edad`, `genero`, `direccion`, `rubro` y `razon_social` quedan nullables en `persona`. La invariante la sigue sosteniendo el constructor de cada subclase, no la base.

## Herencia de `Contacto`: tabla única

* También `SINGLE_TABLE`, con discriminador `tipo_contacto` (`EMAIL` / `SMS` / `WHATSAPP`).
* **Por qué:** las subclases **no agregan estado**, solo redefinen `enviar(mensaje)` con el proveedor correspondiente. Tres tablas idénticas con una sola columna `valor` no aportarían nada.

## Clave compuesta de `Contacto`

* `Contacto` se identifica por `(notificable_id, tipo_notificable)`, mapeado con `@IdClass(ContactoId)`.
* **Por qué:** `Notificable` es una interfaz que implementan dos jerarquías sin superclase común (`Persona` y `EntidadBeneficiaria`), más `PersonaAdministradora`. No existe una tabla única a la que una FK simple pueda apuntar, así que `tipo_notificable` indica de qué lado viene el id.
* **Alternativa descartada:** una tabla de contactos por dueño (`contacto_persona`, `contacto_entidad`). Duplica la estructura y obliga a repetir el mapeo de la jerarquía completa en cada una.

## Enums persistidos como texto

* Todos los enums (`TipoDoc`, `Genero`, `RazonSocial`, `EstadoDonacion`, `EstadoBien`, `Subcategoria`, `Periodo`) usan `@Enumerated(EnumType.STRING)`.
* **Por qué:** con `ORDINAL`, agregar o reordenar una constante corrompe los datos ya guardados. Además el texto hace legible la base sin consultar el código.
* `Subcategoria` y `Categoria` siguen siendo enums del dominio y **no** tablas: su contenido es fijo, lo define el sistema y no lo administra ninguna persona usuaria. `Subcategoria` guarda su categoría, si requiere estado y si requiere vencimiento como parte de la constante.

## `Necesidad` y la política de renovación

* `PoliticaDeRenovacion` es polimórfica (`SinRenovacion` / `RenovacionPeriodica`) pero **no tiene identidad propia**: es una regla, no un dato que se consulte por sí solo.
* **Decisión:** `Necesidad` persiste `fecha_inicio`, `fecha_fin` y `periodo`, y reconstruye la política en `getRenovacion()`: si no hay período, es `SinRenovacion`; si lo hay, `RenovacionPeriodica`. La política queda `@Transient`.
* **Por qué:** conserva el polimorfismo en el dominio sin sumar una jerarquía de tablas para dos clases que entre las dos tienen tres atributos.
* **Alternativa descartada:** tabla `politica_renovacion` con herencia propia: tres tablas y un join más para reconstruir una regla que se deduce de un único campo nullable.

## Cantidades por subcategoría

* `cantidadesRequeridas` y `cantidadesSuplidas` son `Map<Subcategoria, Integer>` mapeados con `@ElementCollection` y `@MapKeyEnumerated`, en las tablas `cantidad_requerida` y `cantidad_suplida`, con PK compuesta `(necesidad_id, subcategoria_id)`.
* **Por qué:** una fila por subcategoría requerida cumple la primera forma normal y deja que el conjunto de subcategorías crezca sin cambiar el esquema. La alternativa —una columna por subcategoría en `necesidad`— obligaría a un ALTER TABLE por cada constante nueva.
* Se mantienen **dos tablas separadas** en vez de una sola con dos columnas porque lo requerido lo declara la entidad y lo suplido lo acumulan las donaciones: son dos hechos con orígenes distintos.

## Colecciones simples

* `correosRepresentantes` de `EntidadBeneficiaria` se mapea con `@ElementCollection` a la tabla `correo_representante`.
* **Por qué:** guardar los correos concatenados en una columna rompe la 1FN y hace imposible buscar por correo.

## Historial de estados

* `Donacion` es la raíz del agregado: `historialEstados` se mapea con `@OneToMany` + `@JoinColumn(donacion_id)`, `cascade = ALL` y `orphanRemoval = true`.
* **Por qué:** un `RegistroCambioEstado` no existe sin su donación; se crea y se borra con ella. La trazabilidad que pide el enunciado es parte del agregado, no una entidad independiente.
* El `Bien` se mapea `@OneToOne` con cascada desde `Donacion` por la misma razón: la segmentación deja exactamente un bien por donación.

## Contactos

* `contactos` y `medioPredeterminado` siguen siendo `@Transient` en `Persona`, `EntidadBeneficiaria` y `PersonaAdministradora`. La tabla `contacto` se consulta mediante `ContactoRepository`; al reconstruir se usa el primer contacto como medio predeterminado, porque el modelo actual no persiste cuál fue seleccionado. Si no hay contactos persistidos, se conserva la colección que el objeto ya tuviera en memoria; la creación y actualización de contactos todavía requiere persistirlos explícitamente.

## Consideraciones de diseño relacional

* **Normalización:** el esquema está en 3FN salvo por dos desnormalizaciones explícitas y justificadas arriba — las columnas nullables de la tabla única `persona`, y `fecha_fin` de `necesidad`, que es derivable de `fecha_inicio` + `periodo` pero se guarda para poder filtrar necesidades vencidas desde una consulta.
* **Integridad referencial:** `donacion.donante_id` es obligatorio (una donación siempre tiene donante) y `donacion.entidad_id` es nullable, porque la donación existe en depósito antes de ser asignada.
* **Cardinalidades:** `Donacion` con `Bien` es 1 a 1 por la segmentación automática; `EntidadBeneficiaria` con `Necesidad` y `Donacion` con `RegistroCambioEstado` son 1 a N con el hijo dependiente del padre.
* **Enums vs tablas de referencia:** ningún enum tiene tabla propia. La contrapartida es que agregar una subcategoría requiere recompilar; se asume porque el catálogo lo define el sistema y no hay requerimiento de administrarlo en runtime.

## Pendientes y simplificaciones a propósito

* **Persistencia en evolución.** Las entidades están mapeadas y `PersistenciaDominioTest` verifica el mapeo contra HSQLDB. Donaciones, donantes, entidades beneficiarias, administradoras y notificaciones cuentan con repositorios JPA. Los contactos se reconstruyen al recuperar cada notificable consultando `notificable_id` y `tipo_notificable`.
* **Contactos:** `contactos` y `medioPredeterminado` siguen siendo `@Transient` en `Persona`, `EntidadBeneficiaria` y `PersonaAdministradora`. La tabla `contacto` se consulta mediante `ContactoRepository`; al reconstruir se usa el primer contacto como medio predeterminado, porque el modelo actual no persiste cuál fue seleccionado. Si no hay contactos persistidos, se conserva la colección que el objeto ya tuviera en memoria; la creación y actualización de contactos todavía requiere persistirlos explícitamente.
* **`RegistroCambioEstado` usa `java.util.Date`** y sigue siendo genérica (`<T>`) aunque solo se instancia con `EstadoDonacion`. Ambas cosas vienen marcadas en las correcciones de la entrega 2.
* **Configuración de base:** la unidad de persistencia de producción usa MySQL y la de testing usa HSQLDB en memoria. La configuración cliente-servidor depende de que MySQL esté disponible en el entorno local.
* `Notificacion` referencia al receptor mediante `receptor_id` y `tipo_receptor`. Como `Notificable` es una interfaz implementada por varias entidades, esa referencia es lógica y no una FK SQL directa.
