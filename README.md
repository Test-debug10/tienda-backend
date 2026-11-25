# Backend Tienda Gamer (DSY1104)

API REST de la tienda online hecha con **Spring Boot 3.5**, **Spring Data JPA**, **Spring Security** y **JWT**.
Documentada con **Swagger (springdoc-openapi)**.

Frontend en React: https://github.com/sebacasti07/Migracion-React-a-Tienda-EvFullStack1

## Cómo ejecutarlo

Requisitos: Java 17 o superior (no hace falta instalar Maven, el proyecto trae `mvnw`).

### Backend desplegado en AWS EC2 (la entrega)

El backend corre en la instancia EC2 `tienda-gamer-db` (**Amazon Linux 2023**), junto a PostgreSQL 18, como servicio de Linux
(`tienda-backend.service`). **Parte solo al hacer Start Lab** en el Learner Lab, sin levantar nada a mano:

| Qué | Dirección |
|---|---|
| API | http://52.5.171.74:8080/api/v1 |
| Swagger | http://52.5.171.74:8080/swagger-ui.html |
| Base de datos | PostgreSQL en la misma instancia (`localhost:5432`, base `tienda_gamer`) |

`52.5.171.74` es la IP elástica de la instancia: no cambia aunque el laboratorio se apague y se vuelva a encender.
El front se conecta a esta API con `npm run dev:ec2`.

Cómo quedó instalado (script en [`deploy/instalar-amazon-linux.sh`](deploy/instalar-amazon-linux.sh)):

1. Se compila el jar en local: `./mvnw package -DskipTests` y se copia a la instancia con `scp`
   (usuario `ec2-user`: `scp -i labsuser.pem target/tienda-backend-1.0.0.jar ec2-user@52.5.171.74:/tmp/tienda-backend.jar`).
2. El script (con `dnf`) instala PostgreSQL 18 y Java 21 (Amazon Corretto), crea la base `tienda_gamer` y el usuario `tienda_user`, crea 1 GB de swap (la instancia t3.micro tiene ~900 MB de RAM), deja el jar en
   `/opt/tienda` y crea `/etc/tienda-backend.env` con el perfil `postgres` y la clave de la base, que toma de
   `/root/clave-db.txt` (la clave nunca pasa por el repositorio).
3. Crea y habilita el servicio systemd `tienda-backend` (`Restart=always`, arranca después de PostgreSQL).

Para actualizar el backend: compilar, copiar el jar a `/tmp/tienda-backend.jar` y volver a correr el script.
Logs: `sudo journalctl -u tienda-backend -f`.

### En local

**Con la base de EC2** (el backend corre en tu PC y se conecta a la base por la IP elástica):

```powershell
$env:TIENDA_DB_PASSWORD = "<clave>"
.mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

**Sin EC2**, con una base H2 local (sirve para probar sin el laboratorio encendido):

```bash
./mvnw spring-boot:run        # Linux / Mac
mvnw.cmd spring-boot:run      # Windows
```

En local la API queda en http://localhost:8080/api/v1 y Swagger en http://localhost:8080/swagger-ui.html.
Consola H2 (solo sin perfil): http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/tiendadb`, usuario `sa`, sin clave).

### Base de datos

| Perfil | Base de datos | Cómo activarlo |
|---|---|---|
| (por defecto) | H2 en archivo `./data/tiendadb` | nada |
| `postgres` | PostgreSQL en la instancia AWS EC2 | `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres` |
| `mysql` | MySQL | `./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql` |

### PostgreSQL en AWS EC2

La base de datos de la entrega está en una instancia EC2 del Learner Lab de AWS Academy:

| Dato | Valor |
|---|---|
| Instancia | `tienda-gamer-db` · Amazon Linux 2023 · t3.micro · us-east-1 |
| Acceso SSH | `ssh -i labsuser.pem ec2-user@52.5.171.74` |
| IP (elástica, no cambia al reiniciar el laboratorio) | `52.5.171.74` |
| Puertos (grupo de seguridad `tienda-db-sg`) | 8080 abierto (API); 5432 y 22 solo para las IP del equipo |
| Base / usuario | `tienda_gamer` / `tienda_user` |
| Versión | PostgreSQL 18 |

PostgreSQL se instaló con el script `deploy/instalar-amazon-linux.sh`. La contraseña de la base
quedó en `/root/clave-db.txt` dentro de la instancia. La contraseña
**no está en el repositorio**: el backend la lee de la variable de entorno `TIENDA_DB_PASSWORD`
(o `DB_PASSWORD`).

