# Proyecto Genesis — Breaze in the Moon

Plataforma que permite a usuarios consumir operaciones de cómputo de forma controlada y con trazabilidad de costos mediante un sistema de tokens.

---

## Requisitos previos

- Java 17+
- Maven 3.8+
- MySQL 8.0+

---

## Configuración inicial

### 1. Crear la base de datos

```bash
mysql -u root -p < init.sql
```

### 2. Variables de entorno

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DB_USERNAME` | Usuario de MySQL | `root` |
| `DB_PASSWORD` | Contraseña de MySQL | `root` |
| `JWT_SECRET` | Clave secreta JWT (mín. 256 bits) | valor en `application.properties` |
| `JWT_EXPIRATION` | Duración del JWT en milisegundos | `86400000` (24h) |

Puedes configurarlas como variables de entorno del sistema o editarlas directamente en `src/main/resources/application.properties`.

### 3. Levantar el proyecto

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Credenciales del administrador inicial

| Campo | Valor |
|---|---|
| Email | `admin@breaze.com` |
| Contraseña | `Admin123!` |

Creadas automáticamente al primer arranque por el `DataSeeder`.

---

## Flujo básico de uso

```
1. POST /api/v1/auth/register   → crear cuenta
2. POST /api/v1/auth/login      → obtener JWT
3. POST /api/v1/plans/2/subscribe → suscribirse al plan Pro (acredita 1000 tokens)
4. POST /api/v1/operations/OP-03/execute → ejecutar operación
5. GET  /api/v1/transactions    → ver historial
```

---

## Endpoints principales

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Público | Registrarse |
| POST | `/api/v1/auth/login` | Público | Login → JWT |
| GET | `/api/v1/profile` | USER/ADMIN | Perfil con saldo y plan |
| GET | `/api/v1/operations/catalog` | USER/ADMIN | Catálogo activo |
| POST | `/api/v1/operations/{code}/execute` | USER | Ejecutar operación |
| GET | `/api/v1/transactions` | USER/ADMIN | Historial paginado |
| GET | `/api/v1/plans` | USER/ADMIN | Listar planes |
| POST | `/api/v1/plans/{id}/subscribe` | USER | Suscribirse a un plan |
| GET | `/api/v1/admin/users` | ADMIN | Listar usuarios |
| PATCH | `/api/v1/admin/users/{id}/activate` | ADMIN | Activar usuario |
| PATCH | `/api/v1/admin/users/{id}/deactivate` | ADMIN | Desactivar usuario |
| POST | `/api/v1/admin/users/{id}/recharge` | ADMIN | Recargar tokens |
| GET | `/api/v1/admin/metrics` | ADMIN | Métricas globales |
| PUT | `/api/v1/admin/exchange-rate` | ADMIN | Actualizar tasa COP/USD |

---

## Colección Bruno

Importar el archivo `genesis-api.json` en Bruno:

1. Abrir Bruno → **Import Collection**
2. Seleccionar `genesis-api.json`
3. En el entorno `local`, pegar el JWT obtenido del login en la variable `jwt`

---

## Modelo de tokens

```
tokens_entrada = floor(longitud_json_entrada / 4)
tokens_salida  = floor(longitud_json_salida  / 4)
costo_total    = costo_base + tokens_entrada + tokens_salida
```

Si el saldo es insuficiente o hay un error en el cálculo, **no se descuentan tokens**.

---

## Catálogo de operaciones

| Código | Nombre | Costo base |
|---|---|---|
| OP-01 | ¿Cuánto me cuesta ese crédito? | 50 tokens |
| OP-02 | Conversor COP ↔ USD | 20 tokens |
| OP-03 | Calculadora de IMC | 15 tokens |
| OP-04 | Calculadora de sueño | 20 tokens |

---

## Planes base

| Plan | Tokens otorgados |
|---|---|
| Free | 200 |
| Pro | 1.000 |
| Enterprise | 5.000 |

Al suscribirse a un plan, los tokens se **acumulan** al saldo actual. Un plan no puede eliminarse si tiene usuarios suscritos.

---

## Principios SOLID aplicados

- **SRP**: `TokenCalculator` solo calcula tokens. Cada `Operation` solo implementa su fórmula.
- **OCP**: Añadir OP-05 = crear clase con `@Component` que implemente `Operation`. Sin tocar código existente.
- **LSP**: Todas las operaciones son intercambiables bajo la interfaz `Operation`.
- **ISP**: Interfaces específicas por dominio (`OperationService`, `AdminService`, `PlanService`...).
- **DIP**: Controladores y servicios dependen de interfaces, no de implementaciones concretas.

---

## Estructura del proyecto

```
src/main/java/com/breaze/genesis/
├── config/           JwtUtil, JwtFilter, SecurityConfig, DataSeeder, OpenApiConfig
├── controller/       AuthController, OperationController, PlanController,
│                     AdminController, ExchangeRateController, TransactionController
├── dto/
│   ├── request/      LoginRequest, RegisterRequest, ExecuteOperationRequest, PlanRequest
│   └── response/     ApiResponse, AuthResponse, ExecuteOperationResponse, PlanResponse,
│                     UserResponse, UserProfileResponse, TransactionResponse,
│                     MetricsResponse, ExchangeRateResponse, CatalogOperationResponse
├── entity/           User, Plan, CatalogOperation, Transaction, ExchangeRate
├── exception/        GlobalExceptionHandler
├── repository/       UserRepository, PlanRepository, CatalogOperationRepository,
│                     TransactionRepository, ExchangeRateRepository
└── service/
    ├── impl/         AuthServiceImpl, AdminServiceImpl, PlanServiceImpl,
    │                 ExchangeRateServiceImpl, OperationServiceImpl,
    │                 TransactionServiceImpl, CustomUserDetailsService
    └── operation/    Operation (interfaz), CreditCalculatorOperation,
                      CurrencyConverterOperation, BmiCalculatorOperation,
                      SleepCalculatorOperation, OperationExecutionException
```
