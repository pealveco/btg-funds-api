# Analysis - Parte 2

Este documento analiza el modelo relacional dado por el enunciado de la Parte 2.

No diseña tablas nuevas ni modifica el modelo; su objetivo es identificar correctamente llaves, relaciones y la forma en que el modelo soporta la consulta SQL pedida.

## 2.1 Descripcion general del modelo

El modelo representa tres entidades principales:

- `cliente`: personas que pueden inscribirse a productos y visitar sucursales
- `producto`: productos ofrecidos por la entidad
- `sucursal`: oficinas o sedes donde puede existir disponibilidad de productos

Sobre esas entidades base existen tres tablas relacionales:

- `inscripcion`: conecta clientes con productos
- `disponibilidad`: conecta productos con sucursales
- `visitan`: conecta clientes con sucursales e incluye la fecha de visita

En conjunto, el modelo permite responder preguntas como:

- que productos tiene inscritos un cliente
- en que sucursales esta disponible un producto
- que sucursales visita un cliente

## 2.2 Definicion de tablas

### Tabla: cliente

- PK: `id`
- FKs: no tiene

Campos:

- `id` (PK)
- `nombre`
- `apellidos`
- `ciudad`

Rol en el modelo:

- representa la entidad principal del cliente
- se relaciona con `producto` por medio de `inscripcion`
- se relaciona con `sucursal` por medio de `visitan`

### Tabla: producto

- PK: `id`
- FKs: no tiene

Campos:

- `id` (PK)
- `nombre`
- `tipoProducto`

Rol en el modelo:

- representa el catalogo de productos
- se relaciona con `cliente` por medio de `inscripcion`
- se relaciona con `sucursal` por medio de `disponibilidad`

### Tabla: sucursal

- PK: `id`
- FKs: no tiene

Campos:

- `id` (PK)
- `nombre`
- `ciudad`

Rol en el modelo:

- representa las sucursales fisicas
- se relaciona con `producto` por medio de `disponibilidad`
- se relaciona con `cliente` por medio de `visitan`

### Tabla: inscripcion

- PK compuesta:
  - `idProducto`
  - `idCliente`
- FKs:
  - `idProducto -> producto.id`
  - `idCliente -> cliente.id`

Campos:

- `idProducto` (PK, FK)
- `idCliente` (PK, FK)

Rol en el modelo:

- representa la inscripcion de un cliente a un producto
- materializa una relacion muchos a muchos entre `cliente` y `producto`

### Tabla: disponibilidad

- PK compuesta:
  - `idSucursal`
  - `idProducto`
- FKs:
  - `idSucursal -> sucursal.id`
  - `idProducto -> producto.id`

Campos:

- `idSucursal` (PK, FK)
- `idProducto` (PK, FK)

Rol en el modelo:

- indica en que sucursales esta disponible cada producto
- materializa una relacion muchos a muchos entre `sucursal` y `producto`

### Tabla: visitan

- PK compuesta:
  - `idSucursal`
  - `idCliente`
- FKs:
  - `idSucursal -> sucursal.id`
  - `idCliente -> cliente.id`
- Campo adicional:
  - `fechaVisita`

Campos:

- `idSucursal` (PK, FK)
- `idCliente` (PK, FK)
- `fechaVisita`

Rol en el modelo:

- registra la relacion entre clientes y sucursales visitadas
- añade el atributo `fechaVisita` a esa relacion

## 2.3 Relaciones entre tablas

### cliente ↔ inscripcion ↔ producto

- un cliente puede tener muchas inscripciones
- un producto puede estar inscrito por muchos clientes
- `inscripcion` resuelve la relacion muchos a muchos

Cardinalidad:

- `cliente (1) -> (N) inscripcion`
- `producto (1) -> (N) inscripcion`

### producto ↔ disponibilidad ↔ sucursal

- un producto puede estar disponible en muchas sucursales
- una sucursal puede ofrecer muchos productos
- `disponibilidad` resuelve la relacion muchos a muchos

Cardinalidad:

- `producto (1) -> (N) disponibilidad`
- `sucursal (1) -> (N) disponibilidad`

### cliente ↔ visitan ↔ sucursal

- un cliente puede visitar muchas sucursales
- una sucursal puede ser visitada por muchos clientes
- `visitan` resuelve la relacion muchos a muchos

Cardinalidad:

- `cliente (1) -> (N) visitan`
- `sucursal (1) -> (N) visitan`

## 2.4 Mapa de relaciones

```text
cliente --< inscripcion >-- producto --< disponibilidad >-- sucursal
   |
   +----< visitan >----------------------------------------+
```

Otra forma de leerlo:

