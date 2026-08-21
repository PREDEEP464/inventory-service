# Inventory Service

The **Inventory Service** is a Spring Boot microservice responsible for
managing products, categories, stock quantities, product availability,
filtering, inventory reporting, validation, caching, and
inventory-related business operations in the Inventory Management
System.

It is designed as an independent service in a microservice-based
e-commerce-style application and uses **PostgreSQL** as its persistent
database, with **Redis** used as the distributed cache for product data.

The service is also consumed by the **Order Service** during order
creation and cancellation. The Order Service verifies product and stock
information through the Inventory Service and updates inventory after
successful orders.

------------------------------------------------------------------------

## 1. Project Overview

The Inventory Service provides:

-   Product creation and modification
-   Category association
-   Product retrieval
-   Product filtering and pagination
-   Product search
-   Active/inactive product filtering
-   Price-range filtering
-   Stock addition/restocking
-   Stock reduction after successful orders
-   Stock restoration when an order is cancelled
-   Low-stock reporting
-   Inventory summary/statistics
-   Input validation
-   Centralized exception handling
-   Standardized API responses
-   PostgreSQL persistence
-   **Redis-based product caching**
-   Cache invalidation when product data changes
-   Inter-service communication with Order Service

The service follows a layered Spring Boot architecture so controllers,
business logic, data access, entities, request/response models, and
infrastructure concerns remain separated.

------------------------------------------------------------------------

## 2. Architecture

The Inventory Service follows a layered architecture with Redis acting
as the product cache.

``` text
                         Client / Postman
                                |
                                v
                    +----------------------+
                    |    ProductController |
                    |   CategoryController |
                    +----------+-----------+
                               |
                               v
                    +----------------------+
                    |     Service Layer    |
                    | ProductServiceImpl   |
                    | CategoryServiceImpl  |
                    +----------+-----------+
                               |
                 +-------------+-------------+
                 |                           |
                 v                           v
        +----------------+          +----------------+
        |     Redis      |          |   Repository   |
        | Product Cache  |          |     Layer      |
        +----------------+          +-------+--------+
                                           |
                                           v
                                   +---------------+
                                   |  PostgreSQL   |
                                   | products /    |
                                   | categories    |
                                   +---------------+
```

### Product retrieval flow

``` text
HTTP Request
    |
    v
Controller
    |
    v
Product Service
    |
    v
Redis Cache
    |
    +---- Cache HIT ----> Return cached ProductVo
    |
    +---- Cache MISS ---> PostgreSQL
                              |
                              v
                         Store in Redis
                              |
                              v
                       Return ProductVo
```

### Product modification flow

``` text
Update / Stock Change
        |
        v
Product Service
        |
        v
PostgreSQL
        |
        v
Update / Evict Redis Cache
```

Redis therefore reduces repeated database reads while PostgreSQL remains
the source of persistent inventory data.

------------------------------------------------------------------------

## 3. Microservice Role

The Inventory Service owns inventory-related data and operations.

The Order Service does not directly access the Inventory Service's
PostgreSQL database. Instead, it communicates with the Inventory Service
through HTTP APIs.

This maintains service boundaries and prevents the Order Service from
directly manipulating inventory data.

### High-level interaction

``` text
                    +-------------------+
                    |    Order Service  |
                    |     MongoDB       |
                    +---------+---------+
                              |
                         HTTP / REST
                              |
                              v
                    +-------------------+
                    | Inventory Service |
                    | PostgreSQL + Redis|
                    +---------+---------+
                              |
                     +--------+--------+
                     |                 |
                     v                 v
                PostgreSQL          Redis
```

------------------------------------------------------------------------

## 4. Technology Stack

  Technology                    Purpose
  ----------------------------- -----------------------------------
  Java                          Application development
  Spring Boot                   Microservice framework
  Spring Web                    REST API development
  Spring Data JPA               Database access
  Hibernate / JPA               ORM
  PostgreSQL                    Persistent inventory storage
  Redis                         Product caching
  Spring Cache                  Cache abstraction and annotations
  Maven                         Dependency management and build
  Lombok                        Reducing boilerplate code
  Jakarta Validation            Request validation
  SLF4J / Spring Boot Logging   Application logging
  Postman                       API testing and demonstration

------------------------------------------------------------------------

## 5. Database and Cache Architecture

### PostgreSQL

PostgreSQL is the **persistent source of truth** for inventory data.

The main domain entities are:

### Category

Represents a product category.

### Product

Represents an inventory item and contains information such as:

-   Product ID
-   Product code
-   Product name
-   Product description
-   Category
-   Product price
-   Total quantity
-   Available quantity
-   Active/inactive status
-   Created timestamp
-   Updated timestamp

