# 💰 BTG Funds API

API backend desarrollada en Java 17 + Spring Boot + Clean Architecture con el scaffold de Bancolombia para la gestion de fondos de inversion.

Permite:
- Consultar fondos disponibles
- Suscribirse a un fondo
- Cancelar suscripciones
- Registrar transacciones
- Integrar notificaciones

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

Las transacciones guardan `subscriptionId`, `clientId`, `fundId`, `type`, `amount` y `createdAt`.

### Manejo de errores

La API usa excepciones de use case y un manejador global en `api-rest`.

- `400` para reglas de negocio invalidas
- `404` para recursos no encontrados
- `409` para conflictos de negocio
- `500` para errores internos o de persistencia

## ☁️ Infraestructura AWS

Provisionada con [cloudformation.yaml](/home/certhakzu/Documentos/Ceiba/tecnical%20test/btg-funds-api/deployment/aws/cloudformation.yaml).

### Recursos

- DynamoDB:
  - Clients
  - Funds
  - Subscriptions
  - Transactions
- SNS Topic para notificaciones
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
curl --location --request DELETE 'http://localhost:8080/subscriptions/sub-001'
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
curl --location --request DELETE 'https://q5d8y4kqwp.us-east-1.awsapprunner.com/subscriptions/sub-001'
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

## ⚠️ Consideraciones tecnicas

- DynamoDB usa indices secundarios para consultas por cliente.
- Los adapters de infraestructura solo traducen dominio ↔ persistencia.
- El dominio no depende de Spring ni de infraestructura concreta.
- Las notificaciones estan desacopladas por `NotificationGateway`.
- La cancelacion mantiene historial completo y no elimina datos.

## 🔭 Mejoras futuras

- autenticacion y autorizacion
- tests de integracion end-to-end
- observabilidad centralizada
- manejo transaccional mas robusto para operaciones compuestas
- resiliencia para notificaciones externas