```text
cliente(id)
  ├─< inscripcion(idCliente, idProducto) >─ producto(id)
  └─< visitan(idCliente, idSucursal, fechaVisita) >─ sucursal(id)

producto(id)
  └─< disponibilidad(idProducto, idSucursal) >─ sucursal(id)
```

## 2.5 Implicaciones para la consulta

La consulta pedida busca clientes que:

- tengan inscrito algun producto
- y ese producto este disponible solo en sucursales que el cliente visita

Eso implica conectar el modelo asi:

1. `cliente` se conecta con `producto` por `inscripcion`
2. el producto inscrito se conecta con `sucursal` por `disponibilidad`
3. el cliente se conecta con `sucursal` por `visitan`

En consecuencia, para resolver la query seran necesarias al menos estas tablas:

- `cliente`
- `inscripcion`
- `disponibilidad`
- `visitan`
- probablemente `producto` y/o `sucursal` segun el estilo de la consulta, aunque no siempre sean estrictamente necesarias para proyectar el resultado

Punto clave del razonamiento:

- no basta con verificar que un cliente visita alguna sucursal donde existe el producto
- hay que validar que no exista una sucursal con disponibilidad del producto que quede fuera del conjunto de sucursales visitadas por ese cliente

Eso sugiere que la logica final de la consulta tendra que comparar conjuntos de sucursales:

- conjunto de sucursales donde el producto esta disponible
- conjunto de sucursales que visita el cliente

La forma mas defendible de hacerlo, conceptualmente, sera una validacion de inclusion o subconjunto, por ejemplo mediante `NOT EXISTS`, aunque esa decision se dejara para la historia donde se escriba la consulta final.

## 2.6 Interpretacion de la regla de negocio

Esta seccion descompone la frase del enunciado para fijar una interpretacion precisa antes de escribir la consulta.

### 2.6.1 Descomposicion semantica de la consulta

#### A. "Clientes que tienen inscrito algun producto"

Esto debe interpretarse asi:

- el cliente debe existir en `cliente`
- el cliente debe aparecer en `inscripcion`
- debe existir al menos una relacion efectiva cliente-producto

Consecuencia:

- no basta con que el cliente visite sucursales
- no basta con que exista disponibilidad de productos en las sucursales que visita
- la condicion de entrada es que el cliente tenga al menos un producto inscrito

#### B. "Producto disponible solo en las sucursales que visitan"

La palabra clave es `solo`.

Interpretacion correcta:

- para un producto `P` inscrito por un cliente `C`
- todas las sucursales donde `P` esta disponible
- deben pertenecer al conjunto de sucursales que `C` visita

Dicho de otra forma:

- no puede existir una sucursal donde el producto este disponible y que el cliente no visite

Interpretaciones incorrectas que podrian confundirse con la frase:

- "que exista al menos una sucursal visitada donde el producto este disponible"
  - demasiado debil, porque no controla las demas sucursales donde el producto tambien podria estar disponible
- "que el cliente visite todas las sucursales del sistema"
  - incorrecto, porque solo importan las sucursales donde esta disponible el producto inscrito
- "que el producto este disponible en una sola sucursal"
  - incorrecto, el producto puede estar en varias sucursales mientras todas pertenezcan al conjunto visitado por el cliente

### 2.6.2 Formalizacion logica

Una formulacion semiform al de la regla seria:

Un cliente `C` califica si existe un producto `P` inscrito por `C` tal que no exista una sucursal `S` donde `P` este disponible y `C` no visite `S`.

Forma equivalente:

- existe `P` inscrito por `C`
- y para toda sucursal `S` donde `P` esta disponible, `C` visita `S`

La segunda forma es util para entender la regla.
La primera suele ser mas natural para traducir a SQL mediante `NOT EXISTS`.

## 2.7 Evaluacion de operadores SQL

### JOIN

Si, se necesitan `JOIN` o equivalentes logicos entre tablas relacionadas.

Tablas relevantes:

- `cliente`
- `inscripcion`
- `disponibilidad`
- `visitan`
- `producto` puede aparecer si mejora legibilidad, aunque no siempre es indispensable

Uso esperado:

- conectar cliente con sus productos inscritos
- conectar cada producto con sus sucursales disponibles
- comparar esas sucursales con las que visita el cliente

### GROUP BY / HAVING

No parecen ser la opcion mas clara para esta regla.

Seria posible construir una solucion por agregacion, comparando conteos de sucursales o conjuntos derivados, pero eso introduce mas complejidad y riesgo:

- puede requerir `COUNT(DISTINCT ...)`
- puede volverse menos legible
- puede ocultar errores semanticos si los conteos coinciden por casualidad pero no por inclusion real de conjuntos

Por claridad y defensa tecnica, no es la estrategia preferida.

### EXISTS

Si, `EXISTS` es util para expresar la condicion:

