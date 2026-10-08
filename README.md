# Webshop Order Management Service & Relational Database Solution

A RESTful backend service built with Java, Spring Boot, and MariaDB demonstrating advanced relational database design, transactions, indexing, views, triggers, and temporal features.

## Architecture & Layers
- `controller`: REST API endpoints
- `service`: Business logic and transactional boundaries
- `repository`: Spring Data JPA interfaces and custom SQL queries
- `entity`: Database table entity mappings
- `dto`: Request and response data structures
- `mappers`: Entity-DTO mappers

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
