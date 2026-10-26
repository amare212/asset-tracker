# IT Asset & Equipment Tracker API

Spring Boot 3 + Java 21 + PostgreSQL, secured with stateless JWT and role-based access control.

## Run with Docker (recommended)

```bash
cp .env.example .env        # then edit the values
docker compose up --build
```

- API:        http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

Stop with `docker compose down` (data is kept in the `pgdata` volume; add `-v` to wipe it).

## Run from VS Code (app on your machine, DB in Docker)

```bash
docker compose up -d postgres
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/assetdb
export SPRING_DATASOURCE_USERNAME=asset_user
export SPRING_DATASOURCE_PASSWORD=change_me_db_password
export JWT_SECRET=change-this-to-a-long-random-secret-at-least-32-chars
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=change_me_admin_password
mvn spring-boot:run
```

(Use the same values you put in `.env`.)

## Try it

```bash
# 1. login as the seeded admin
curl -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"change_me_admin_password"}'
# copy the token, then:
TOKEN=<paste token>

# 2. create a category
curl -X POST localhost:8080/api/categories -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" -d '{"name":"Laptop"}'

# 3. create an asset
curl -X POST localhost:8080/api/assets -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"name":"HP ProBook 450","serialNumber":"SN-001","categoryId":1}'

# 4. register a staff user, then assign asset 1 to them (use the returned user id)
curl -X POST localhost:8080/api/auth/register -H "Content-Type: application/json" \
     -d '{"username":"abebe","password":"secret123"}'
curl -X PATCH localhost:8080/api/assets/1/assign/2 -H "Authorization: Bearer $TOKEN"

# 5. assigning it again -> 400 Bad Request
curl -X PATCH localhost:8080/api/assets/1/assign/2 -H "Authorization: Bearer $TOKEN"
```

## Endpoints

| Method | Endpoint | Roles |
|---|---|---|
| POST | /api/auth/register | public (creates ROLE_STAFF) |
| POST | /api/auth/login | public |
| GET | /api/categories | ADMIN, STAFF |
| POST | /api/categories | ADMIN |
| GET | /api/assets (`?status=`) | ADMIN, STAFF |
| GET | /api/assets/my-assets | ADMIN, STAFF |
| POST | /api/assets | ADMIN |
| PATCH | /api/assets/{id}/assign/{userId} | ADMIN |
| PATCH | /api/assets/{id}/return | ADMIN |
| DELETE | /api/assets/{id} | ADMIN |

## Error format

```json
{ "timestamp": "2026-10-26T10:15:30", "status": 400, "message": "...", "path": "/api/assets" }
```