### Redis

Redis is used as a **distributed in-memory cache** for frequently
accessed product information.

The cache is named:

``` text
products
```

A product is cached using its product ID as the cache key.

Conceptually:

``` text
cache name = products
key        = productId
value      = ProductVo
```

For example:

``` text
products::1
products::2
products::7
```

The application does not use the old in-memory `ProductCache` `HashMap`
implementation anymore. Redis is now responsible for product caching.

------------------------------------------------------------------------

## 6. Package Structure

``` text
inventory-service/
│
├── src/
│   └── main/
│       └── java/
│           └── com.inventory.service/
│               │
│               ├── controller/
│               │   ├── CategoryController.java
│               │   └── ProductController.java
│               │
│               ├── dao.api/
│               │   ├── CategoryRepository.java
│               │   └── ProductRepository.java
│               │
│               ├── exception/
│               │   ├── ErrorResponse.java
│               │   └── GlobalExceptionHandler.java
│               │
│               ├── model.entity/
│               │   ├── vo/
│               │   │   ├── ApiResponse.java
│               │   │   ├── CategoryVo.java
│               │   │   ├── InventoryStatisticsVo.java
│               │   │   ├── ProductVo.java
│               │   │   └── StockUpdateVo.java
│               │   │
│               │   ├── Category.java
│               │   └── Product.java
│               │
│               ├── service/
│               │   ├── CategoryService.java
│               │   └── ProductService.java
│               │
│               ├── serviceimpl/
│               │   ├── CategoryServiceImpl.java
│               │   └── ProductServiceImpl.java
│               │
│               ├── specification/
│               │   └── ProductSpecification.java
│               │
│               └── InventoryServiceApplication.java
│
└── ...
```

> The previous local `cache/ProductCache.java` implementation has been
> removed because Redis now provides the product cache.

------------------------------------------------------------------------

## 7. Product Management

The `ProductController` exposes REST endpoints for product management.

Base URL:

``` text
/api/products
```

### Create Product

``` http
POST /api/products
```

Creates a new product and associates it with an existing category.

The request is validated before the service layer processes it.

After creation, the persistent product is stored in PostgreSQL. Cache
state is kept consistent so subsequent product retrievals do not use
stale data.

------------------------------------------------------------------------

### Update Product

``` http
PUT /api/products/{productId}
```

Updates an existing product.

The service first verifies that:

1.  The product exists.
2.  The requested category exists.

Because the product data has changed, the corresponding Redis cache
entry must not remain stale.

------------------------------------------------------------------------

### Get Product By ID

``` http
GET /api/products/{productId}
```

Fetches a product by its database ID.

The product retrieval operation uses Redis caching.

Conceptually:

``` text
GET Product 1
      |
      v
Redis: products::1
      |
      +---- HIT ----> Return cached ProductVo
      |
      +---- MISS ---> Read PostgreSQL
                          |
                          v
                    Store in Redis
                          |
                          v
                    Return ProductVo
```

This avoids querying PostgreSQL for every repeated request for the same
product.

------------------------------------------------------------------------

### Get Products

``` http
GET /api/products
```

The main product listing endpoint supports filtering and pagination.

Supported query parameters include:

``` text
categoryId
isActive
minPrice
maxPrice
name
page
size
sort
```

Example:

``` http
GET /api/products?isActive=true&page=0&size=5
```

The implementation uses Spring Data `Pageable` and a dynamic
`ProductSpecification`.

------------------------------------------------------------------------

### Get Products By Category

``` http
GET /api/products/category/{categoryId}
```

Retrieves products belonging to a particular category.

------------------------------------------------------------------------

### Get Products By Active Status

``` http
GET /api/products/active/{isActive}
```

Retrieves products based on their active/inactive status.

------------------------------------------------------------------------

### Get Products By Price Range

``` http
GET /api/products/price-range?minPrice={minPrice}&maxPrice={maxPrice}
```

Retrieves products whose price falls within the specified range.

------------------------------------------------------------------------

### Search Products By Name

``` http
GET /api/products/search?name={name}
```

Performs case-insensitive product-name searching.

------------------------------------------------------------------------

## 8. Redis Product Caching

Redis caching is implemented using Spring's cache abstraction.

A representative product lookup uses:

``` java
@Cacheable(
        cacheNames = "products",
        key = "#productId"
)
```

This means:

-   `cacheNames = "products"` identifies the cache region.
-   `key = "#productId"` uses the requested product ID as the Redis
    cache key.
-   The returned `ProductVo` becomes the cached value.

For example:

