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

### OpenAPI / Swagger UI

The generated OpenAPI document is available at `/v3/api-docs` and the interactive Swagger UI is available at `/swagger-ui/index.html` while the application is running.

### Errors

Every error is returned as:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Category with id 42 was not found",
  "instance": "/api/categories/42"
}
```

| Status            | When                                                                            |
| ----------------- | ------------------------------------------------------------------------------- |
| `400 Bad Request` | Malformed JSON, a path id that is not a number, or a body that fails validation |
| `404 Not Found`   | The requested resource does not exist                                           |
| `409 Conflict`    | The request is valid but cannot be carried out in the current state             |

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

| Method   | Path                   | Purpose                          | Success                                         | Errors                                          |
| -------- | ---------------------- | -------------------------------- | ----------------------------------------------- | ----------------------------------------------- |
| `GET`    | `/api/categories`      | List all categories              | `200` with an array of categories               | -                                               |
| `GET`    | `/api/categories/{id}` | Get one category                 | `200` with the category                         | `404`                                           |
| `POST`   | `/api/categories`      | Create a category                | `201` with the category and a `Location` header | `400`                                           |
| `PUT`    | `/api/categories/{id}` | Replace the name and description | `200` with the category                         | `400`, `404`                                    |
| `DELETE` | `/api/categories/{id}` | Delete a category                | `204` without a body                            | `404`, `409` if the category still has products |

Request body (`POST`, `PUT`):

| Field         | Type   | Rules                                       |
| ------------- | ------ | ------------------------------------------- |
| `name`        | string | required, not blank, at most 100 characters |
| `description` | string | optional                                    |

```json
{ "name": "Books", "description": "Printed and digital books" }
```

Response body:

```json
{ "id": 1, "name": "Books", "description": "Printed and digital books" }
```

Deleting a category never deletes its products: the request is rejected with `409` until the products are moved or removed.

### Products

Products of the shop. Each product response combines data from the `Products`, `ProductCategories` and `Suppliers` tables.

| Method   | Path                 | Purpose                                              | Success                                               | Errors                                         |
| -------- | -------------------- | ---------------------------------------------------- | ----------------------------------------------------- | ---------------------------------------------- |
| `GET`    | `/api/products`      | List products, optionally filtered by name and price | `200` with an array of products                       | `400` if `minPrice` is greater than `maxPrice` |
| `GET`    | `/api/products/{id}` | Get one product                                      | `200` with the product                                | `404`                                          |
| `POST`   | `/api/products`      | Create a product                                     | `201` with the product                                | `400`                                          |
| `PUT`    | `/api/products/{id}` | Replace all fields of a product                      | `200` with the product                                | `400`, `404`                                   |
| `DELETE` | `/api/products/{id}` | Delete a product                                     | `204` without a body, also when the id does not exist | `409` if the product appears on an order       |

Query parameters of `GET /api/products` (optional):

| Parameter  | Meaning                                                       | Default |
| ---------- | ------------------------------------------------------------- | ------- |
| `name`     | Only products whose name starts with this text, ignoring case | -       |
| `minPrice` | Only products that cost this much or more                     | -       |
| `maxPrice` | Only products that cost this much or less                     | -       |

The filters can be combined. `minPrice` greater than `maxPrice` returns `400`.

Example: `GET /api/products?name=clean&minPrice=20&maxPrice=50`

Request body (`POST`, `PUT`):

| Field           | Type    | Rules                                                        |
| --------------- | ------- | ------------------------------------------------------------ |
| `name`          | string  | required, not blank, at most 255 characters                  |
| `description`   | string  | optional                                                     |
| `price`         | number  | required, 0 or more, at most 8 integer digits and 2 decimals |
| `stockQuantity` | integer | required, 0 or more                                          |
| `categoryId`    | integer | optional; must be an existing category, otherwise `400`      |
| `supplierId`    | integer | optional; must be an existing supplier, otherwise `400`      |

```json
{
  "name": "Clean Code",
  "description": "A handbook of agile software craftsmanship",
  "price": 39.9,
  "stockQuantity": 5,
  "categoryId": 1,
  "supplierId": 2
}
```

Product response (`category` and `supplier` are `null` when the product has none):

```json
{
  "id": 7,
  "name": "Clean Code",
  "description": "A handbook of agile software craftsmanship",
  "price": 39.9,
  "stockQuantity": 5,
  "category": { "id": 1, "name": "Books" },
  "supplier": { "id": 2, "name": "Book Wholesale" }
}
```

A product that appears on an order is never deleted, because the order items refer to it: the request is rejected with `409`.
The list loads category and supplier together with the products in one query, instead of one extra query per product.

### Customers

Customers of the shop. Each customer response combines data from the `Customers` and `CustomerAddresses` tables.

| Method   | Path                  | Purpose                                 | Success                                               | Errors                                                       |
| -------- | --------------------- | --------------------------------------- | ----------------------------------------------------- | ------------------------------------------------------------ |
| `GET`    | `/api/customers`      | List all customers with their addresses | `200` with an array of customers                      | -                                                            |
| `GET`    | `/api/customers/{id}` | Get one customer with their addresses   | `200` with the customer                               | `404`                                                        |
| `POST`   | `/api/customers`      | Create a customer                       | `201` with the customer                               | `400`, `409` if the email is already used                    |
| `PUT`    | `/api/customers/{id}` | Replace all fields of a customer        | `200` with the customer                               | `400`, `404`, `409` if the email is used by another customer |
| `DELETE` | `/api/customers/{id}` | Delete a customer and their addresses   | `204` without a body, also when the id does not exist | `409` if the customer has orders                             |

Request body (`POST`, `PUT`):

| Field       | Type   | Rules                                                                                           |
| ----------- | ------ | ----------------------------------------------------------------------------------------------- |
| `firstName` | string | required, not blank, at most 100 characters                                                     |
| `lastName`  | string | required, not blank, at most 100 characters                                                     |
| `email`     | string | required, a valid email address, at most 255 characters, unique among customers (ignoring case) |
| `phone`     | string | optional, at most 30 characters                                                                 |

```json
{
  "firstName": "Ada",
  "lastName": "Lovelace",
  "email": "ada@example.com",
  "phone": "+358 40 1234567"
}
```

Customer response:

```json
{
  "id": 3,
  "firstName": "Ada",
  "lastName": "Lovelace",
  "email": "ada@example.com",
  "phone": "+358 40 1234567",
  "addresses": [
    {
      "id": 5,
      "streetAddress": "Main Street 1",
      "postalCode": "00100",
      "city": "Helsinki",
      "country": "Finland"
    }
  ]
}
```

Addresses are not part of the customer request body, they are managed through the address endpoints below.
A customer who has orders is never deleted, because the orders refer to the customer: the request is rejected with `409`.
The list loads the addresses together with the customers in one query, instead of one extra query per customer.

### Customer addresses

Addresses of one customer. An address is only reachable through the customer it belongs to: using the id of another
customer's address returns `404`.

| Method   | Path                                                | Purpose                          | Success                          | Errors                                                          |
| -------- | --------------------------------------------------- | -------------------------------- | -------------------------------- | --------------------------------------------------------------- |
| `GET`    | `/api/customers/{customerId}/addresses`             | List the addresses of a customer | `200` with an array of addresses | `404` if the customer does not exist                            |
| `GET`    | `/api/customers/{customerId}/addresses/{addressId}` | Get one address                  | `200` with the address           | `404`                                                           |
| `POST`   | `/api/customers/{customerId}/addresses`             | Add an address to a customer     | `201` with the address           | `400`, `404` if the customer does not exist                     |
| `PUT`    | `/api/customers/{customerId}/addresses/{addressId}` | Replace all fields of an address | `200` with the address           | `400`, `404`                                                    |
| `DELETE` | `/api/customers/{customerId}/addresses/{addressId}` | Delete an address                | `204` without a body             | `404`, `409` if the address is the shipping address of an order |

Request body (`POST`, `PUT`), also used for supplier addresses:

| Field           | Type   | Rules                                       |
| --------------- | ------ | ------------------------------------------- |
| `streetAddress` | string | required, not blank, at most 255 characters |
| `postalCode`    | string | optional, at most 20 characters             |
| `city`          | string | required, not blank, at most 100 characters |
| `country`       | string | optional, at most 100 characters            |

```json
{
  "streetAddress": "Main Street 1",
  "postalCode": "00100",
  "city": "Helsinki",
  "country": "Finland"
}
```

Address response:

```json
{
  "id": 5,
  "streetAddress": "Main Street 1",
  "postalCode": "00100",
  "city": "Helsinki",
  "country": "Finland"
}
```

### Suppliers

Suppliers of the products. Each supplier response combines data from the `Suppliers` and `SupplierAddresses` tables.

| Method   | Path                  | Purpose                                 | Success                                               | Errors                                   |
| -------- | --------------------- | --------------------------------------- | ----------------------------------------------------- | ---------------------------------------- |
| `GET`    | `/api/suppliers`      | List all suppliers with their addresses | `200` with an array of suppliers                      | -                                        |
| `GET`    | `/api/suppliers/{id}` | Get one supplier with its addresses     | `200` with the supplier                               | `404`                                    |
| `POST`   | `/api/suppliers`      | Create a supplier                       | `201` with the supplier                               | `400`                                    |
| `PUT`    | `/api/suppliers/{id}` | Replace all fields of a supplier        | `200` with the supplier                               | `400`, `404`                             |
| `DELETE` | `/api/suppliers/{id}` | Delete a supplier and its addresses     | `204` without a body, also when the id does not exist | `409` if the supplier still has products |

Request body (`POST`, `PUT`):

| Field         | Type   | Rules                                                   |
| ------------- | ------ | ------------------------------------------------------- |
| `name`        | string | required, not blank, at most 255 characters             |
| `contactName` | string | optional, at most 100 characters                        |
| `phone`       | string | optional, at most 30 characters                         |
| `email`       | string | optional, a valid email address, at most 255 characters |

```json
{
  "name": "Book Wholesale",
  "contactName": "Grace Hopper",
  "phone": "+358 9 1234567",
  "email": "sales@example.com"
}
```

Supplier response:

```json
{
  "id": 2,
  "name": "Book Wholesale",
  "contactName": "Grace Hopper",
  "phone": "+358 9 1234567",
  "email": "sales@example.com",
  "addresses": [
    {
      "id": 4,
      "streetAddress": "Warehouse Road 3",
      "postalCode": "33100",
      "city": "Tampere",
      "country": "Finland"
    }
  ]
}
```

A supplier that still has products is never deleted, because the products refer to it: the request is rejected with `409`.

### Supplier addresses

Addresses of one supplier. They work like customer addresses and use the same request and response bodies.

| Method   | Path                                                | Purpose                          | Success                          | Errors                                      |
| -------- | --------------------------------------------------- | -------------------------------- | -------------------------------- | ------------------------------------------- |
| `GET`    | `/api/suppliers/{supplierId}/addresses`             | List the addresses of a supplier | `200` with an array of addresses | `404` if the supplier does not exist        |
| `GET`    | `/api/suppliers/{supplierId}/addresses/{addressId}` | Get one address                  | `200` with the address           | `404`                                       |
| `POST`   | `/api/suppliers/{supplierId}/addresses`             | Add an address to a supplier     | `201` with the address           | `400`, `404` if the supplier does not exist |
| `PUT`    | `/api/suppliers/{supplierId}/addresses/{addressId}` | Replace all fields of an address | `200` with the address           | `400`, `404`                                |
| `DELETE` | `/api/suppliers/{supplierId}/addresses/{addressId}` | Delete an address                | `204` without a body             | `404`                                       |

### Orders

Orders of the shop. Each order response combines data from the `Orders`, `OrderItems`, `Products`, `Customers` and
`CustomerAddresses` tables.

| Method | Path                        | Purpose                                      | Success                       | Errors                                                                                                        |
| ------ | --------------------------- | -------------------------------------------- | ----------------------------- | ------------------------------------------------------------------------------------------------------------- |
| `GET`  | `/api/orders`               | List orders, optionally in one status        | `200` with an array of orders | `400` for an unknown status                                                                                   |
| `GET`  | `/api/orders/{id}`          | Get one order                                | `200` with the order          | `404`                                                                                                         |
| `GET`  | `/api/orders/customer/{id}` | List the orders of the customer with this id | `200` with an array of orders | `404` if the customer does not exist                                                                          |
| `POST` | `/api/orders`               | Place an order                               | `201` with the order          | `400`, `409` if there is not enough stock                                                                     |
| `PUT`  | `/api/orders/{id}/status`   | Change the status of an order                | `200` with the order          | `400`, `404`, `409` if the change is not allowed or the order was changed by another request at the same time |
| `PUT`  | `/api/orders/{id}/update-status` | Move an order to the next status of its life cycle, no request body | `200` with the order | `404` |

Orders are never deleted. An order that should not be carried out is cancelled through the status endpoint.

Query parameters of `GET /api/orders` (optional):

| Parameter | Meaning                                                                                  | Default |
| --------- | ---------------------------------------------------------------------------------------- | ------- |
| `status`  | Only orders in this status (`NEW`, `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`) | -       |

Example: `GET /api/orders?status=NEW`

Request body of `POST /api/orders`:

| Field               | Type    | Rules                                                          |
| ------------------- | ------- | -------------------------------------------------------------- |
| `customerId`        | integer | required; must be an existing customer, otherwise `400`        |
| `shippingAddressId` | integer | optional; must be an address of that customer, otherwise `400` |
| `items`             | array   | required, at least one item; a product can only appear once    |
| `items[].productId` | integer | required; must be an existing product, otherwise `400`         |
| `items[].quantity`  | integer | required, 1 or more                                            |

```json
{
  "customerId": 3,
  "shippingAddressId": 5,
  "items": [
    { "productId": 7, "quantity": 2 },
    { "productId": 9, "quantity": 1 }
  ]
}
```

Order response:

```json
{
  "id": 12,
  "orderDate": "2026-10-08T18:30:00",
  "deliveryDate": null,
  "status": "NEW",
  "customer": { "id": 3, "firstName": "Ada", "lastName": "Lovelace", "email": "ada@example.com" },
  "shippingAddress": {
    "id": 5,
    "streetAddress": "Main Street 1",
    "postalCode": "00100",
    "city": "Helsinki",
    "country": "Finland"
  },
  "items": [
    {
      "productId": 7,
      "productName": "Clean Code",
      "quantity": 2,
      "unitPrice": 39.9,
      "lineTotal": 79.8
    },
    {
      "productId": 9,
      "productName": "Teddy Bear",
      "quantity": 1,
      "unitPrice": 15.0,
      "lineTotal": 15.0
    }
  ],
  "totalAmount": 94.8
}
```

`unitPrice` is the price the product had when the order was placed. It is stored on the order item, so later price
changes of the product do not change existing orders. `lineTotal` and `totalAmount` are calculated from the items,
the schema has no total column.

Request body of `PUT /api/orders/{id}/status`:

```json
{ "status": "PENDING" }
```

| From        | Allowed next status      |
| ----------- | ------------------------ |
| `NEW`       | `PENDING`, `CANCELLED`   |
| `PENDING`   | `CONFIRMED`, `CANCELLED` |
| `CONFIRMED` | `SHIPPED`, `CANCELLED`   |
| `SHIPPED`   | `DELIVERED`              |
| `DELIVERED` | none                     |
| `CANCELLED` | none                     |

A new order starts as `NEW`, which is also the default of the `status` column in the schema.

Any other change is rejected with `409`, an unknown status value with `400`. Changing to `DELIVERED` sets
`deliveryDate` to the current time. Changing to `CANCELLED` puts the ordered quantities back into the stock.

`PUT /api/orders/{id}/update-status` moves the order one step forward: `NEW` to `PENDING`, `PENDING` to
`CONFIRMED`, `CONFIRMED` to `SHIPPED`, `SHIPPED` to `DELIVERED`. A delivered or cancelled order is returned
unchanged. Unlike the status endpoint above it does not set `deliveryDate` when the order becomes `DELIVERED`.

#### Transactions and stock

- Placing an order runs in one database transaction: the order row, all order item rows and all stock changes are
  committed together. If one item fails (unknown product, not enough stock), the whole transaction is rolled back,
  so no stock is taken and no order is stored.
- The product rows of an order are read with a pessimistic write lock (`SELECT ... FOR UPDATE`). Two orders placed at
  the same time for the last items in stock are handled one after the other, so the stock cannot go below zero.
- The product rows are always locked in ascending product id order, so two orders that contain the same products
  cannot deadlock each other.
- Cancelling an order uses the same lock and returns the stock in the same transaction as the status change.
- Status changes are protected by an optimistic lock. `Orders` has a `version` column (`@Version` on the entity) that
  Hibernate checks and increases on every update of an order. When two requests change the same order at the same
  time, the second one to commit updates no row, fails and is rolled back completely (including returned stock), and
  the client gets `409`. Hibernate adds the `version` column on startup, existing orders start at version 0.

### Reports

Read-only data that the database writes itself.

| Method | Path                                           | Purpose                                       | Success                                      |
| ------ | ---------------------------------------------- | --------------------------------------------- | -------------------------------------------- |
| `GET`  | `/api/reports/orders/{orderId}/status-history` | Every status change of an order, oldest first | `200` with an array, empty if there are none |

The rows come from the table `OrderStatusHistory`, which is written by an entity event when the status of an order
changes (see "Entity events"). The first status of a new order is not logged, only changes:

```json
[
  {
    "id": 1,
    "orderId": 12,
    "previousStatus": "NEW",
    "updatedStatus": "PENDING",
    "createdAt": "2026-10-01T10:30:00"
  },
  {
    "id": 2,
    "orderId": 12,
    "previousStatus": "PENDING",
    "updatedStatus": "CONFIRMED",
    "createdAt": "2026-10-02T08:15:00"
  }
]
```

## Database features

### Indexes

The script `src/main/resources/sql/indexes.sql` adds the indexes that the schema does not already have. It can be run
again safely.

| Index                          | Table (columns)               | Used by                                       |
| ------------------------------ | ----------------------------- | --------------------------------------------- |
| `idx_orders_status_order_date` | `Orders (status, order_date)` | `GET /api/orders?status=...`                  |
| `idx_product_name`             | `Products (name)`             | `GET /api/products?name=...`                  |
| `idx_product_price`            | `Products (price)`            | `GET /api/products?minPrice=...&maxPrice=...` |

Indexes that already exist without this script, because InnoDB creates one for every primary key, `UNIQUE` column and
foreign key column:

| Index on                                                             | Used by                                                                                     |
| -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------- |
| Primary keys                                                         | every `GET /{id}`, `PUT /{id}` and `DELETE /{id}`                                           |
| `Customers (email)`, unique                                          | the duplicate email check when a customer is created or updated                             |
| `Orders (customer_id)`                                               | `GET /api/orders/customer/{id}`, the "customer has orders" check before deleting a customer |
| `Orders (shipping_address_id)`                                       | the "address is a shipping address" check before deleting a customer address                |
| `OrderItems (product_id)`                                            | the "product appears on an order" check before deleting a product                           |
| `Products (category_id)`, `Products (supplier_id)`                   | the "still has products" checks before deleting a category or supplier                      |
| `CustomerAddresses (customer_id)`, `SupplierAddresses (supplier_id)` | the address lists of a customer or supplier                                                 |

Decisions behind the indexes:

- **Composite, filter column first.** `idx_orders_status_order_date` starts with `status` because that is the column
  the filter compares with `=`. `order_date` comes second so the orders of one status are stored in date order.
- **No index for customer search.** Customers are looked up by id or listed in full; they rarely need to be
  searched, so an index on the name columns would cost more on every insert and update than it would save.
- **The product name search is a prefix search so it can use `idx_product_name`.** `name LIKE 'clean%'` can use an
  index on `name`. A search for text anywhere in the name (`LIKE '%clean%'`) cannot, and neither can a query that
  wraps the column in `UPPER()`, so the repository method is `StartingWith` without `IgnoreCase`. The search still
  ignores case on MariaDB, because its default collation compares text without case.
- **The price filter is a range, which `idx_product_price` supports.** `price BETWEEN ? AND ?` reads only the part
  of the index inside the range. A missing `minPrice` or `maxPrice` is replaced by 0 or the largest possible price.
- **One index per query.** When name and price are both given, MariaDB normally picks the one index it estimates to
  be more selective and checks the other condition on the rows it found.
- **Not verified with `EXPLAIN` yet.** Whether MariaDB chooses these indexes has not been checked on a MariaDB
  instance. To check: `EXPLAIN SELECT * FROM Orders WHERE status = 'PENDING';` should
  show `idx_orders_status_order_date` in the `key` column, and
  `EXPLAIN SELECT * FROM Products WHERE name LIKE 'clean%';` should show `idx_product_name`. On a table with only a few rows MariaDB may still prefer
  a full scan.

### Views

The script `src/main/resources/sql/views.sql` creates two views. MariaDB only, because `ProductHistory` uses
`FOR SYSTEM_TIME`. It can be run again safely.

| View             | Reads                                                  | One row per                                                              |
| ---------------- | ------------------------------------------------------ | ------------------------------------------------------------------------ |
| `OrderSummaries` | `Orders`, `Customers`, `OrderItems`, grouped per order | order, with customer name, number of different products and total amount |
| `ProductHistory` | all row versions of `Products` (`FOR SYSTEM_TIME ALL`) | version of a product row, with the time range in which it was valid      |

The views are available in the database and are queried with SQL, for example
`SELECT * FROM OrderSummaries WHERE customer_id = 3;` or `SELECT * FROM ProductHistory WHERE product_id = 7;`.
The API has no endpoints for them.

### Triggers

The script `src/main/resources/sql/triggers.sql` creates the audit table `OrderStatusHistory`, one procedure and
three triggers. MariaDB only.

| Object                                    | When                             | What it does                                                                         |
| ----------------------------------------- | -------------------------------- | ------------------------------------------------------------------------------------ |
| trigger `trg_orders_log_status_change`    | after update on `Orders`         | writes previous and new status to `OrderStatusHistory`, only when the status changed |
| procedure `check_product_stock_quantity`  | called by the two triggers below | raises "Stock quantity cannot be negative" for a negative value                      |
| trigger `trg_products_check_stock_insert` | before insert on `Products`      | calls the procedure with the new stock                                               |
| trigger `trg_products_check_stock_update` | before update on `Products`      | calls the procedure with the new stock                                               |

### Entity events

The rules of the trigger script as JPA lifecycle events, so they run on any database.

| Event                                   | Where                                   | What it does                                                                          |
| --------------------------------------- | --------------------------------------- | ------------------------------------------------------------------------------------- |
| `@PostLoad`, `@PostPersist` on `Order`  | `OrderStatusHistoryListener`            | remembers the status the order has in the database                                    |
| `@PostUpdate` on `Order`                | `OrderStatusHistoryListener`            | writes previous and new status to `OrderStatusHistory`, only when the status changed  |
| `@PrePersist`, `@PreUpdate` on `Product` | `Product.rejectNegativeStock`           | throws "Stock quantity cannot be negative" for a negative stock                        |
| `@PrePersist` on `Order`                | `Order.applyDefaults`                   | sets the order date and the status `NEW` when they are missing                        |
| `@PrePersist`, `@PreUpdate`             | `Customer`, `Supplier` `normaliseEmail` | stores the email trimmed and in lower case                                            |

### Temporal data

The script `src/main/resources/sql/temporal.sql` turns `Products` into a system-versioned table. MariaDB only, run it
once. MariaDB then keeps the old version of a row on every update and delete, with the time range in which it was
valid. Normal queries still see only the current rows; the versions are read with SQL through the view `ProductHistory`,
so the price a product had at any time can be looked up.

After this script other `ALTER TABLE` statements on `Products` are refused by MariaDB unless
`SET system_versioning_alter_history = KEEP;` is run first in the same session.

## Running locally

1. Create the database and load the schema:
   ```
   mariadb -u <user> -p -e "CREATE DATABASE webshop"
   mariadb -u <user> -p webshop < src/main/resources/sql/schema_no_data_clean.sql
   mariadb -u <user> -p webshop < src/main/resources/sql/indexes.sql
   mariadb -u <user> -p webshop < src/main/resources/sql/temporal.sql
   mariadb -u <user> -p webshop < src/main/resources/sql/views.sql
   ```
   Run them in this order: `views.sql` needs the versioned table from `temporal.sql`. Run `temporal.sql` only once.
2. Set the connection settings as environment variables:

   | Variable      | Required | Default                                 |
   | ------------- | -------- | --------------------------------------- |
   | `DB_URL`      | no       | `jdbc:mariadb://localhost:3306/webshop` |
   | `DB_USERNAME` | yes      | -                                       |
   | `DB_PASSWORD` | yes      | -                                       |

3. Start the service with `mvn spring-boot:run`.

The tables come from the SQL scripts in `src/main/resources/sql`. Hibernate runs with `ddl-auto: update`: on startup it adds what the entities need and the database lacks, and it never drops anything. With the given schema that is the table `hibernate_sequences` (see below), the table `OrderStatusHistory` and the column `Orders.version` for the optimistic lock.

### Address inheritance

`CustomerAddress` and `SupplierAddress` extend a shared abstract `Address` entity (JPA `TABLE_PER_CLASS` inheritance). Each subclass keeps its own table, `CustomerAddresses` and `SupplierAddresses`, and adds only its foreign key.

This inheritance strategy cannot use `AUTO_INCREMENT` ids, so Hibernate takes address ids from the `hibernate_sequences` table, which Hibernate creates itself on the first startup. Address ids are therefore unique across both address tables and are handed out in blocks, so they are not always consecutive. Addresses must be created through the API: rows inserted directly with SQL get `AUTO_INCREMENT` ids that Hibernate does not know about and can collide with later.

## Tests

Run the tests with `mvn test`. They use an in-memory H2 database, so no database setup is needed.
