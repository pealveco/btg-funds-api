# BTG Funds API

API REST para la gestión de fondos de inversión, desarrollada como solución para la prueba técnica de Backend.
Permite a los clientes suscribirse a fondos, cancelar suscripciones y consultar su historial de transacciones.

---

## 📌 Descripción del problema

Se requiere construir una plataforma que permita a los clientes gestionar sus fondos de inversión sin necesidad de intervención de un asesor.

### Funcionalidades principales:
- Suscribirse a un fondo
- Cancelar una suscripción
- Consultar historial de transacciones
- Recibir notificaciones (email o SMS)

### Reglas de negocio:
- Saldo inicial del cliente: **COP $500.000**
- Cada fondo tiene un monto mínimo de vinculación
- Cada transacción tiene un identificador único
- Al cancelar una suscripción, el saldo se reintegra
- Si no hay saldo suficiente, se debe informar al usuario

---

## 🏗️ Arquitectura

El proyecto está basado en **Clean Architecture (Hexagonal)**, lo que permite separar claramente la lógica de negocio de la infraestructura.

### Estructura del proyecto:

```
├── domain            # Entidades y contratos del negocio
├── usecase          # Casos de uso (lógica de aplicación)
├── entry-points     # API REST (controladores)
├── driven-adapters  # Persistencia, notificaciones, etc.
```

### Principios aplicados:
- Separación de responsabilidades
- Inversión de dependencias
- Código desacoplado de frameworks
- Testabilidad

---

## 🧠 Modelo de dominio

### Entidades principales:
- **Client**
- **Fund**
- **Subscription**
- **Transaction**

### Enumeraciones:
- `TransactionType` (SUBSCRIPTION, CANCELLATION)
- `NotificationChannel` (EMAIL, SMS)

---

## 🗄️ Modelo de datos (NoSQL)

Se propone un modelo basado en documentos (DynamoDB o equivalente), optimizado para consultas por cliente.

### Colecciones:

#### clients
- clientId
- name
- email
- phone
- notificationPreference
- availableBalance

#### funds
- fundId
- name
- minimumAmount
- category

#### subscriptions
- subscriptionId
- clientId
- fundId
- amount
- status
- createdAt
- cancelledAt

#### transactions
- transactionId
- clientId
- fundId
- type
- amount
- createdAt

---

## 🚀 API REST

### Endpoints principales:

#### Suscribirse a un fondo
```
POST /api/v1/funds/subscriptions
```

#### Cancelar suscripción
```
POST /api/v1/funds/subscriptions/{subscriptionId}/cancel
```

#### Consultar historial de transacciones
```
GET /api/v1/clients/{clientId}/transactions
```

#### Listar fondos disponibles
```
GET /api/v1/funds
```

---

## ⚙️ Manejo de errores

Se implementa un manejo centralizado de excepciones:

Ejemplo de respuesta:

```json
{
  "code": "INSUFFICIENT_BALANCE",
  "message": "No tiene saldo disponible para vincularse al fondo",
  "timestamp": "2025-06-20T10:00:00"
}
```

---

## 🔔 Notificaciones

El sistema envía notificaciones al cliente al momento de suscribirse a un fondo:

- Email
- SMS

La implementación se abstrae mediante un `NotificationGateway`, permitiendo cambiar fácilmente el proveedor.

---

## 🔐 Seguridad

Se define una estrategia basada en:

- Autenticación mediante JWT
- Autorización por roles (CLIENT)
- Encriptación en tránsito (HTTPS)
- Encriptación en reposo (AWS DynamoDB)

> Nota: Para esta prueba, se puede simular el esquema de autenticación.

---

## 🧪 Pruebas

Se incluyen pruebas unitarias enfocadas en los casos de uso principales:

- Suscripción exitosa
- Validación de saldo insuficiente
- Cancelación de suscripción
- Consulta de historial

---

## 🧩 SQL (Parte 2)

Se incluye la solución a la consulta SQL solicitada en:

```
/sql/solution.sql
```

---

## ▶️ Cómo ejecutar el proyecto

### Requisitos:
- Java 17
- Gradle

### Ejecutar:
```bash
./gradlew bootRun
```

---

## 🧪 Ejecutar pruebas

```bash
./gradlew test
```

---

## ☁️ Despliegue

El proyecto está diseñado para ser desplegado en AWS utilizando:

- AWS CloudFormation
- DynamoDB
- API Gateway (opcional)
- Lambda o ECS (opcional)

---

## 📌 Decisiones técnicas

- Uso de Clean Architecture para desacoplar lógica de negocio
- Modelo NoSQL orientado a acceso por cliente
- Separación de capas para facilitar testabilidad
- Uso de DTOs para desacoplar API del dominio

---

## 🚀 Mejoras futuras

- Implementación completa de seguridad con JWT
- Integración real con servicios de email/SMS
- Uso de eventos (event-driven architecture)
- Implementación de CQRS
- Observabilidad (logs, métricas, tracing)

---

## 👨‍💻 Autor

Desarrollado por **Piter Velasquez**