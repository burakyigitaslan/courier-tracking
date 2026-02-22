# Courier Tracking Application

A RESTful Spring Boot application that tracks courier geolocations, calculates total travel distances, and logs when couriers enter a **100-meter radius** of predefined store locations.

## Key Features

- **Real-time Tracking**: Logs courier locations via REST API.
- **Distance Calculation**: Calculates total travel distance using configurable strategies (**Haversine**, **Equirectangular**).
- **Store Entry Detection**: Automatically logs when a courier enters a store's perimeter (100m).
- **Initial Store Data**: Stores are automatically loaded into the PostgreSQL database via `StoreDataLoader` on application startup using `stores.json`.
- **Debouncing**: Prevents duplicate store entry logs within 1 minute.
- **Concurrency Control**:
    - **Pessimistic Locking**: Uses `Pessimistic Lock (WRITE)` on the `Courier` entity to serialize location updates and ensure data consistency without race conditions.
- **Reliability & Fault Tolerance**:
    - **Dead Letter Queue (DLQ)**: Failed RabbitMQ messages are routed to a DLQ for manual inspection, preventing data loss.
    - **Retry Mechanism**: Automatic retries for transient failures.

## Tech Stack

- **Java 21**, **Spring Boot 3.4.1**
- **PostgreSQL**
- **H2** (Test Database)
- **RabbitMQ**
- **Spring Data JPA**
- **MapStruct**
- **Lombok**
- **Docker**

## Project Structure

```text
src/main/java/com/casestudy/couriertracking/
├── CourierTrackingApplication.java
├── api/
│   ├── controller/      CourierController, CourierTrackingController
│   └── exception/       CourierNotFoundException, GlobalExceptionHandler
├── application/
│   ├── dto/
│   │   ├── request/     CourierRequestDTO, LocationRequestDTO
│   │   └── response/    CourierLocationResponseDTO, CourierResponseDTO
│   ├── mapper/          CourierLocationMapper, CourierMapper
│   └── service/         CourierService, CourierTrackingService
│       └── implementation/ CourierServiceImpl, CourierTrackingServiceImpl
├── domain/
│   ├── model/           Courier, CourierLocation, Store, StoreEntryLog
│   ├── repository/      CourierLocationRepository, CourierRepository, StoreEntryLogRepository
│   └── strategy/        DistanceCalculationStrategy, DistanceStrategyFactory, EquirectangularDistanceStrategy, HaversineDistanceStrategy
│       └── enumeration/ DistanceStrategyType
└── infrastructure/
    ├── config/          CourierTrackingProperties, RabbitMQConfig, StoreDataLoader
    ├── listener/        StoreProximityListener
    └── messaging/       LocationEventPublisher, LocationMessage, LocationMessageConsumer, RabbitMQLocationEventPublisherImpl
```

## Configuration

The application can be configured via `application.properties`.

| Property | Default | Description |
| :--- | :--- | :--- |
| `courier.tracking.distance-strategy` | `HAVERSINE` | Strategy for distance calculation. Options: `HAVERSINE`, `EQUIRECTANGULAR` |
| `courier.tracking.store-radius-meters` | `100.0` | Radius in meters for store entry detection |
| `courier.tracking.debounce-minutes` | `1` | Time in minutes to ignore re-entries to the same store |

## Run with Docker

To run the entire application stack (App + DB + RabbitMQ) with a single command:

```bash
docker compose up --build
```

For Windows users, you can simply run:
```cmd
run.cmd
```

For macOS/Linux users:
```bash
chmod +x run.sh
./run.sh
```

The application will be available at **http://localhost:8080**.

## API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| **POST** | `/api/v1/courier` | Create a new courier |
| **GET** | `/api/v1/courier` | Get all couriers |
| **GET** | `/api/v1/courier/{courierId}` | Get a courier by ID |
| **POST** | `/api/v1/courier-tracking/location` | Log a courier's location |
| **GET** | `/api/v1/courier-tracking/travel-distance/{courierId}` | Get total travel distance for a courier |

### 1. Create a Courier
**POST** `/api/v1/courier`
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+905551234567"
}
```

### 2. Log Courier Location
**POST** `/api/v1/courier-tracking/location`
```json
{
  "time": "2024-01-01T10:00:00",
  "courierId": 1,
  "lat": 40.9923,
  "lng": 29.1244
}
```

### 3. Get Total Travel Distance
**GET** `/api/v1/courier-tracking/travel-distance/{courierId}`

Returns the total distance in **meters** (e.g., `5732.0`).

## Tests

Tests use **H2 in-memory** database and **Mockito** for comprehensive coverage.
The test suite spans across unit and integration tests, explicitly testing edge-cases such as:
- **High Concurrency Location Updates** (Verifying Pessimistic Locking)
- **Misordered Location Handling** (Handling delayed GPS pings)
- **Idempotency** (Skipping duplicate events from RabbitMQ)
- **Store Proximity Debounce Logic**

## API Documentation & Interfaces

The application provides interactive API documentation via Swagger UI, and RabbitMQ provides a management interface.

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **RabbitMQ Management UI**: [http://localhost:15672](http://localhost:15672) *(Credentials: `guest` / `guest`)*
