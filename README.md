# Backend RBAC + JWT (Spring Boot 3.2, MyBatis-Plus)

## Requirements
- Java 17
- MySQL 8.x

## Setup
1. Import schema: `petAdoptCommunityPublic/sql/rbac_schema.sql`
2. Update database config in `backend/src/main/resources/application.yml`
3. Ensure `password_hash` is a BCrypt hash (plain text will not authenticate)

## Run
```
./mvnw spring-boot:run
```

## API
- `POST /api/auth/login`
- `GET /api/health`
- `GET /api/users`
- `POST /api/users`
- `POST /api/users/assign-roles`
- `GET /api/roles`
- `POST /api/roles`
- `POST /api/roles/assign-permissions`
- `GET /api/permissions`
- `POST /api/permissions`