- existe al menos una inscripcion valida de un cliente a un producto

Eso encaja bien con la frase "tienen inscrito algun producto".

### NOT EXISTS

Si, `NOT EXISTS` parece ser el operador clave.

Sirve para expresar la ausencia de sucursales prohibidas:

- no debe existir una sucursal donde el producto este disponible
- y que no este dentro de las sucursales visitadas por el cliente

Esta formulacion refleja con mucha precision la palabra `solo`.

## 2.8 Estrategia SQL elegida

La estrategia mas clara y defendible es:

- usar `EXISTS` para asegurar que el cliente tiene inscrito al menos un producto
- dentro de esa logica, usar `NOT EXISTS` para garantizar que no exista una sucursal donde el producto este disponible y el cliente no la visite
- usar `JOIN` o subconsultas correlacionadas para conectar `cliente`, `inscripcion`, `disponibilidad` y `visitan`
- evitar `GROUP BY / HAVING` porque no aportan mas claridad que una formulacion por existencia y ausencia

### Conclusión tecnica

La regla de negocio se entiende mejor como una condicion de subconjunto:

- sucursales donde el producto esta disponible
- debe ser subconjunto de sucursales que visita el cliente

Por eso, la forma SQL mas defendible para la historia siguiente sera una combinacion de:

- `EXISTS`
- `NOT EXISTS`
- joins o correlaciones claras entre tablas

Sin necesidad, por ahora, de escribir la consulta final completa.

## 2.9 Materializacion de la estrategia

La consulta final de esta Parte 2 ya quedo implementada en `solution.sql` con la estrategia elegida:

- `JOIN` entre `cliente`, `inscripcion` y `producto`
- `EXISTS` para asegurar que el producto inscrito realmente tenga disponibilidad
- `NOT EXISTS` para descartar sucursales donde el producto esta disponible pero el cliente no visita
- `DISTINCT` para evitar nombres duplicados cuando un mismo cliente cumple la condicion con mas de un producto

## 2.10 Validacion logica de la consulta

### Falsos positivos evitados

La consulta final evita estos falsos positivos:

- clientes sin inscripcion
  - no aparecen porque la base de la consulta parte de `inscripcion`
- clientes con productos inscritos disponibles en alguna sucursal que no visitan
  - no aparecen porque el `NOT EXISTS` falla en cuanto existe una sucursal disponible no visitada
- clientes con productos inscritos pero sin disponibilidad real
  - no aparecen porque se exige `EXISTS` sobre `disponibilidad`
- clientes que solo cumplen una interpretacion debil de la regla
  - por ejemplo, visitar una sucursal donde el producto esta disponible no basta si existe otra sucursal disponible no visitada

### Casos validos preservados

La consulta no excluye clientes validos por error cuando se cumple la regla correcta:

- si un cliente tiene un producto inscrito
- y todas las sucursales donde ese producto esta disponible pertenecen al conjunto de sucursales que visita
- entonces el `NOT EXISTS` se satisface correctamente y el cliente es retornado

Ademas:

- `DISTINCT` evita nombres repetidos cuando un cliente califica por varios productos
- pero no elimina clientes validos; solo elimina duplicacion en la salida

### Confirmacion de la interpretacion de "solo"

La interpretacion aplicada y validada es esta:

Para un producto inscrito por un cliente, todas las sucursales donde ese producto esta disponible deben pertenecer al conjunto de sucursales que ese cliente visita.

Equivalente logico:

- no debe existir una sucursal donde el producto este disponible y el cliente no la visite

Esta es la interpretacion correcta porque:

- respeta el sentido de inclusion total contenido en la palabra `solo`
- evita la lectura debil de "al menos una coincidencia"
- no exige condiciones ajenas al producto, como visitar todas las sucursales del sistema

### Por que `NOT EXISTS` resuelve correctamente la condicion

`NOT EXISTS` es la forma mas clara de expresar una restriccion de ausencia:

- se busca si existe una sucursal "prohibida"
- es decir, una sucursal donde el producto esta disponible
- pero el cliente no la visita

Si esa sucursal prohibida existe, el cliente no califica.
Si no existe, entonces todas las sucursales relevantes quedan cubiertas por las visitas del cliente.

### Conclusión defendible

La consulta final es logicamente correcta porque combina:

- `JOIN` para asegurar la relacion cliente-producto inscrito
- `EXISTS` para exigir disponibilidad real del producto
- `NOT EXISTS` para validar la condicion de subconjunto entre disponibilidad y visitas
- `DISTINCT` para presentar el resultado sin duplicados

Con esto:

- no se devuelven clientes por coincidencias parciales
- no se pierden clientes validos
- la lectura de la palabra `solo` queda implementada de manera rigurosa