``` text
GET /api/products/1
```

can result in a Redis entry conceptually represented as:

``` text
products::1
```

### Cache Hit

If the product is already cached:

``` text
Request
  ↓
Redis
  ↓
Product found in cache
  ↓
Return ProductVo
```

The database does not need to be queried for that request.

### Cache Miss

If the product is not cached:

``` text
Request
  ↓
Redis
  ↓
Cache miss
  ↓
PostgreSQL
  ↓
Product retrieved
  ↓
Redis cache populated
  ↓
Return ProductVo
```

### Why Redis instead of a Java HashMap?

The earlier local `ProductCache` used:

``` java
Map<Long, ProductVo>
```

inside the application process.

That approach has limitations:

-   Cache exists only inside one application instance.
-   Data disappears when the application restarts.
-   It is not suitable for sharing cache state between multiple
    application instances.
-   Cache management becomes an application-memory concern.

Redis provides a separate, centralized in-memory caching layer that can
be accessed by application instances.

------------------------------------------------------------------------

## 9. Cache Consistency

Caching introduces an important requirement: **stale data must not be
served after product changes.**

Whenever a product is updated or its stock state changes, the Redis
entry corresponding to that product must be refreshed or invalidated
according to the service implementation.

Conceptually:

``` text
Product Update
      |
      v
PostgreSQL updated
      |
      v
Redis product cache updated/evicted
      |
      v
Next GET retrieves current data
```

This keeps PostgreSQL as the persistent source of truth while Redis
provides fast reads.

------------------------------------------------------------------------

## 10. Stock Management

Stock operations are separated based on their business purpose.

### Add / Restock Inventory

``` http
PATCH /api/products/re-stock
```

This operation is intended for inventory/admin stock updates.

It increases both:

``` text
totalQuantity
availableQuantity
```

Example:

``` text
Before:
Total     = 50
Available = 20

Admin adds 10 units

After:
Total     = 60
Available = 30
```

The product cache must remain consistent after the stock update.

------------------------------------------------------------------------

### Reduce Available Stock

``` http
PATCH /api/products/stock/reduce
```

This operation is used by the Order Service after a successful order.

Only `availableQuantity` is reduced.

Example:

``` text
Before:
Total     = 53
Available = 47

Order quantity = 8

After:
Total     = 53
Available = 39
```

The service prevents the available quantity from becoming negative.

Because stock is product state, the related Redis product entry must
also be kept consistent after the update.

------------------------------------------------------------------------

### Restore Stock

``` http
PATCH /api/products/stock/restore
```

Stock restoration is used when a previously placed order is cancelled.

The restoration operation increases available quantity without
increasing total physical inventory quantity.

Example:

``` text
Before cancellation:
Total     = 53
Available = 39

Cancelled order quantity = 8

After cancellation:
Total     = 53
Available = 47
```

------------------------------------------------------------------------

## 11. Low-Stock API

The service provides low-stock reporting to identify products whose
available inventory has reached the configured low-stock threshold.

This is useful for:

-   Inventory monitoring
-   Replenishment decisions
-   Admin dashboards
-   Stock alerts

The reporting logic remains inside the Inventory Service because
inventory thresholds and stock information belong to the inventory
domain.

------------------------------------------------------------------------

## 12. Inventory Summary / Statistics

The service provides an inventory summary/reporting capability.

The summary aggregates inventory information such as product and
quantity-related statistics.

This functionality demonstrates practical use of Java collection
processing and Stream operations.

------------------------------------------------------------------------

## 13. Filtering and Dynamic Queries

Product filtering is implemented using Spring Data JPA Specifications.

The `ProductSpecification` dynamically builds query conditions based on
the parameters provided by the client.

Possible filters include:

-   Category
-   Active status
-   Minimum price
-   Maximum price
-   Product name

This allows combinations such as:

``` text
Active products
+
Category
+
Price range
+
Name
```

Pagination is handled using Spring Data's `Pageable`.

------------------------------------------------------------------------

## 14. Validation

The service uses **Jakarta Bean Validation** to validate incoming API
requests.

Examples include:

``` java
@NotNull
@NotBlank
@NotEmpty
@Positive
```

Invalid input is rejected before the business operation proceeds.

Validation protects the service from:

-   Missing required values
-   Empty names
-   Invalid quantities
-   Invalid identifiers
-   Invalid request fields

Validation errors are handled centrally by the global exception handler.

------------------------------------------------------------------------

## 15. Standardized API Response

The service uses an `ApiResponse` wrapper for successful API responses.

Conceptually:

``` json
{
    "message": "Product fetched successfully",
    "data": {
        "...": "..."
    }
}
```