```powershell
$env:TIENDA_DB_PASSWORD = "<clave>"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

Antes hay que iniciar el laboratorio (Start Lab): al apagarse, la instancia se detiene.
Si la red desde donde se trabaja cambia de IP, hay que actualizar la regla del puerto 5432 en
`tienda-db-sg`. Para otra instancia se puede sobrescribir todo con `DB_URL`, `DB_USER` y `DB_PASSWORD`.

Las tablas se crean solas (`ddl-auto=update`) y la primera vez se cargan datos de ejemplo.

### Usuarios de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| ADMIN | admin@duoc.cl | Admin1234@ |
| VENDEDOR | vendedor@duoc.cl | Vendedor123@ |
| CLIENTE | cliente@gmail.com | Cliente123@ |

## Modelo de datos

```
categorias (id, nombre, descripcion)
    1 ── N
productos (id, codigo, nombre, descripcion, precio, precio_oferta, stock, stock_critico, imagen, emoji, categoria_id)

usuarios (id, run, nombre, apellidos, correo, password[BCrypt], rol, fecha_nacimiento, region, comuna, direccion, fecha_registro)
    1 ── N
ordenes (id, fecha, estado, usuario_id, nombre, apellidos, correo, calle, departamento, region, comuna, indicaciones, total)
    1 ── N
detalle_orden (id, orden_id, producto_id, nombre_producto, cantidad, precio_unitario, subtotal)
```

## Endpoints (versión v1)

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| POST | /api/v1/auth/login | Iniciar sesión, devuelve token y refresh token | Público |
| POST | /api/v1/auth/registro | Registro de cliente | Público |
| POST | /api/v1/auth/refresh | Renovar token de acceso | Público (con refresh token) |
| GET | /api/v1/auth/me | Perfil del usuario conectado | Autenticado |
| PUT | /api/v1/auth/password | Cambiar mi contraseña | Autenticado |
| GET | /api/v1/productos | Listar productos (`?categoriaId=`) | Público |
| GET | /api/v1/productos/ofertas | Productos en oferta | Público |
| GET | /api/v1/productos/stock-critico | Productos con stock crítico | ADMIN, VENDEDOR |
| GET | /api/v1/productos/{id} | Detalle de producto | Público |
| POST | /api/v1/productos | Crear producto | ADMIN |
| PUT | /api/v1/productos/{id} | Actualizar producto | ADMIN |
| DELETE | /api/v1/productos/{id} | Eliminar producto | ADMIN |
| GET | /api/v1/categorias | Listar categorías | Público |
| GET | /api/v1/categorias/{id} | Detalle de categoría | Público |
| POST/PUT/DELETE | /api/v1/categorias[/{id}] | Mantener categorías | ADMIN |
| POST | /api/v1/ordenes | Crear orden (checkout) | CLIENTE, ADMIN |
| GET | /api/v1/ordenes/mias | Mis compras | Autenticado |
| GET | /api/v1/ordenes | Listar órdenes | ADMIN, VENDEDOR |
| GET | /api/v1/ordenes/{id} | Detalle de orden | ADMIN, VENDEDOR |
| PUT | /api/v1/ordenes/{id}/estado | Cambiar estado | ADMIN |
| DELETE | /api/v1/ordenes/{id} | Eliminar orden | ADMIN |
| GET/POST/PUT/DELETE | /api/v1/usuarios[/{id}] | Mantenedor de usuarios y roles | ADMIN |

## Seguridad

- Contraseñas cifradas con **BCrypt**.
- Login entrega un **token de acceso** (30 min) y un **refresh token** (7 días), ambos JWT firmados con HMAC-SHA256.
- `JwtAuthenticationFilter` valida firma, expiración y tipo de token en cada petición y carga el rol desde la BD.
- `SecurityConfig` define qué rol puede usar cada endpoint. Sesión `STATELESS`.
- Errores en JSON: 401 (sin token o token inválido), 403 (rol sin permiso), 404, 409 (regla de negocio).
- La clave del JWT se puede definir con la variable de entorno `JWT_SECRET`.

## Lógica de negocio

- El total de la orden se calcula en el servidor con los precios de la BD (usa el precio de oferta si existe).
- Si un producto no tiene stock suficiente la compra se rechaza (409) y no se guarda nada.
- Al comprar se descuenta stock; al cancelar o eliminar una orden se devuelve.
- RUN validado con módulo 11 y correos solo `@duoc.cl`, `@duocuc.cl`, `@profesor.duoc.cl` o `@gmail.com`.
- No se puede eliminar una categoría con productos, ni el último administrador, ni la propia cuenta.

## Pruebas

```bash
./mvnw test
```

- `ValidadoresTest`: RUN y correo.
- `OrdenServiceTest`: lógica de compra con Mockito (total con oferta, stock insuficiente, cancelación).
- `SeguridadApiTest`: integración con MockMvc (login, JWT alterado, refresh, permisos por rol, CRUD).
