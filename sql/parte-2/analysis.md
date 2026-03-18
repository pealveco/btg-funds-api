# Analysis - Parte 2

## Modelo

El ejercicio tiene tres tablas principales:

- `cliente`
- `producto`
- `sucursal`

Y tres tablas relacionales:

- `inscripcion(idProducto, idCliente)`
- `disponibilidad(idSucursal, idProducto)`
- `visitan(idSucursal, idCliente, fechaVisita)`

Relaciones:

```text
cliente --< inscripcion >-- producto --< disponibilidad >-- sucursal
cliente --< visitan >-- sucursal
```

## Lectura del enunciado

La consulta pide:

> Obtener los nombres de los clientes que tienen inscrito algún producto disponible solo en las sucursales que visitan.

Lo entendí así:

- el cliente debe tener al menos un producto inscrito
- ese producto debe estar disponible en una o más sucursales
- todas las sucursales donde ese producto está disponible deben estar dentro de las sucursales que visita el cliente

No significa:

- que alcance con una coincidencia parcial
- que el cliente visite todas las sucursales del sistema
- que el producto esté disponible en una sola sucursal

## Idea de solución

La forma más clara me pareció:

- partir de `cliente` + `inscripcion`
- asegurar que el producto tenga disponibilidad real con `EXISTS`
- usar `NOT EXISTS` para descartar una sucursal donde el producto esté disponible y el cliente no la visite
- usar `DISTINCT` para no repetir clientes

En otras palabras:

- el cliente califica si existe un producto inscrito
- y no existe una sucursal "prohibida" para ese producto

## Validación lógica

La consulta final evita falsos positivos porque:

- no devuelve clientes sin inscripción
- no devuelve clientes si el producto aparece en una sucursal que no visitan
- no devuelve productos inscritos sin disponibilidad real

Y no excluye casos válidos porque:

- si todas las sucursales donde el producto está disponible sí están dentro de las que visita el cliente, el cliente sale en el resultado

## Nota

La consulta final está en `solution.sql`.
