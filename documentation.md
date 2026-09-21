# Orders — Technical Documentation

> Implements an Orders microservice that creates and persists customer orders by orchestrating calls to customer, address, card, cart, payment, and shipping services, exposing a REST API and Prometheus 

*Generated from `microservices-demo/orders` at `546a10c92d1f` — 38 source files, 410 symbols, 922 relationships analysed.*

---

## Contents

- [Project Overview](#project-overview)
- [How the System Works](#how-the-system-works)
- [Technology Stack](#technology-stack)
- [Repository Structure](#repository-structure)
- [Architecture](#architecture)
- [Components](#components)
- [Execution Flows](#execution-flows)
- [Build, Tooling & CI](#build-tooling--ci)
- [Developer Guide](#developer-guide)
- [File Reference](#file-reference)

---

## Project Overview

Implements an Orders microservice that creates and persists customer orders by orchestrating calls to customer, address, card, cart, payment, and shipping services, exposing a REST API and Prometheus metrics.

> **Repository type**
> This is a **service** repository — 8 HTTP endpoint(s) and 2 database interaction(s).

|  |  |
|---|---|
| Domain | e-commerce order processing |
| Languages | Java, Python, Shell |
| Entry points | `works.weave.socks.orders.OrderApplication.main`, `works.weave.socks.orders.controllers.OrdersController.newOrder`, `works.weave.socks.orders.controllers.HealthCheckController.getHealth` |
| External systems | MongoDB orders-db |

*Source: `src/main/java/works/weave/socks/orders/controllers/OrdersController.java:49-126`*

---

## How the System Works

> **Key idea**
> Controllers call service and repository beans via Spring dependency injection, AsyncGetService uses RestTemplate (optionally via an HTTP proxy) to call external services, and Spring Data MongoDB persists CustomerOrder entities through CustomerOrderRepository.

The system's main paths are:

1. POST /orders validates the NewOrderResource, fetches customer/address/card/items from other services, calculates total, authorises payment, triggers shipping, and writes a CustomerOrder via CustomerOrderRepository.
1. GET /health checks application liveness and runs a MongoDB buildInfo command to report database health.

**The rules that shape its behaviour**

- An order request must include customer, address, card, and items or an InvalidOrderException is thrown with HTTP 406.
- Order total is computed as the sum of item quantity × unitPrice plus a flat shipping fee of 4.99.
- Payment must be successfully authorised by the payment service; missing or unauthorised responses cause a PaymentDeclinedException with HTTP 406.
- If the user ID cannot be parsed from a resource href, order creation fails with an IllegalStateException.

---

## Technology Stack

Every entry below was detected in the repository — from source files, route annotations, import statements or build manifests.

| Technology | Purpose | Evidence |
|---|---|---|
| **Java** | Implementation language | 26 parsed source file(s) |
| **Javascript** | Implementation language | 1 parsed source file(s) |
| **Python** | Implementation language | 8 parsed source file(s) |
| **Shell** | Implementation language | 3 parsed source file(s) |
| **Flask** | HTTP API framework | imported in test/json-server/server.py |
| **Maven** | Build and dependency management | pom.xml is present |
| **Docker** | Container image build | docker/orders/Dockerfile is present |
| **GitHub Actions** | Continuous integration | 1 workflow file(s) |

---

## Repository Structure

Directories that contain analysed source, largest first.

| Directory | Files | Role |
|---|---|---|
| `src/main/java/works/weave/socks/orders/entities/` | 8 | — |
| `src/main/java/works/weave/socks/orders/config/` | 6 | — |
| `test/` | 5 | — |
| `src/main/java/works/weave/socks/orders/resources/` | 3 | — |
| `test/util/` | 3 | — |
| `scripts/` | 2 | — |
| `src/main/java/works/weave/socks/orders/controllers/` | 2 | Order orchestration REST controller |
| `src/main/java/works/weave/socks/orders/values/` | 2 | — |
| `api-spec/` | 1 | — |
| `src/main/java/works/weave/socks/orders/` | 1 | — |
| `src/main/java/works/weave/socks/orders/middleware/` | 1 | HTTP metrics middleware |
| `src/main/java/works/weave/socks/orders/repositories/` | 1 | repository |
| `src/main/java/works/weave/socks/orders/services/` | 1 | HTTP integration service |
| `src/test/java/works/weave/socks/orders/entities/` | 1 | — |
| `test/json-server/` | 1 | — |

> **Note**
> 2 file(s) were excluded from analysis: 1 × binary or media file, 1 × empty file.

---

## Architecture

Spring Boot microservice with RESTful endpoints, MongoDB persistence, and Prometheus/Zipkin observability.

**Layers:** `API layer (Spring MVC and Spring Data REST controllers)` → `Service/integration layer (AsyncGetService, RestProxyTemplate)` → `Persistence layer (Spring Data MongoDB repositories and entities)` → `Observability/middleware (HTTPMonitoringInterceptor, PrometheusAutoConfiguration)`

**System architecture**

```text
  API Layer
  +---------------------------+
  | OrdersController          |
  +---------------------------+
                |
                v
  Persistence
  +---------------------------+
  | CustomerOrderRepository   |
  +---------------------------+
                |
                v
  Application Layer
  +---------------------------+
  | AsyncGetService           |
  +---------------------------+
                |
                v
  Other
  +---------------------------+
  | HTTPMonitoringInterceptor |
  +---------------------------+

  Connections
    OrdersController --> CustomerOrderRepository   (After successful payment and shipping, OrdersController writes a CustomerOrder entity)
    OrdersController --> AsyncGetService
```

*Components grouped by layer. Every edge is a relationship resolved by static analysis.*

**How the parts talk to each other**

| From | To | Interaction |
|---|---|---|
| `OrdersController` | `CustomerOrderRepository` | After successful payment and shipping, OrdersController writes a CustomerOrder entity using CustomerOrderRepository. |

The service is designed as part of a larger microservices-demo system, relying on other services for customer, address, card, cart, payment, and shipping data, with observability via Prometheus and Zipkin.

---

## Components

### CustomerOrderRepository

`works.weave.socks.orders.repositories.CustomerOrderRepository` · *repository*

Provides CRUD and findByCustomerId operations for CustomerOrder entities and exposes them via Spring Data REST at /orders.

|  |  |
|---|---|
| Inputs | CustomerOrder entities and customerId query parameters. |
| Outputs | Stored CustomerOrder documents in MongoDB and REST resources for orders. |
| Used by | `OrdersController` |
| Key symbols | `findByCustomerId` |

Delegates persistence and query operations to Spring Data MongoDB based on method naming conventions.

> **Failure behaviour**
> Relies on Spring Data's exception handling for database errors; no custom handling is defined here.

*Source: `src/main/java/works/weave/socks/orders/repositories/CustomerOrderRepository.java:11-15`*

### AsyncGetService

`works.weave.socks.orders.services.AsyncGetService` · *HTTP integration service*

Performs asynchronous HTTP GET and POST requests to other microservices and returns Future results.

|  |  |
|---|---|
| Inputs | URIs and type references for resources to fetch or post. |
| Outputs | Future-wrapped resources, lists, or responses from external services. |
| Depends on | `RestProxyTemplate` |
| Used by | `OrdersController` |
| Key symbols | `getResource`, `getDataList`, `getDataList`, `postResource` |

Wraps RestTemplate calls in asynchronous operations to parallelise remote calls during order creation.

> **Failure behaviour**
> Propagates exceptions from HTTP calls and Future.get timeouts back to callers; specific handling is in OrdersController.

*Source: `src/main/java/works/weave/socks/orders/services/AsyncGetService.java:32-93`, `src/main/java/works/weave/socks/orders/controllers/OrdersController.java:49-126`*

### OrdersController

`works.weave.socks.orders.controllers.OrdersController` · *Order orchestration REST controller*

Validates incoming order requests, calls external services to resolve references and authorise payment, triggers shipping, and persists the resulting CustomerOrder.

|  |  |
|---|---|
| Inputs | HTTP POST /orders with a JSON NewOrderResource containing links to customer, address, card, and items. |
| Outputs | Returns a persisted CustomerOrder resource or HTTP 406 errors on validation or payment failure. |
| Depends on | `OrdersConfigurationProperties`, `AsyncGetService`, `CustomerOrderRepository` |
| Key symbols | `newOrder`, `parseId`, `calculateTotal` |

Performs null checks on required fields, asynchronously fetches referenced resources, computes total, posts a PaymentRequest to the payment service, and on success proceeds to shipping and repository save.

> **Failure behaviour**
> Throws InvalidOrderException for missing fields, PaymentDeclinedException for payment issues, and IllegalStateException if IDs cannot be parsed from hrefs.

*Source: `src/main/java/works/weave/socks/orders/controllers/OrdersController.java:33-175`*

### HTTPMonitoringInterceptor

`works.weave.socks.orders.middleware.HTTPMonitoringInterceptor` · *HTTP metrics middleware*

Measures request latency and records it in a Prometheus Histogram labelled by service, method, path, and status code.

|  |  |
|---|---|
| Inputs | Incoming HttpServletRequest and HttpServletResponse objects for all mapped endpoints. |
| Outputs | Prometheus metrics for request durations with resolved URL patterns. |
| Key symbols | `preHandle`, `postHandle`, `afterCompletion`, `getMatchingURLPattern`, `getUrlPatterns` |

Stores a start time at preHandle, computes elapsed time at postHandle, resolves the best-matching URL pattern, and observes the latency in the histogram.

> **Failure behaviour**
> If no URL pattern matches or the path is /error, it skips recording metrics; it does not alter request processing on failure.

*Source: `src/main/java/works/weave/socks/orders/middleware/HTTPMonitoringInterceptor.java:21-99`*

---

## Execution Flows

Each flow below follows resolved call edges through the code. Every step names the symbol that performs it.

### Create Order (POST /orders)

**Trigger:** `HTTP POST request to /orders with a NewOrderResource payload.`

**Entry point:** `works.weave.socks.orders.controllers.OrdersController.newOrder`

```text
  +======================================================+
  | HTTP POST request to /orders with a NewOrderResource |
  | payload.                                             |
  +======================================================+
                              |
                              v
  +------------------------------------------------------+
  | Validate that address, customer, card, and items     |
  | fields are non-null, otherwise throw                 |
  | InvalidOrderException.                               |
  +------------------------------------------------------+
                              |
                              |  Always executed after validation passes.
                              v
  +------------------------------------------------------+
  | Start asynchronous HTTP GET requests to fetch        |
  | Address, Customer, Card resources and a ListItem     |
  | from the provided URIs.                              |
  +------------------------------------------------------+
                              |
                              |  itemsFuture.gettimeout, TimeUnit.SECONDS succeeds.
                              v
  +------------------------------------------------------+
  | Wait for itemsFuture with a timeout and compute the  |
  | total amount as sum of quantity × unitPrice plus a   |
  | flat shipping fee.                                   |
  +------------------------------------------------------+
                              |
                              |  addressFuture, cardFuture, and customerFuture complete
                              |  successfully within timeout.
                              v
  +------------------------------------------------------+
  | Build a PaymentRequest from resolved resources, POST |
  | it to the payment service, and ensure the            |
  | PaymentResponse is non-null and authorised,          |
  | otherwise…                                           |
  +------------------------------------------------------+
                              |
                              |  PaymentResponse is authorised.
                              v
  +------------------------------------------------------+
  | Trigger shipping for the order using the shipping    |
  | service and construct a Shipment object exact        |
  | implementation partially truncated.                  |
  +------------------------------------------------------+
                              |
                              |  All previous steps succeed without throwing exceptions.
                              v
  +------------------------------------------------------+
  | Persist the newly created CustomerOrder entity using |
  | CustomerOrderRepository and return it in the HTTP    |
  | response.                                            |
  +------------------------------------------------------+
```

| # | Kind | What happens | Source |
|---|---|---|---|
| 1 | `validation` | Validate that address, customer, card, and items fields are non-null, otherwise throw InvalidOrderException. *(if item.address == null \|\| item.customer == null \|\| item.card == null \|\| item.items == null)* | `src/main/java/works/weave/socks/orders/controllers/OrdersController.java:49-126` |
| 2 | `external_call` | Start asynchronous HTTP GET requests to fetch Address, Customer, Card resources and a List<Item> from the provided URIs. *(if Always executed after validation passes.)* | `src/main/java/works/weave/socks/orders/services/AsyncGetService.java:32-93` |
| 3 | `transform` | Wait for itemsFuture with a timeout and compute the total amount as sum of quantity × unitPrice plus a flat shipping fee. *(if itemsFuture.get(timeout, TimeUnit.SECONDS) succeeds.)* | `src/main/java/works/weave/socks/orders/controllers/OrdersController.java:154-160` |
| 4 | `external_call` | Build a PaymentRequest from resolved resources, POST it to the payment service, and ensure the PaymentResponse is non-null and authorised, otherwise throw PaymentDeclinedException. *(if addressFuture, cardFuture, and customerFuture complete successfully within timeout.)* | `src/main/java/works/weave/socks/orders/services/AsyncGetService.java:84-92` |
| 5 | `external_call` | Trigger shipping for the order using the shipping service and construct a Shipment object (exact implementation partially truncated). *(if PaymentResponse is authorised.)* | `src/main/java/works/weave/socks/orders/controllers/OrdersController.java:49-126` |
| 6 | `db_write` | Persist the newly created CustomerOrder entity using CustomerOrderRepository and return it in the HTTP response. *(if All previous steps succeed without throwing exceptions.)* | `src/main/java/works/weave/socks/orders/repositories/CustomerOrderRepository.java:11-15` |

**Branches:** If any of address, customer, card, or items is null, the flow aborts with InvalidOrderException.; If PaymentResponse is null or not authorised, the flow aborts with PaymentDeclinedException.; If ID parsing from href fails, an IllegalStateException is thrown, aborting the flow.

**Errors:** InvalidOrderException for missing required fields; PaymentDeclinedException for payment issues; IllegalStateException for ID parsing failures; Timeouts or HTTP errors from AsyncGetService propagate as exceptions (handling not explicitly shown)

---

## Build, Tooling & CI

Every entry below is present in the repository as a real file.

| Tool | Kind | Purpose | Configured in |
|---|---|---|---|
| **Maven** | package | Build and dependency management | `pom.xml` |
| **Docker** | build | Container image build | `docker/orders/Dockerfile` |
| **GitHub Actions** | ci | Continuous integration | `.github/workflows/main.yaml` |

### Continuous integration

1 GitHub Actions workflow(s) run against this repository.

| Workflow | File |
|---|---|
| `main` | `.github/workflows/main.yaml` |

### Build and tooling entry points

| Entry point | Kind | File |
|---|---|---|
| `build.sh` | script | `scripts/build.sh` |
| `push.sh` | script | `scripts/push.sh` |

---

## Developer Guide

Where to go for the changes you will most often need to make.

| If you want to… | Start here |
|---|---|
| Change business logic | `works.weave.socks.orders.services.AsyncGetService` |
| Change how data is stored or queried | `works.weave.socks.orders.repositories.CustomerOrderRepository` |

### Entry points

| Entry point | Kind | Where |
|---|---|---|
| `works.weave.socks.orders.OrderApplication.main` | main | `src/main/java/works/weave/socks/orders/OrderApplication.java:11` |
| `build.sh` | script | `scripts/build.sh` |
| `push.sh` | script | `scripts/push.sh` |
| `GET /addresses/<addressid>` | server | `test/json-server/server.py:124` |

---

## File Reference

small repository: all 38 analysed source files are listed

| File | Role | Symbols | Key symbols | Uses | Used by | Documented above |
|---|---|---|---|---|---|---|
| `src/main/java/works/weave/socks/orders/entities/CustomerOrder.java` | source | 32 | `CustomerOrder`, `toString`, `getId`, `setId`, `getCustomerId` | `Address.java`, `Card.java`, `Customer.java` +1 | `CustomerOrderRepository.java` | — |
| `src/main/java/works/weave/socks/orders/entities/Address.java` | source | 25 | `Address`, `toString`, `equals`, `hashCode`, `getId` | — | `CustomerOrder.java`, `PaymentRequest.java`, `PaymentRequest.java` | — |
| `src/main/java/works/weave/socks/orders/entities/Customer.java` | source | 25 | `Customer`, `toString`, `equals`, `hashCode`, `getId` | — | `CustomerOrder.java`, `PaymentRequest.java`, `PaymentRequest.java` | — |
| `src/main/java/works/weave/socks/orders/entities/Item.java` | source | 23 | `Item`, `id`, `itemId`, `quantity`, `toString` | — | `OrdersController.java`, `Cart.java` | — |
| `src/main/java/works/weave/socks/orders/config/OrdersConfigurationProperties.java` | source | 21 | `OrdersConfigurationProperties`, `getPaymentUri`, `getShippingUri`, `setDomain`, `Hostname` | — | `OrdersController.java` | yes |
| `src/main/java/works/weave/socks/orders/entities/Card.java` | source | 19 | `Card`, `toString`, `equals`, `hashCode`, `getId` | — | `CustomerOrder.java`, `PaymentRequest.java`, `PaymentRequest.java` | — |
| `src/main/java/works/weave/socks/orders/entities/Cart.java` | source | 16 | `Cart`, `contents`, `add`, `remove`, `getCustomerId` | `Item.java` | — | — |
| `src/main/java/works/weave/socks/orders/resources/PaymentRequest.java` | source | 16 | `PaymentRequest`, `toString`, `getAddress`, `setAddress`, `getCard` | `Address.java`, `Card.java`, `Customer.java` | — | — |
| `src/main/java/works/weave/socks/orders/values/PaymentRequest.java` | source | 16 | `PaymentRequest`, `toString`, `getAddress`, `setAddress`, `getCard` | `Address.java`, `Card.java`, `Customer.java` | `OrdersController.java` | — |
| `src/main/java/works/weave/socks/orders/middleware/HTTPMonitoringInterceptor.java` | HTTP metrics middleware | 15 | `HTTPMonitoringInterceptor`, `preHandle`, `postHandle`, `afterCompletion`, `getMatchingURLPattern` | — | `WebMvcConfig.java` | yes |
| `test/container.py` | test | 15 | `ServiceMock`, `start_container`, `cleanup`, `__init__`, `OrdersContainerTest` | `Api.py`, `Docker.py`, `Dredd.py` | — | — |
| `src/main/java/works/weave/socks/orders/controllers/OrdersController.java` | Order orchestration REST controller | 13 | `OrdersController`, `newOrder`, `parseId`, `calculateTotal`, `PaymentDeclinedException` | `OrdersConfigurationProperties.java`, `Item.java`, `CustomerOrderRepository.java` +4 | — | yes |
| `src/main/java/works/weave/socks/orders/entities/HealthCheck.java` | source | 13 | `HealthCheck`, `toString`, `getService`, `setService`, `getStatus` | — | `HealthCheckController.java` | — |
| `src/main/java/works/weave/socks/orders/entities/Shipment.java` | source | 13 | `Shipment`, `toString`, `equals`, `hashCode`, `getId` | — | `CustomerOrder.java` | — |
| `src/main/java/works/weave/socks/orders/resources/PaymentResponse.java` | source | 10 | `PaymentResponse`, `toString`, `isAuthorised`, `setAuthorised`, `setMessage` | — | — | — |
| `src/main/java/works/weave/socks/orders/values/PaymentResponse.java` | source | 10 | `PaymentResponse`, `toString`, `isAuthorised`, `setAuthorised`, `setMessage` | — | `OrdersController.java` | — |
| `src/main/java/works/weave/socks/orders/services/AsyncGetService.java` | HTTP integration service | 9 | `AsyncGetService`, `getResource`, `getDataList`, `postResource` | `RestProxyTemplate.java` | `OrdersController.java` | yes |
| `test/json-server/server.py` | test | 9 | `get_data`, `api_cusutomers`, `api_cards`, `api_addresses`, `api_carts` | — | — | — |
| `src/main/java/works/weave/socks/orders/config/RestProxyTemplate.java` | source | 8 | `RestProxyTemplate`, `restTemplate`, `init`, `getRestTemplate`, `logger` | — | `AsyncGetService.java` | — |
| `scripts/build.sh` | script | 6 | `IMAGE`, `SCRIPT_DIR`, `DOCKER_CMD`, `CODE_DIR`, `REPO` | — | — | — |
| `scripts/push.sh` | script | 6 | `push`, `tag_and_push_all`, `DOCKER_PUSH`, `TAG`, `REPO` | — | — | — |
| `src/test/java/works/weave/socks/orders/entities/UnitPojo.java` | test | 6 | `UnitPojo`, `ensureExpectedPojoCount`, `testPojoStructureAndBehavior`, `EXPECTED_CLASS_COUNT`, `POJO_PACKAGE` | — | — | — |
| `test/test.sh` | test | 6 | `SCRIPT_DIR`, `SCRIPT_NAME`, `SSH_OPTS`, `DOCKER_CMD`, `CODE_DIR` | — | — | — |
| `test/util/Docker.py` | test | 6 | `Docker`, `kill_and_remove`, `random_container_name`, `get_container_ip`, `execute` | — | `component.py`, `container.py`, `coveralls.py` +2 | — |
| `src/main/java/works/weave/socks/orders/resources/NewOrderResource.java` | source | 5 | `NewOrderResource`, `customer`, `address`, `card`, `items` | — | `OrdersController.java` | — |
| `test/util/Dredd.py` | test | 4 | `Dredd`, `test_against_endpoint`, `image`, `container_name` | `Api.py`, `Docker.py` | `container.py` | — |
| `src/main/java/works/weave/socks/orders/config/PrometheusAutoConfiguration.java` | source | 3 | `PrometheusAutoConfiguration`, `springBootMetricsCollector`, `prometheusMetricsServletRegistrationBean` | — | — | — |
| `src/main/java/works/weave/socks/orders/config/WebMvcConfig.java` | source | 3 | `WebMvcConfig`, `httpMonitoringInterceptor`, `myMappedInterceptor` | `HTTPMonitoringInterceptor.java` | — | — |
| `src/main/java/works/weave/socks/orders/controllers/HealthCheckController.java` | source | 3 | `HealthCheckController`, `getHealth`, `mongoTemplate` | `HealthCheck.java` | — | — |
| `src/main/java/works/weave/socks/orders/OrderApplication.java` | source | 2 | `OrderApplication`, `main` | — | — | — |
| `src/main/java/works/weave/socks/orders/config/MongoConfiguration.java` | source | 2 | `MongoConfiguration`, `optionsProvider` | — | — | — |
| `src/main/java/works/weave/socks/orders/config/OrdersConfiguration.java` | source | 2 | `OrdersConfiguration`, `frameworkMesosConfigProperties` | — | — | — |
| `src/main/java/works/weave/socks/orders/repositories/CustomerOrderRepository.java` | repository | 2 | `CustomerOrderRepository`, `findByCustomerId` | `CustomerOrder.java` | `OrdersController.java` | yes |
| `test/component.py` | test | 2 | `JavaServices`, `test_maven` | `Docker.py` | — | — |
| `test/coveralls.py` | test | 2 | `JavaServices`, `test_maven` | `Docker.py` | — | — |
| `test/unit.py` | test | 2 | `JavaServices`, `test_maven` | `Docker.py` | — | — |
| `test/util/Api.py` | test | 2 | `Api`, `noResponse` | — | `container.py`, `Dredd.py` | — |
| `api-spec/hooks.js` | source | 0 | — | — | — | — |

---
