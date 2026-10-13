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

## Desarrollo con Docker (MySQL + API + front)
Solo para desarrollo. Requiere Docker con Compose y los repos clonados lado a lado (`zarape-springboot` y `zarape-react`).
```bash
./dev.sh up        # primer plano; ./dev.sh bg para segundo plano
./dev.sh logs api  # logs de un servicio (db, api, web)
./dev.sh restart   # reinicia la API para recargar cambios de Java
./dev.sh down      # detiene (conserva la BD); ./dev.sh reset borra también la BD
```
Sin `dev.sh` (p. ej. en Windows): `docker compose -f docker-compose.dev.yml up --build`.

- Front: http://localhost:5173 · API: http://localhost:8080/api · MySQL: `localhost:3307` (usuario `root`, contraseña `dev`).
- El código se monta como volumen: el front recarga solo; la API necesita `./dev.sh restart` tras cambios.
- La primera vez tarda porque descarga dependencias de Maven y npm (quedan cacheadas en volúmenes).

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
