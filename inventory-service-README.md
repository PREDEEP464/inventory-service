# Inventory Service

The **Inventory Service** is a Spring Boot microservice responsible for
managing products, categories, stock quantities, product availability,
filtering, inventory reporting, validation, and inventory-related
business operations in the Inventory Management System.

It is designed as an independent service in a microservice-based
e-commerce-style application and uses **PostgreSQL** as its persistent
database.

The service is also consumed by the **Order Service** during order
creation and cancellation. The Order Service verifies product and stock
information through the Inventory Service and updates inventory after
successful orders.

------------------------------------------------------------------------

## 1. Project Overview

The Inventory Service provides the inventory-management capabilities of
the application:

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

The service follows a layered Spring Boot architecture so that
controllers, business logic, data access, entities, and request/response
models remain separated.

------------------------------------------------------------------------

## 2. Architecture

The Inventory Service follows a layered architecture:

``` text
Client / Postman
       |
       v
+----------------------+
|   ProductController  |
|   CategoryController |
+----------------------+
       |
       v
+----------------------+
|     Service Layer    |
| ProductServiceImpl   |
| CategoryServiceImpl  |
+----------------------+
       |
       v
+----------------------+
|   Repository Layer   |
| ProductRepository    |
| CategoryRepository   |
+----------------------+
       |
       v
+----------------------+
|      PostgreSQL      |
| products / categories|
+----------------------+
```

For product operations, the request flow is:

``` text
HTTP Request
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
    ↓
Entity
    ↓
VO / API Response
```

Exceptions are handled centrally through:

``` text
Controller / Service
        ↓
Exception
        ↓
GlobalExceptionHandler
        ↓
ErrorResponse
        ↓
HTTP Error Response
```

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
                    |    PostgreSQL     |
                    +-------------------+
```

------------------------------------------------------------------------

## 4. Technology Stack

  Technology                    Purpose
  ----------------------------- ---------------------------------
  Java                          Application development
  Spring Boot                   Microservice framework
  Spring Web                    REST API development
  Spring Data JPA               Database access
  Hibernate / JPA               ORM
  PostgreSQL                    Inventory persistence
  Maven                         Dependency management and build
  Lombok                        Reducing boilerplate code
  Jakarta Validation            Request validation
  SLF4J / Spring Boot Logging   Application logging
  Postman                       API testing and documentation

------------------------------------------------------------------------

## 5. Database Architecture

The Inventory Service uses **PostgreSQL**.

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

### Quantity Model

The service intentionally maintains two stock values:

``` text
totalQuantity
availableQuantity
```

`totalQuantity` represents the total physical stock owned by the
inventory.

`availableQuantity` represents the quantity currently available for new
orders.

For example:

``` text
Total Quantity     = 53
Available Quantity = 47
```

If an order for 6 units is placed:

``` text
Total Quantity     = 53
Available Quantity = 41
```

The order consumes available stock; it does not reduce the total
physical quantity.

When an order is cancelled, the available quantity is restored.

------------------------------------------------------------------------

## 6. Package Structure

The service follows a layered package organization.

``` text
inventory-service/
│
├── src/
│   └── main/
│       └── java/
│           └── com.inventory.service/
│               │
│               ├── cache/
│               │   └── ProductCache.java
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


### Package responsibility

``` text
cache
    └── Product caching functionality

controller
    └── REST API endpoints

dao.api
    └── Spring Data JPA repositories / database access

exception
    ├── ErrorResponse
    └── Global exception handling

model.entity
    ├── JPA entities
    └── VO / DTO objects used for API communication

service
    └── Service interfaces

serviceimpl
    └── Business logic implementations

specification
    └── Dynamic product filtering specifications
```

The exact package names are kept consistent with the implementation.

------------------------------------------------------------------------

# 7. Product Management

The `ProductController` exposes REST endpoints for product management.

Base URL:

``` text
/api/products
```

## Create Product

``` http
POST /api/products
```

Creates a new product and associates it with an existing category.

The request is validated before the service layer processes it.

------------------------------------------------------------------------

## Update Product

``` http
PUT /api/products/{productId}
```

Updates an existing product.

The service first verifies that:

1.  The product exists.
2.  The requested category exists.

------------------------------------------------------------------------

## Get Product By ID

``` http
GET /api/products/{productId}
```

Fetches a product by its database ID.

If the product does not exist, the centralized exception handler returns
an appropriate `404 Not Found` response.

