# 💰 BTG Funds API

API backend desarrollada en Java 17 + Spring Boot + Clean Architecture con el scaffold de Bancolombia para la gestion de fondos de inversion.

Permite:
- Consultar fondos disponibles
- Suscribirse a un fondo
- Cancelar suscripciones
- Consultar historial de transacciones
- Publicar eventos de notificacion

## 🚀 Tecnologias

- Java 17
- Spring Boot
- Gradle
- Clean Architecture (Scaffold Bancolombia)
- Docker
- AWS:
  - DynamoDB
  - SNS
  - ECR
  - App Runner
  - CloudFormation

## 🧱 Arquitectura

El proyecto sigue Clean Architecture, separando responsabilidades por capas:

```text
.
├── applications
│   └── app-service
├── domain
│   ├── model
│   └── usecase
└── infrastructure
    ├── driven-adapters
    └── entry-points
```

### 🔑 Principios aplicados

- Inversion de dependencias
- Separacion de responsabilidades
- Dominio independiente de infraestructura
- Alta testabilidad

## 📦 Funcionalidades implementadas

### ✅ Historia 2.1 – Consultar fondos

- Endpoint: `GET /funds`
- Retorna la lista completa de fondos disponibles
- Los fondos seed se cargan automaticamente en DynamoDB

### ✅ Historia 2.2 – Suscribirse a un fondo

- Endpoint: `POST /subscriptions`
- Valida:
  - cliente existente
  - fondo existente
  - monto minimo del fondo
  - saldo disponible
  - suscripcion activa duplicada
- Guarda la suscripcion en DynamoDB
- Descuenta saldo del cliente
- Registra transaccion
- Usa el gateway de notificaciones de forma desacoplada

### ✅ Historia 2.3 – Cancelar suscripcion

- Endpoint: `DELETE /subscriptions/{id}`
- Valida:
  - suscripcion existente
  - que no este cancelada previamente
- Implementacion:
  - cancelacion logica
  - `status = CANCELLED`
  - `cancelledAt = timestamp`
- Reintegra saldo al cliente
- Registra transaccion de cancelacion

### ✅ Historia 2.4 – Historial de transacciones

- Endpoint: `GET /transactions?clientId=<CLIENT_ID>`
- Filtra por `clientId`
- Devuelve transacciones de suscripcion y cancelacion
- Ordena por fecha descendente: la mas reciente primero
- Valida `clientId` faltante o vacio con `400`

## 🧠 Decisiones de diseño clave

### Cancelacion logica

La cancelacion es soft delete. No se elimina la suscripcion fisicamente para:

- mantener historial de operaciones
- soportar auditoria y trazabilidad
- evitar perdida de informacion financiera

### Registro de transacciones

Cada operacion relevante genera una transaccion:

- suscripcion
- cancelacion

El historial se consulta por `clientId` y se ordena por `createdAt` descendente en el use case para dejar el comportamiento explicito y estable.

Las transacciones guardan `subscriptionId`, `clientId`, `fundId`, `type`, `amount` y `createdAt`.

### Manejo de errores

La API usa excepciones de use case y un manejador global en `api-rest`.

- `400` para reglas de negocio invalidas
- `400` para payloads invalidos o parametros requeridos faltantes
- `404` para recursos no encontrados
- `409` para conflictos de negocio
- `500` para errores internos o de persistencia

### Logging y trazabilidad

- logging centralizado de casos de uso mediante aspecto en `app-service`
- `GlobalExceptionHandler` con logs `WARN` para errores controlados y `ERROR` para errores inesperados
- correlation id por request usando header `X-Correlation-Id`
- si el header no llega, la API genera un UUID
- el correlation id se devuelve en la respuesta y aparece en logs

## ☁️ Infraestructura AWS

Provisionada con [cloudformation.yaml](/home/certhakzu/Documentos/Ceiba/tecnical%20test/btg-funds-api/deployment/aws/cloudformation.yaml).

### Recursos

- DynamoDB:
  - Clients
  - Funds
  - Subscriptions
  - Transactions
- SNS Topic para notificaciones
- SQS suscrita al topic SNS para inspeccionar eventos publicados
- ECR para imagenes Docker
- App Runner para despliegue
- Lambda para seed de fondos y clientes

## 🌱 Seed de datos

CloudFormation carga automaticamente:

