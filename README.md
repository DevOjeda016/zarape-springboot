# El Zarape — API (Spring Boot)

MVP de la API REST de El Zarape, migrada del sistema original (JAX-RS + MySQL con stored procedures) a **Spring Boot 3 + JPA + JWT**.

## Requisitos
- Java 17+ y Maven 3.9+

## Ejecutar
```bash
mvn spring-boot:run            # H2 en memoria, datos de demo incluidos
mvn spring-boot:run -Dspring-boot.run.profiles=mysql   # MySQL (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD)
mvn test
```
API en `http://localhost:8080/api`. Front: repo `zarape-react` (CORS permite `http://localhost:5173`, configurable con `CORS_ORIGINS`).

## Usuarios de demo (solo desarrollo)
| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` (`ADMIN_USER`/`ADMIN_PASSWORD`) | ADMIN |
| `cocina` | `cocina123` | EMPLOYEE |

Para producción: define `JWT_SECRET` (≥ 32 caracteres), cambia las credenciales y pon `zarape.seed.enabled=false`.

## Endpoints
| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/login`, `/api/auth/signup` | público (signup crea CLIENT) |
| GET | `/api/auth/me` | autenticado |
| GET | `/api/categories`, `/api/products?categoryId=&type=FOOD\|DRINK`, `/api/branches` | público |
| POST/PUT/DELETE | `/api/categories`, `/api/products`, `/api/branches` | ADMIN (borrado lógico) |
| POST | `/api/orders` | autenticado (precios tomados del catálogo) |
| GET | `/api/orders/mine` | autenticado |
| GET | `/api/orders` | ADMIN / EMPLOYEE (cocina) |
| PATCH | `/api/orders/{id}/status` | ADMIN / EMPLOYEE |

Flujo de estados de pedido (ticket + comanda unificados): `IN_SERVICE → READY → DELIVERED`; `CANCELLED` desde `IN_SERVICE` o `READY`.

## Alcance del MVP / pendiente
Incluido: auth, catálogo (categorías, productos, sucursales), pedidos y cocina.
Pendiente respecto al sistema original: CRUD de empleados/clientes, ciudades/estados, tarjetas y pasarela de pago real (el pago con tarjeta está simulado), subida de imágenes (se usa URL/ruta), combos.