------------------------------------------------------------------------

## Get Products

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

## Get Products By Category

``` http
GET /api/products/category/{categoryId}
```

Retrieves products belonging to a particular category.

------------------------------------------------------------------------

## Get Products By Active Status

``` http
GET /api/products/active/{isActive}
```

Retrieves products based on their active/inactive status.

------------------------------------------------------------------------

## Get Products By Price Range

``` http
GET /api/products/price-range?minPrice={minPrice}&maxPrice={maxPrice}
```

Retrieves products whose price falls within the specified range.

------------------------------------------------------------------------

## Search Products By Name

``` http
GET /api/products/search?name={name}
```

Performs case-insensitive product-name searching.

------------------------------------------------------------------------

# 8. Stock Management

Stock operations are separated based on their business purpose.

## Add / Restock Inventory

``` http
PATCH /api/products/{productId}/stock
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

------------------------------------------------------------------------

## Reduce Available Stock

``` http
PATCH /api/products/{productId}/stock/reduce
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

------------------------------------------------------------------------

## Restore Stock

Stock restoration is used when a previously placed order is cancelled.

The restoration operation increases the available quantity without
increasing the total physical inventory quantity.

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

This distinction prevents cancelled orders from incorrectly appearing as
newly purchased inventory.

------------------------------------------------------------------------

# 9. Low-Stock API

The service provides a low-stock reporting capability to identify
products whose available inventory has reached the configured low-stock
threshold.

This is useful for:

-   Inventory monitoring
-   Replenishment decisions
-   Admin dashboards
-   Stock alerts

The implementation keeps this reporting logic inside the Inventory
Service because inventory thresholds and stock information belong to the
inventory domain.

------------------------------------------------------------------------

# 10. Inventory Summary / Statistics

The service also provides an inventory summary/reporting capability.

The summary aggregates inventory information such as product and
quantity-related statistics.

This reporting functionality demonstrates practical use of Java
collection processing and Stream operations.

The statistics API is intended to provide a quick overview for an
administrator instead of requiring the client to retrieve and manually
aggregate every product.

------------------------------------------------------------------------

# 11. Filtering and Dynamic Queries

Product filtering is implemented using Spring Data JPA Specifications.

The `ProductSpecification` dynamically builds query conditions based on
the parameters provided by the client.

Possible filters include:

-   Category
-   Active status
-   Minimum price
-   Maximum price
-   Product name

This allows one endpoint to support combinations such as:

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

This avoids creating a separate repository method for every possible
combination of filters.

------------------------------------------------------------------------

# 12. Validation

The service uses **Jakarta Bean Validation** to validate incoming API
requests.

Validation is applied to request/VO objects using annotations such as:

``` java
@NotNull
@NotBlank
@NotEmpty
@Positive
```

Invalid input is rejected before the business operation proceeds.

This protects the service from invalid data such as:

-   Missing required values
-   Empty names
-   Invalid quantities
-   Invalid identifiers
-   Invalid request fields

Validation errors are handled centrally by the global exception handler.

------------------------------------------------------------------------

# 13. Standardized API Response

The service uses an `ApiResponse` wrapper for successful API responses.

Instead of returning different response structures from every controller
method, successful responses follow a consistent structure.

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

# 14. ErrorResponse

The service also uses a dedicated `ErrorResponse` model for error
responses.

The purpose is to provide a consistent structure for failures.

A typical error response contains:

``` json
{
    "timestamp": "2026-08-09T20:32:15.187341",
    "status": 404,
    "error": "Not Found",
    "message": "Product not found"
}
```

This separates successful API response structures from error response
structures.

------------------------------------------------------------------------

# 15. Global Exception Handling

The service uses:

``` java
@RestControllerAdvice
```

through `GlobalExceptionHandler`.

This allows exceptions from controller/service processing to be handled
centrally rather than placing repetitive `try/catch` blocks inside every
controller.

The handler currently covers important cases including:

### Validation Errors

``` text
MethodArgumentNotValidException
→ 400 Bad Request
```

### Data Conflicts

``` text
DataIntegrityViolationException
→ 409 Conflict
```

### Runtime / Not Found Errors

Runtime exceptions are mapped to suitable HTTP responses based on the
error condition.

For example:

``` text
Product not found
→ 404 Not Found
```

This was verified through Postman testing.

------------------------------------------------------------------------

# 16. HTTP Status Codes

The service follows HTTP semantics for its REST APIs.