This provides a consistent API contract for clients consuming the
service.

------------------------------------------------------------------------

## 16. ErrorResponse

The service uses a dedicated `ErrorResponse` model for error responses.

A typical error response contains:

``` json
{
    "timestamp": "2026-08-09T20:32:15.187341",
    "status": 404,
    "error": "Not Found",
    "message": "Product not found"
}
```

------------------------------------------------------------------------

## 17. Global Exception Handling

The service uses:

``` java
@RestControllerAdvice
```

through `GlobalExceptionHandler`.

This allows exceptions to be handled centrally rather than placing
repetitive `try/catch` blocks inside controllers.

Important cases include:

``` text
MethodArgumentNotValidException
→ 400 Bad Request

DataIntegrityViolationException
→ 409 Conflict

Product not found
→ 404 Not Found
```

------------------------------------------------------------------------

## 18. HTTP Status Codes

  Operation                 Status
  ------------------------- ---------------------------
  Successful GET            200 OK
  Successful PUT/PATCH      200 OK
  Successful DELETE         200 OK
  Product created           201 Created
  Validation failure        400 Bad Request
  Resource not found        404 Not Found
  Data conflict             409 Conflict
  Unexpected server error   500 Internal Server Error

------------------------------------------------------------------------

## 19. Streams, Lambdas and Method References

Java Stream API is used where collection transformation or aggregation
is useful.

For example:

``` java
productRepository.findAll()
        .stream()
        .map(this::convertToVo)
        .toList();
```

This demonstrates:

-   Stream API
-   Lambda-style processing
-   Method references
-   Collection transformation

Streams are also used for inventory reporting/statistics.

------------------------------------------------------------------------

## 20. DTO / VO Mapping

The service separates persistence entities from API-facing objects.

``` text
Product Entity
      ↓
convertToVo()
      ↓
ProductVo
```

`ProductVo` is used for API communication while `Product` represents the
database model.

This avoids directly exposing persistence entities as the API contract.

------------------------------------------------------------------------

## 21. Constructor Injection

Dependencies are provided through constructors.

For example:

``` java
public ProductServiceImpl(
        ProductRepository productRepository,
        CategoryRepository categoryRepository) {

    this.productRepository = productRepository;
    this.categoryRepository = categoryRepository;
}
```

This makes dependencies explicit and improves maintainability and
testability.

------------------------------------------------------------------------

## 22. Logging

Spring Boot logging infrastructure and SLF4J are used for meaningful
application events such as:

-   Product operations
-   Stock updates
-   Stock reductions
-   Important failures
-   Cache-related application events where useful

Logging is intentionally focused on meaningful application events.

------------------------------------------------------------------------

## 23. Inter-Service Communication

The Inventory Service is consumed by the Order Service through REST
APIs.

The Order Service communicates with inventory operations such as:

``` text
GET product
    ↓
Verify product exists
    ↓
Verify active status
    ↓
Verify available quantity
    ↓
Create order
    ↓
Reduce available inventory
```

When an order is cancelled:

``` text
Order cancellation
       ↓
Inventory Service
       ↓
Restore available quantity
```

The Inventory Service remains responsible for modifying its own
inventory data.

------------------------------------------------------------------------

## 24. Order and Inventory Stock Lifecycle

``` text
ADMIN RESTOCK
     |
     v
totalQuantity + quantity
availableQuantity + quantity


ORDER PLACED
     |
     v
availableQuantity - quantity
totalQuantity unchanged


ORDER CANCELLED
     |
     v
availableQuantity + quantity
totalQuantity unchanged
```

Redis caching sits alongside this lifecycle to provide fast product
reads while PostgreSQL remains the persistent source of truth.

------------------------------------------------------------------------

## 25. API Testing with Postman

The Inventory Service APIs are tested using Postman.

Testing includes:

### Product Operations

-   Create product
-   Update product
-   Get product
-   Get all/filter products
-   Search products
-   Category filtering
-   Active status filtering
-   Price-range filtering

### Stock Operations

-   Add stock
-   Reduce stock
-   Restore stock through cancellation flow

### Validation / Error Cases

-   Invalid product ID
-   Invalid input
-   Invalid quantity
-   Insufficient stock
-   Invalid category
-   Data conflicts

### Reporting

-   Low-stock reporting
-   Inventory summary/statistics

### Redis Cache Verification

Product retrieval can also be demonstrated through Redis:

``` text
First request
    ↓
Redis cache miss
    ↓
PostgreSQL
    ↓
Product stored in Redis

Repeated request
    ↓
Redis cache hit
    ↓
Product returned without another database read
```

