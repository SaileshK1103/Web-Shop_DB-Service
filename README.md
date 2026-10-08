# Webshop Order Management Service & Relational Database Solution

A RESTful backend service built with Java, Spring Boot, and MariaDB demonstrating advanced relational database design, transactions, indexing, views, triggers, and temporal features.

## Architecture & Layers
- `controller`: REST API endpoints
- `service`: Business logic and transactional boundaries
- `repository`: Spring Data JPA interfaces and custom SQL queries
- `entity`: Database table entity mappings
- `dto`: Request and response data structures
- `mappers`: Entity-DTO mappers
- `exception`: Domain exceptions and the global error handler

## API

All request and response bodies are JSON.

### Errors
Every error is returned as an RFC 7807 problem document (`application/problem+json`):

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Category with id 42 was not found",
  "instance": "/api/categories/42"
}
```

| Status | When |
|---|---|
| `400 Bad Request` | Malformed JSON, a path id that is not a number, or a body that fails validation |
| `404 Not Found` | The requested resource does not exist |
| `409 Conflict` | The request is valid but cannot be carried out in the current state |

Validation failures add an `errors` object with one message per rejected field:

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "Invalid request content.",
  "errors": { "name": "must not be blank" }
}
```

### Categories
Product categories of the shop.

| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| `GET` | `/api/categories` | List all categories, sorted by name | `200` with an array of categories | - |
| `GET` | `/api/categories/{id}` | Get one category | `200` with the category | `404` |
| `POST` | `/api/categories` | Create a category | `201` with the category and a `Location` header | `400` |
| `PUT` | `/api/categories/{id}` | Replace the name and description | `200` with the category | `400`, `404` |
| `DELETE` | `/api/categories/{id}` | Delete a category | `204` without a body | `404`, `409` if the category still has products |

Request body (`POST`, `PUT`):

| Field | Type | Rules |
|---|---|---|
| `name` | string | required, not blank, at most 100 characters |
| `description` | string | optional |

```json
{ "name": "Books", "description": "Printed and digital books" }
```

Response body:

```json
{ "id": 1, "name": "Books", "description": "Printed and digital books" }
```

Deleting a category never deletes its products: the request is rejected with `409` until the products are moved or removed.

## Running locally
1. Create the database and load the schema:
   ```
   mariadb -u <user> -p -e "CREATE DATABASE webshop"
   mariadb -u <user> -p webshop < src/main/resources/sql/schema_no_data_clean.sql
   ```
2. Set the connection settings as environment variables:

   | Variable | Required | Default |
   |---|---|---|
   | `DB_URL` | no | `jdbc:mariadb://localhost:3306/webshop` |
   | `DB_USERNAME` | yes | - |
   | `DB_PASSWORD` | yes | - |

3. Start the service with `mvn spring-boot:run`.

The schema is owned by the SQL scripts in `src/main/resources/sql`. Hibernate runs with `ddl-auto: validate`, so the service refuses to start if an entity does not match the database.

## Tests
`mvn test` runs against an in-memory H2 database in MariaDB mode, created from the same schema script, so no database setup is needed.
