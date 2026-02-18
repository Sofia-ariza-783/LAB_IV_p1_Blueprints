# ╰┈➤ 【🌐】 | Lab IV: BluePrints ┆⤿⌗  
### *Escuela Colombiana de Ingeniería – Arquitecturas de Software*  

---

## ╰┈➤ °.⭑【📋】 Requisitos ┆⤿⌗

- **Java 21**  
- **Maven 3.9+**  
- **Docker Desktop**

---

## ╰┈➤ °.⭑【▶️】 Ejecución del proyecto ┆⤿⌗

Para ejecutar el proyecto, asegúrate de tener **Docker Desktop instalado y en ejecución**. Luego usa:

```bash
mvn clean install
docker compose up --build
````

> Si deseas activar filtros de puntos (reducción de redundancia, *undersampling*, etc.), implementa nuevas clases que extiendan `BlueprintsFilter` y reemplaza `IdentityFilter` mediante `@Primary` o configuraciones de Spring.

### Acceso en navegador:

*   **Swagger UI:** <http://localhost:8080/swagger-ui.html>
*   **OpenAPI JSON:** <http://localhost:8080/v3/api-docs>

***

## ╰┈➤ °.⭑【🗂️】 Estructura de carpetas (arquitectura) ┆⤿⌗

    src/main/java/edu/eci/arsw/blueprints
      ├── model/         # Entidades de dominio: Blueprint, Point
      ├── persistence/   # Interfaz + repositorios (InMemory, Postgres)
      │    └── old/      # Implementaciones antiguas previas a la migración
      │    └── impl/     # Implementaciones concretas para PostgreSQL
      ├── services/      # Lógica de negocio y orquestación
      ├── filters/       # Filtros de procesamiento (Identity, Redundancy, Undersampling)
      ├── controllers/   # REST Controllers (BlueprintsAPIController)
      └── config/        # Configuración (Swagger/OpenAPI, etc.)

> La estructura sigue el patrón de **capas lógicas**, permitiendo extender el sistema hacia nuevas tecnologías o fuentes de datos.

***

## ╰┈➤ °.⭑【📖】 Actividades del laboratorio ┆⤿⌗

### 1. Familiarización con el código base

*   Revisión del paquete `model` con las clases `Blueprint` y `Point`.
*   Análisis de la capa `persistence` y su implementación `InMemoryBlueprintPersistence`.
*   Estudio de la capa `services` (`BlueprintsServices`) y del controlador `BlueprintsAPIController`.

El código actual implementa una API para gestionar *blueprints* y sus puntos, organizados por autor y nombre. La persistencia es temporal, almacenada en un mapa en memoria; esto permite simular almacenamiento persistente usando datos iniciales predefinidos.

Dado que el diseño es extensible y limpio, la migración hacia una base de datos real requiere cambios mínimos, pues ya existen los contratos necesarios para los repositorios.

***

### 2. Migración a persistencia en PostgreSQL

*   Configurar PostgreSQL (recomendado: Docker).
*   Implementar `PostgresBlueprintPersistence` reemplazando la versión en memoria.
*   Mantener el contrato definido en `BlueprintPersistence`.

Se utilizó Docker por su facilidad de despliegue. Se generaron los archivos base con:

```bash
docker init
```

A partir de esto se configuró el `Dockerfile` y `compose.yaml`, definiendo:

*   Puerto del contenedor PostgreSQL: **5432**
*   Puerto del servicio Spring Boot: **8080**
*   Variables de autenticación básicas (solo para pruebas).

Después se implementó el repositorio `PostgresBlueprintPersistence`, manteniendo la interfaz original y creando un repositorio que extiende `JpaRepository`.

En esta versión no se inicializaron datos por defecto, ya que la persistencia ahora es real y se mantiene entre sesiones.

![img.png](img.png)

***

### 3. Buenas prácticas de API REST

*   Actualización del path base a `/api/v1/blueprints`.
*   Uso correcto de códigos HTTP:
    *   **200 OK**
    *   **201 Created**
    *   **202 Accepted**
    *   **400 Bad Request**
    *   **404 Not Found**
*   Implementación de una respuesta uniforme (`ApiResponse<T>`).

Ejemplo del modelo:

```java
public record ApiResponse<T>(int code, String message, T data) {}
```

Ejemplo JSON:

```json
{
  "code": 200,
  "message": "execute ok",
  "data": { "author": "john", "name": "house", "points": [...] }
}
```

Se creó la clase `ApiResponseFormated` para manejar códigos HTTP y mensajes estandarizados. Se agregaron `try-catch` en el controlador para retornar respuestas adecuadas y mensajes correctos según cada operación.

***

### 4. OpenAPI / Swagger

*   Configuración de `springdoc-openapi`.
*   Documentación accesible en `/swagger-ui.html`.
*   Anotación de endpoints con `@Operation` y `@ApiResponse`.

Estas anotaciones permiten que Swagger muestre información detallada de cada endpoint, su método, respuesta esperada y códigos HTTP.

![img_1.png](img_1.png)

![img_2.png](img_2.png)

***

### 5. Filtros de *Blueprints*

Filtros implementados:

*   **RedundancyFilter**: elimina puntos consecutivos duplicados.
*   **UndersamplingFilter**: conserva uno de cada dos puntos.

Se configuraron perfiles de Spring (`redundancy`, `undersampling`).  
El filtro por defecto (`IdentityFilter`) incluye:

```java
@Profile("!redundancy && !undersampling")
```

Ejemplo de funcionamiento:

#### Get original

![img_3.png](img_3.png)

#### Get con filtro **redundancy**

![img_4.png](img_4.png)

#### Get con filtro **undersampling**

![img_5.png](img_5.png)

***

## ✅ Entregables

1.  Repositorio GitHub con:
    *   Código actualizado
    *   Configuración PostgreSQL
    *   Swagger/OpenAPI habilitado
    *   Implementación de `ApiResponse<T>`

2.  Documentación del laboratorio:
    *   Instrucciones claras
    *   Evidencia en Swagger y base de datos
    *   Explicación de buenas prácticas adoptadas

***

## 📊 Criterios de evaluación

| Criterio                         | Peso |
| -------------------------------- | ---- |
| Diseño de API                    | 25%  |
| Migración a PostgreSQL           | 25%  |
| Manejo de códigos HTTP           | 20%  |
| Documentación (OpenAPI + README) | 15%  |
| Pruebas básicas                  | 15%  |

**Bonus:**

*   Imagen de contenedor (`spring-boot:build-image`)
*   Métricas con Actuator