The Postman workspace/collection is used as the API testing and
demonstration reference for the project.

------------------------------------------------------------------------

## 26. Running the Service

### Prerequisites

Install/configure:

-   Java
-   Maven
-   PostgreSQL
-   Redis
-   Postman

### Database

Create/configure the PostgreSQL database used by the Inventory Service.

Update the application's database configuration with the appropriate
local PostgreSQL connection details.

### Redis

Make sure Redis is running locally before starting the Inventory
Service.

The application uses Redis for product caching.

### Start the Service

From the Inventory Service project directory:

``` bash
mvn spring-boot:run
```

Alternatively, run the Spring Boot main application class from the IDE.

Once the application starts, the REST APIs can be tested through
Postman.

------------------------------------------------------------------------

## 27. Important Design Decisions

### Why PostgreSQL?

Inventory data has structured relationships such as:

``` text
Category
   ↓
Product
```

A relational database is suitable for this structured domain and
supports constraints and transactional data operations.

### Why Redis?

Product retrieval is a frequent read operation.

Redis provides a fast in-memory cache so repeated product lookups do not
always require a PostgreSQL query.

The architecture therefore separates responsibilities:

``` text
PostgreSQL
    ↓
Persistent source of truth

Redis
    ↓
Fast product cache
```

### Why not use the old `ProductCache` HashMap?

The old implementation stored products in the application's local
memory:

``` java
Map<Long, ProductVo>
```

That cache was tied to a single application process.

Redis is a better fit for the microservice architecture because the
cache is external to the application process and can be shared by
multiple application instances.

### Why separate Inventory and Order services?

Each service owns a specific business responsibility.

``` text
Inventory Service → Products, categories, stock
Order Service     → Orders, customers, order lifecycle
```

This keeps responsibilities separated and allows services to evolve
independently.

### Why does Order Service call Inventory Service?

The Order Service should not directly modify another service's database.

Instead:

``` text
Order Service
      ↓ HTTP
Inventory Service API
      ↓
PostgreSQL
```

This preserves microservice boundaries.

### Why separate total and available quantity?

`totalQuantity` represents physical inventory, while `availableQuantity`
represents stock currently available for ordering.

This allows cancellation to restore available stock without incorrectly
increasing physical inventory.

### Why use `PATCH` for stock changes?

Stock operations modify only part of the product resource, specifically
its quantity-related state. `PATCH` therefore represents a partial
update.

### Why use `GlobalExceptionHandler`?

Centralized exception handling keeps controllers clean and provides
consistent HTTP error responses.

### Why use `ApiResponse` and `ErrorResponse`?

They provide predictable and consistent response structures for clients.

------------------------------------------------------------------------

## 28. Key Learning Concepts Demonstrated

This service demonstrates practical use of:

-   Spring Boot
-   REST APIs
-   Dependency Injection
-   Constructor Injection
-   Spring MVC
-   Spring Data JPA
-   Hibernate / ORM
-   PostgreSQL
-   Redis
-   Spring Cache
-   `@Cacheable`
-   Cache keys and cache names
-   Cache invalidation / consistency
-   Entity relationships
-   DTO / VO mapping
-   Bean Validation
-   Global exception handling
-   Custom API response structures
-   HTTP status codes
-   Pagination
-   Spring Data Specifications
-   Java Streams
-   Lambdas
-   Method references
-   Inventory business logic
-   Microservice communication
-   Logging
-   Postman API testing

------------------------------------------------------------------------

## 29. Related Service

The Inventory Service works together with the **Order Service**.

The Order Service is responsible for:

-   Creating orders
-   Validating inventory through the Inventory Service
-   Calculating order totals
-   Reducing available inventory after successful orders
-   Cancelling orders
-   Restoring available inventory after cancellation

The Inventory Service is responsible for:

-   Product and category management
-   Stock management
-   Persistent inventory state
-   Redis product caching
-   Maintaining cache consistency after product changes

Together, the two services form the core of the Inventory Management
System.

------------------------------------------------------------------------

## 30. Project Status

The Inventory Service currently includes:

-   Product management
-   Category management
-   Stock management
-   Product filtering and pagination
-   Validation
-   Centralized exception handling
-   Standardized API responses
-   Low-stock reporting
-   Inventory summary/statistics
-   Stream-based processing
-   Order Service integration
-   **Redis product caching**
-   **Spring Cache / `@Cacheable` implementation**
-   **Product-based Redis cache keys**
-   **Cache consistency handling for product changes**
-   Postman API testing
-   Professional layered architecture

------------------------------------------------------------------------

**Inventory Service --- Spring Boot + PostgreSQL + Redis Microservice**