### Fondos iniciales

- `FPV_BTG_PACTUAL_RECAUDADORA`
- `FPV_BTG_PACTUAL_ECOPETROL`
- `DEUDAPRIVADA`
- `FDO-ACCIONES`
- `FPV_BTG_PACTUAL_DINAMICA`

### Clientes iniciales

- `client-001`
- `client-002`

Ambos con saldo inicial de `500000`.

## 🔧 Variables de entorno

Variables usadas por la aplicacion:

```text
AWS_REGION
CLIENTS_TABLE
FUNDS_TABLE
SUBSCRIPTIONS_TABLE
TRANSACTIONS_TABLE
NOTIFICATIONS_TOPIC_ARN
SPRING_APPLICATION_NAME
SERVER_PORT
DYNAMODB_ENDPOINT
SPRING_PROFILES_INCLUDE
```

Notas:

- `DYNAMODB_ENDPOINT` es opcional. Si esta vacio, usa DynamoDB real en AWS.
- `funds.env` es solo para entorno local y no se versiona.

## ▶️ Ejecucion local

### 1. Clonar repositorio

```bash
git clone <repo-url>
cd btg-funds-api
```

### 2. Cargar variables locales

```bash
set -a
source funds.env
set +a
```

### 3. Ejecutar aplicacion

```bash
./gradlew bootRun
```

## 🧪 Pruebas con curl

### Health Check

```bash
curl --location 'http://localhost:8080/actuator/health'
```

### Obtener fondos

```bash
curl --location 'http://localhost:8080/funds' \
  --header 'Accept: application/json'
```

### Suscribirse a un fondo

```bash
curl --location 'http://localhost:8080/subscriptions' \
  --header 'X-Correlation-Id: corr-subscribe-001' \
  --header 'Content-Type: application/json' \
  --header 'Accept: application/json' \
  --data '{
    "clientId": "client-001",
    "fundId": "1",
    "amount": 100000
  }'
```

### Cancelar suscripcion

```bash
curl --location --request DELETE 'http://localhost:8080/subscriptions/sub-001' \
  --header 'X-Correlation-Id: corr-cancel-001'
```

### Consultar historial de transacciones

```bash
curl --location 'http://localhost:8080/transactions?clientId=client-001' \
  --header 'X-Correlation-Id: corr-transactions-001' \
  --header 'Accept: application/json'
```

### Error controlado de ejemplo

```bash
curl --location 'http://localhost:8080/subscriptions' \
  --header 'X-Correlation-Id: corr-error-001' \
  --header 'Content-Type: application/json' \
  --data '{}'
```

## 🌐 Despliegue en App Runner

### Health check

```bash
curl --location 'https://q5d8y4kqwp.us-east-1.awsapprunner.com/actuator/health'
```

### Fondos

```bash
curl --location 'https://q5d8y4kqwp.us-east-1.awsapprunner.com/funds' \
  --header 'Accept: application/json'
```

### Suscribirse

```bash
curl --location 'https://q5d8y4kqwp.us-east-1.awsapprunner.com/subscriptions' \
  --header 'X-Correlation-Id: corr-subscribe-001' \
  --header 'Content-Type: application/json' \
  --header 'Accept: application/json' \
  --data '{
    "clientId": "client-001",
    "fundId": "1",
    "amount": 100000
  }'
```

### Cancelar suscripcion

```bash
curl --location --request DELETE 'https://q5d8y4kqwp.us-east-1.awsapprunner.com/subscriptions/sub-001' \
  --header 'X-Correlation-Id: corr-cancel-001'
```

### Historial de transacciones

```bash
curl --location 'https://q5d8y4kqwp.us-east-1.awsapprunner.com/transactions?clientId=client-001' \
  --header 'X-Correlation-Id: corr-transactions-001' \
  --header 'Accept: application/json'
```

## 🐳 Docker

La imagen se construye con [Dockerfile](/home/certhakzu/Documentos/Ceiba/tecnical%20test/btg-funds-api/deployment/Dockerfile) usando el jar:

`applications/app-service/build/libs/BtgPactualFundsApi.jar`

### Build

```bash
docker build -f deployment/Dockerfile -t btg-funds-api .
```

### Run

```bash
docker run -p 8080:8080 btg-funds-api
```

## 📦 CI/CD