Examples:

  Operation                                      Status
  ------------------------- ---------------------------
  Successful GET                                 200 OK
  Successful PUT/PATCH                           200 OK
  Successful DELETE                              200 OK
  Product created                           201 Created
  Validation failure                    400 Bad Request
  Resource not found                      404 Not Found
  Data conflict                            409 Conflict
  Unexpected server error     500 Internal Server Error

------------------------------------------------------------------------

# 17. Streams, Lambdas and Method References

Java Stream API is used where collection transformation or aggregation
is useful.

For example, product entities are converted to response objects using a
stream:

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

Streams are also useful for inventory reporting/statistics where data
needs to be filtered, mapped, counted, or aggregated.

The project uses Streams where they provide meaningful collection
processing rather than forcing them into every method.

------------------------------------------------------------------------

# 18. DTO / VO Mapping

The service separates persistence entities from API-facing objects.

For example:

``` text
Product Entity
      ↓
convertToVo()
      ↓
ProductVo
```

The `ProductVo` is used for API communication while the `Product` entity
represents the database model.

This avoids directly exposing persistence entities as the API contract
and keeps the API model independent from the database model.

------------------------------------------------------------------------

# 19. Constructor Injection

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

This is dependency injection provided by Spring.

It makes dependencies explicit and supports easier testing and
maintainability.

The project therefore does not require field-level `@Autowired`
injection for these dependencies.

------------------------------------------------------------------------

# 20. Logging

Spring Boot's logging infrastructure and SLF4J are used for application
logging.

The service layer can log meaningful business events such as:

-   Product operations
-   Stock updates
-   Stock reductions
-   Important failures

Logging is intentionally focused on meaningful application events rather
than logging every individual line of execution.

------------------------------------------------------------------------

# 21. Inter-Service Communication

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

The Inventory Service remains responsible for actually modifying its own
inventory data.

------------------------------------------------------------------------

# 22. Order and Inventory Stock Lifecycle

The important stock lifecycle is:

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

This distinction is an important business rule of the system.

------------------------------------------------------------------------

# 23. API Testing with Postman

The Inventory Service APIs were tested using Postman.

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

The Postman workspace/collection is used as the API testing and
demonstration reference for the project.

------------------------------------------------------------------------

# 24. Running the Service

## Prerequisites

Install/configure:

-   Java
-   Maven
-   PostgreSQL
-   Postman (for API testing)

## Database

Create/configure the PostgreSQL database used by the Inventory Service.

Update the application's database configuration with the appropriate
local PostgreSQL connection details.

## Start the Service

From the Inventory Service project directory:

``` bash
mvn spring-boot:run
```

Alternatively, run the Spring Boot main application class from the IDE.

Once the application starts, the REST APIs can be tested through
Postman.

------------------------------------------------------------------------

# 25. Important Design Decisions

### Why PostgreSQL?

Inventory data has structured relationships such as:

``` text
Category
   ↓
Product
```

A relational database is suitable for this structured domain and
supports constraints and transactional data operations.

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
Inventory Database
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

### Why use a cancellation endpoint instead of deleting an order?

An order represents a business transaction and should retain its
history.

Therefore cancellation changes the state:

``` text
PLACED → CANCELLED
```

instead of physically deleting the order document.

### Why use `GlobalExceptionHandler`?

Centralized exception handling keeps controllers clean and provides
consistent HTTP error responses across the service.

### Why use `ApiResponse` and `ErrorResponse`?

They provide predictable and consistent response structures for clients.

Successful operations use `ApiResponse`, while failures use
`ErrorResponse`.

------------------------------------------------------------------------

# 26. Key Learning Concepts Demonstrated

This service demonstrates practical use of:

-   Spring Boot
-   REST APIs
-   Dependency Injection
-   Constructor Injection
-   Spring MVC
-   Spring Data JPA
-   Hibernate / ORM
-   PostgreSQL
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

## 27. Related Service

The Inventory Service works together with the **Order Service**.

The Order Service is responsible for:

-   Creating orders
-   Validating inventory through the Inventory Service
-   Calculating order totals
-   Reducing available inventory after successful orders
-   Cancelling orders
-   Restoring available inventory after cancellation

Together, the two services form the core of the Inventory Management
System.

------------------------------------------------------------------------

## 28. Project Status

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
-   Postman API testing
-   Professional layered architecture

------------------------------------------------------------------------

**Inventory Service --- Spring Boot + PostgreSQL Microservice**
