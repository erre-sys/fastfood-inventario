# erre-inventario

Microservicio de **inventario** de Food in House: insumos, unidades y conversiones, rendimientos, proveedores, compras y kardex. Diseño: documento de Discovery (Outline, colección Producto) y ADR 0022 a 0025 en `erre-infra`.

## Requisitos
- **Java 25**: el archivo `.java-version` lo fija con jenv.
- **Docker** (Colima): las pruebas y el desarrollo local levantan PostgreSQL con Testcontainers. `colima start --cpu 2 --memory 3 --vm-type vz`.

## Comandos
| Para qué | Comando |
|---|---|
| Compilar, probar y verificar todo | `./mvnw verify` |
| Formatear el código | `./mvnw spotless:apply` |
| Correr el servicio en local (con PostgreSQL en un contenedor) | `./mvnw spring-boot:test-run` |

Nunca se guardan bases de datos, volcados ni archivos `.env` en el repo.

## Arquitectura
Hexagonal, en un solo módulo. `ArquitecturaTest` (ArchUnit) hace fallar el build si se rompe:
- `dominio`: modelo y reglas. Sin Spring, sin JPA, sin Lombok.
- `aplicacion`: casos de uso.
- `infraestructura`: REST, persistencia JPA (único lugar con Lombok) y configuración.

Records para el dominio, los comandos y las respuestas; Lombok solo en las entidades JPA, sin `@Data` (`lombok.config`).

## Flujo de trabajo
Gitflow: ramas `feature/*` → PR con **squash** a `develop`; `release/*` y `hotfix/*` → PR con **merge commit** a `main`. Commits con *Conventional Commits* (`feat:`, `fix:`, `docs:`…).

## Licencia
[AGPL-3.0](LICENSE).