El workflow está en [ci-cd.yml](/home/certhakzu/Documentos/Ceiba/tecnical%20test/btg-funds-api/.github/workflows/ci-cd.yml).

Hace:

- checkout del repo
- setup de Java 17
- `./gradlew clean build`
- validacion del template CloudFormation
- autenticacion en AWS
- resolucion del `EcrRepositoryUri` desde el stack
- build y push de imagen Docker a ECR
- deploy del stack con `AppImageUri` versionado por SHA

### Secrets requeridos

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION`

### Variables globales del workflow

- `AWS_REGION=us-east-1`
- `PROJECT_NAME=btg-funds-api`
- `ENVIRONMENT=dev`
- `STACK_NAME=btg-funds-api-dev`
- `IMAGE_TAG=${{ github.sha }}`

### Como despliega

El pipeline publica una imagen en ECR con tag igual al SHA del commit y actualiza CloudFormation con ese `AppImageUri`. Esto evita el problema de usar `latest` y garantiza que App Runner detecte cambios reales.

## 📊 Modelo de datos

### Subscription

- `subscriptionId`
- `clientId`
- `fundId`
- `amount`
- `status`
- `createdAt`
- `cancelledAt`

### Transaction

- `transactionId`
- `subscriptionId`
- `clientId`
- `fundId`
- `type`
- `amount`
- `createdAt`

## 🧪 Validacion HTTP

En los entry points con payload se usa Bean Validation.

Ejemplos actuales:

- `POST /subscriptions`
  - `clientId` requerido
  - `fundId` requerido
  - `amount` requerido
  - `amount > 0`

- `GET /transactions`
  - `clientId` requerido como query param

## 📣 Eventos SNS

La aplicacion publica eventos al:

- suscribirse a un fondo
- cancelar una suscripcion

El payload incluye campos como:

- `eventType`
- `subscriptionId`
- `clientId`
- `fundId`
- `amount`
- `timestamp`
- `status`
- `channel`
- `destination`

La publicacion a SNS es `best-effort`: si falla, no rompe la operacion principal.

Para validar en AWS, el stack crea una cola SQS suscrita al topic SNS. Asi puedes inspeccionar el mensaje publicado sin depender de email o SMS reales.

## 🚨 Contrato de error

Las respuestas de error usan un formato uniforme:

```json
{
  "code": "CLIENT_NOT_FOUND",
  "timestamp": "2026-03-18T14:12:05.629",
  "status": 404,
  "error": "Not Found",
  "message": "Cliente no encontrado con id client-001",
  "path": "/subscriptions",
  "correlationId": "corr-sub-404"
}
```

Codigos usados actualmente:

- `REQUEST_VALIDATION_ERROR`
- `INVALID_REQUEST`
- `INVALID_REQUEST_PAYLOAD`
- `CLIENT_NOT_FOUND`
- `FUND_NOT_FOUND`
- `SUBSCRIPTION_NOT_FOUND`
- `SUBSCRIPTION_ALREADY_CANCELLED`
- `ACTIVE_SUBSCRIPTION_ALREADY_EXISTS`
- `INSUFFICIENT_BALANCE`
- `MINIMUM_SUBSCRIPTION_AMOUNT`
- `FUNDS_RETRIEVAL_ERROR`
- `TRANSACTION_HISTORY_RETRIEVAL_ERROR`
- `SUBSCRIPTION_PERSISTENCE_ERROR`
- `SUBSCRIPTION_CANCELLATION_PERSISTENCE_ERROR`
- `INTERNAL_ERROR`

## ⚠️ Consideraciones tecnicas

- DynamoDB usa indices secundarios para consultas por cliente.
- Los adapters de infraestructura solo traducen dominio ↔ persistencia.
- El dominio no depende de Spring ni de infraestructura concreta.
- Las notificaciones estan desacopladas por `NotificationGateway`.
- La cancelacion mantiene historial completo y no elimina datos.
- El historial de transacciones se ordena en el use case aunque DynamoDB ya consulta por el indice `clientId-createdAt-index`.

## 🔭 Mejoras futuras

- autenticacion y autorizacion
- tests de integracion end-to-end
- observabilidad centralizada
- manejo transaccional mas robusto para operaciones compuestas
- resiliencia para notificaciones externas

👨‍💻 Autor

Piter Velasquez
Backend Developer – Java | Spring Boot | AWS
