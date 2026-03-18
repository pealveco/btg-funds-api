# 📌 Parte 2 – SQL

Ruta desde el root del repositorio: `sql/parte-2/`

## 🧩 Objetivo

Resolver la consulta SQL pedida por el enunciado sobre la base de datos relacional `BTG`:

> Obtener los nombres de los clientes que tienen inscrito algún producto disponible únicamente en las sucursales que visitan.

El resultado esperado es una lista de nombres de clientes que cumplen exactamente esa regla.

## 🧠 Interpretación del enunciado

La lectura correcta del problema es esta:

- un cliente debe tener al menos un producto inscrito en `inscripcion`
- ese producto debe tener disponibilidad real en al menos una sucursal
- todas las sucursales donde ese producto está disponible deben pertenecer al conjunto de sucursales que el cliente visita

La palabra `solo` no significa:

- que exista una sola sucursal
- que el cliente visite todas las sucursales del sistema
- que exista al menos una coincidencia entre una sucursal visitada y una sucursal con disponibilidad

Sí significa:

- que no exista una sucursal donde el producto esté disponible y el cliente no la visite

## ⚙️ Estrategia de solución

La solución implementada sigue esta estrategia:

- `JOIN` entre `cliente`, `inscripcion` y `producto` para partir de clientes con productos inscritos
- `EXISTS` para asegurar que el producto inscrito realmente tiene disponibilidad
- `NOT EXISTS` para expresar la condición clave de la regla:
  - no debe existir una sucursal donde el producto esté disponible y el cliente no la visite
- `DISTINCT` para evitar duplicados si un cliente cumple la condición con más de un producto

No se usó `GROUP BY` / `HAVING` porque para esta regla la formulación con `EXISTS` y `NOT EXISTS` es más clara, más directa y más fácil de defender técnicamente.

## 🧮 Consulta SQL

La consulta final está en:

- [solution.sql](/home/certhakzu/Documentos/Ceiba/tecnical%20test/btg-funds-api/sql/parte-2/solution.sql)

El análisis técnico y la interpretación detallada están en:

- [analysis.md](/home/certhakzu/Documentos/Ceiba/tecnical%20test/btg-funds-api/sql/parte-2/analysis.md)

## ✅ Validación lógica

La consulta es defendible porque:

- no devuelve clientes sin inscripción
- no devuelve clientes si el producto inscrito está disponible en una sucursal que no visitan
- no acepta falsos positivos por productos inscritos sin disponibilidad, porque exige `EXISTS` sobre `disponibilidad`
- no excluye casos válidos cuando todas las sucursales donde el producto está disponible sí están dentro del conjunto que visita el cliente

En términos lógicos, la solución implementa una condición de subconjunto:

- sucursales donde el producto está disponible
- deben ser subconjunto de sucursales que visita el cliente

## 📂 Estructura de la solución

- `solution.sql`
  - consulta final lista para ejecutar
- `analysis.md`
  - análisis del modelo, interpretación de la regla y validación lógica
- `README.md`
  - documentación resumida y orientada a evaluación

## ⚠️ Nota importante

Esta Parte 2 es completamente independiente de la Parte 1 del repositorio.

No usa:

- Java
- Spring Boot
- DynamoDB
- lógica de la API

Está basada únicamente en el modelo relacional dado en el enunciado de la prueba técnica.
