**Proyecto Génesis - Breaze in the Moon 🚀**

Proyecto Génesis es la plataforma tecnológica fundacional de Breaze in the Moon. Diseñada como un sistema de consumo de cómputo controlado, permite a los usuarios 
ejecutar operaciones del catálogo mediante el consumo de tokens, garantizando una trazabilidad total de costos y saldos.

**📋 Tabla de Contenidos
Características**

- Tecnologías y Requisitos
- Modelo de Tokens
- Catálogo de Operaciones
- Metodología de Desarrollo
- Instalación y Configuración
- Credenciales de Acceso

✨ Características:
El sistema implementa dos roles principales con funcionalidades específicas:

**Usuario Final**

- **Gestión de cuenta:** Registro e inicio de sesión con seguridad JWT.
- **Suscripciones:** Acceso a planes (Free, Pro, Enterprise) que acreditan tokens automáticamente.
- **Operaciones: **Ejecución de cálculos del catálogo y consulta de historial de transacciones.
- **Perfil: **Consulta de saldo de tokens y plan activo.


**Administrador**

- **Gestión de Usuarios: **Activar/desactivar usuarios y recarga manual de tokens.
- **Gestión de Planes: **CRUD completo de planes paramétricos (sin modificar código).
- **Control de Operaciones:** Activar/desactivar servicios del catálogo y actualizar tasas de cambio (COP/USD).
- **Métricas:** Consulta de consumo global, operaciones más usadas y usuarios destacados.

**🛠️ Tecnologías y Requisitos**
Este proyecto se ha desarrollado bajo estándares de alta calidad y principios SOLID:
- **Backend:** Java con Spring Boot.
- **Seguridad:** Spring Security + JWT.
- **Persistencia: **JPA / Hibernate sobre MySQL.
- **Documentación:** OpenAPI 3.0 (Swagger UI).
- **Otros:** Paginación de listados y validación detallada de entradas campo a campo.


**🪙 Modelo de Tokens**
El costo de cada operación se calcula dinámicamente:
- Si el usuario no tiene saldo suficiente, la operación no se ejecuta.
- En caso de error en el cálculo, no se descuentan tokens del saldo.

**📖 Catálogo de Operaciones**

**Código,Operación,Costo Base**
- OP-01,¿Cuánto me cuesta ese crédito? (Amortización),50 tokens 
- OP-02,Conversor COP / USD,20 tokens 
- OP-03,Calculadora de IMC (Índice de Masa Corporal),15 tokens 
- OP-04,Calculadora de sueño (Ciclos de descanso),20 tokens

**⚙️ Metodología de Desarrollo**

- **API First:** El contrato de la API (openapi.yaml) fue diseñado y validado antes de la implementación.
- **Gitflow Estricto: **Gestión de ramas mediante main, develop, feature/, release/ y hotfix/.
- **Commits Significativos:** Historial de cambios descriptivo y estructurado.

**🚀 Instalación y Configuración**

**1. Requisitos Previos**
   - Java JDK 17+
   - MySQL Server
   - Herramienta de pruebas Bruno (para ejecución de colección)

**2. Variables de Entorno**
Crea un archivo .env o configura las siguientes variables en tu entorno:
**- DB_URL:** URL de conexión a MySQL.
**- DB_USERNAME:** Usuario de la base de datos.
**- DB_PASSWORD**: Contraseña de la base de datos.
**- JWT_SECRET:** Clave secreta para la firma de tokens.

  
**3. Ejecución**
1. Clonar el repositorio.
2. Ejecutar el script SQL incluido en la raíz para preparar la base de datos.
3. Iniciar la aplicación con ./mvnw spring-boot:run.
4. Acceder a Swagger UI en /swagger-ui.html.
