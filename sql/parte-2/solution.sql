-- Parte 2 - SQL
-- Entregable independiente de la solucion API/NoSQL de la Parte 1.
--
-- Supuesto adoptado:
-- Un cliente califica si existe al menos un producto inscrito por ese cliente
-- tal que no exista una sucursal donde el producto este disponible y el cliente
-- no la visite.
--
-- Aclaracion:
-- Ademas se exige que el producto tenga al menos una fila en `disponibilidad`,
-- para evitar falsos positivos por productos inscritos pero no disponibles.

SELECT DISTINCT
    c.nombre
FROM cliente c
JOIN inscripcion i
    ON i.idCliente = c.id
JOIN producto p
    ON p.id = i.idProducto
WHERE EXISTS (
    -- Garantiza que el producto inscrito realmente esta disponible
    -- en al menos una sucursal.
    SELECT 1
    FROM disponibilidad d0
    WHERE d0.idProducto = p.id
)
AND NOT EXISTS (
    -- Busca una sucursal "prohibida": una sucursal donde el producto inscrito
    -- si esta disponible, pero el cliente no la visita.
    SELECT 1
    FROM disponibilidad d
    WHERE d.idProducto = p.id
      AND NOT EXISTS (
          SELECT 1
          FROM visitan v
          WHERE v.idCliente = c.id
            AND v.idSucursal = d.idSucursal
      )
);
