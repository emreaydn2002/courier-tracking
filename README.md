# Courier Tracking Case

A simple RESTful web application written in Java & Spring Boot that tracks courier
locations, calculates the total travel distance of each courier, and logs when
couriers enter the 100 m radius of Migros stores.

Store locations are provided in a JSON file: `src/main/resources/stores.json`.

---

## 1. Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 4.x (Spring MVC)
- **Build Tool:** Maven (with `mvnw` wrapper)
- **JSON:** Jackson
- **Validation:** Jakarta Bean Validation (Hibernate Validator)
- **Docs:** springdoc-openapi + Swagger UI
- **Testing:** Spring Boot Test, JUnit 5, AssertJ

---

## 2. Project Structure (High Level)

Main packages under `org.courier.couriertracking`:

- `controller`
    - `LocationController` – REST API endpoints.
- `domain`
    - `Store` – Migros store (name, latitude, longitude).
    - `CourierTrack` – per-courier state (last location, total distance).
    - `StoreEntranceLog` – log entry for store entrances.
- `dto`
    - `LocationUpdateRequest` – incoming courier location payload.
- `repository`
    - `StoreRepository` – loads stores from `stores.json` into memory.
    - `CourierTrackRepository` – in-memory storage of `CourierTrack`.
    - `StoreEntranceRepository` – in-memory log of store entrances
      and last entrance times (used for 1-minute re-entry rule).
- `service`
    - `LocationService` – orchestrates location handling, exposes
      `getTotalTravelDistance(courierId)` and entrance queries.
- `distance`
    - `DistanceCalculator` – strategy interface for distance calculation.
    - `HaversineDistanceCalculator` – implementation using the Haversine formula.
- `event`
    - `LocationUpdatedEvent` – Spring application event.
    - `LocationDistanceEventHandler` – updates courier total distance.
    - `StoreEntranceEventHandler` – logs store entrances.

Resources:

- `stores.json` – list of Migros store coordinates.

---

## 3. How to Build, Run & Test

### 3.1 Prerequisites

- Java 17 installed.

### 3.2 Build

From the project root:

~~~bash
./mvnw clean package
~~~

(Windows: `mvnw.cmd clean package`)

### 3.3 Run

~~~bash
./mvnw spring-boot:run
~~~

The application starts on:

~~~text
http://localhost:8080
~~~

You should see logs similar to:

~~~text
Tomcat started on port 8080
Loaded 5 stores from stores.json
Started CourierTrackingApplication ...
~~~

### 3.4 Run Tests

~~~bash
./mvnw test
~~~

Main test class:

- `LocationServiceTest`
    - Sends `LocationUpdateRequest` DTOs directly to `LocationService`.
    - Verifies that:
        - total travel distance for a courier is accumulated and greater than zero,
        - store entrances are logged,
        - re-entries to the same store within **1 minute** are **not** counted as new entrances.

### 3.5 Run via Script (`run.sh`)

For convenience, there is also an executable script in the project root:

- `run.sh`  (macOS / Linux)

Usage:

~~~bash
chmod +x run.sh   # first time only
./run.sh
~~~

What the script does:

1. Runs tests (`./mvnw clean test`).
2. Prints sample `curl` commands for trying the APIs manually.
3. Starts the application in the foreground on `http://localhost:8080`.

> The script automatically checks if `$HOME/.m2/settings-personal.xml`
> exists; if it does, it uses that Maven settings file.  
> If not, it falls back to Maven’s default configuration, so it also works
> on machines that don’t have this custom settings file.

---

## 4. API Documentation (Swagger)

Swagger UI is available at:

~~~text
http://localhost:8080/swagger-ui/index.html
~~~

From there you can:

- Inspect all endpoints and their models
- Try requests directly from the browser

(OpenAPI JSON is available at `/v3/api-docs`.)

---

## 5. API Endpoints

### 5.1 Health Check

`GET /ping`  
Returns a simple `"pong"` string. Useful to verify the app is running.

---

### 5.2 Send Courier Location

`POST /locations`

Consumes a `LocationUpdateRequest`:

~~~json
{
  "time": "2025-12-07T11:30:00Z",
  "courierId": "c1",
  "lat": 40.99235,
  "lng": 29.12440
}
~~~

- `time` – ISO-8601 timestamp (`Instant`), used for the 1-minute re-entry rule
- `courierId` – unique identifier of the courier
- `lat`, `lng` – latitude and longitude in degrees

Example `curl`:

~~~bash
curl -X POST http://localhost:8080/locations \
  -H "Content-Type: application/json" \
  -d '{
        "time": "2025-12-07T11:30:00Z",
        "courierId": "c1",
        "lat": 40.99235,
        "lng": 29.12440
      }'
~~~

**Behavior**

For each location update:

1. A `LocationUpdatedEvent` is published.
2. `LocationDistanceEventHandler`:
    - Fetches or creates a `CourierTrack` for the courier.
    - Calculates the distance (meters) from the previous location using
      `DistanceCalculator` (Haversine).
    - Adds this value to the courier’s `totalDistanceMeters`.
3. `StoreEntranceEventHandler`:
    - Iterates over all stores from `StoreRepository`.
    - Calculates the distance between courier and each store.
    - If the distance ≤ **100 meters**, an entrance is logged in
      `StoreEntranceRepository`, **unless**:
        - The same courier has already entered the same store within the last **1 minute**
          (re-entries within 1 minute are ignored).

Validation errors (missing fields, invalid coordinates, etc.) result in `400 Bad Request`.

---

### 5.3 Get Total Travel Distance of a Courier

`GET /couriers/{courierId}/distance`

Returns the total distance (in meters) traveled by the courier, based on all
received location updates.

Example:

~~~bash
curl http://localhost:8080/couriers/c1/distance
~~~

Example response:

~~~json
1234.5678
~~~

Internally it calls:

~~~java
double getTotalTravelDistance(String courierId);
~~~

on `LocationService`.

---

### 5.4 Get Store Entrances of a Courier

`GET /couriers/{courierId}/entrances`

Returns all logged store entrances for the given courier.

Example:

~~~bash
curl http://localhost:8080/couriers/c1/entrances
~~~

Example response:

~~~json
[
  {
    "courierId": "c1",
    "storeName": "Ataşehir MMM Migros",
    "entranceTime": "2025-12-07T11:30:00Z",
    "distanceMeters": 2.88
  },
  {
    "courierId": "c1",
    "storeName": "Ataşehir MMM Migros",
    "entranceTime": "2025-12-07T11:31:40Z",
    "distanceMeters": 2.94
  }
]
~~~

Each item represents a **new** entrance into the store’s 100 m radius, with the
1-minute re-entry rule applied.

---

## 6. Distance Calculation

The application uses the **Haversine formula** to compute great-circle distances
between two GPS coordinates.

Strategy interface:

~~~java
public interface DistanceCalculator {
    double calculateDistanceMeters(double lat1, double lng1,
                                   double lat2, double lng2);
}
~~~

Default implementation:

~~~java
@Component
public class HaversineDistanceCalculator implements DistanceCalculator {
    // ...
}
~~~

This implementation is injected wherever distance is needed (distance handler and
store entrance handler).

---

## 7. Design Patterns

### 7.1 Strategy Pattern

**Why?**  
To allow different distance calculation algorithms without changing the rest of the code.

**How?**

- `DistanceCalculator` – strategy interface
- `HaversineDistanceCalculator` – concrete strategy (Haversine)
- Used by:
    - `LocationDistanceEventHandler` (courier total distance)
    - `StoreEntranceEventHandler` (distance between courier and store)

---

### 7.2 Observer / Event-Driven Pattern

**Why?**  
To decouple “accept location” from “business logic” (distance and store entrance rules).

**How?**

- `LocationService` publishes `LocationUpdatedEvent` for each `/locations` request.
- `LocationDistanceEventHandler` listens and updates courier total distance.
- `StoreEntranceEventHandler` listens and logs store entrances.

New behaviors can be added by implementing additional event listeners without changing
the controller or service.

---

### 7.3 Repository Pattern (In-Memory)

**Why?**  
To abstract how data is stored, making it easy to replace in-memory storage with a DB later.

**How?**

- `StoreRepository` – loads stores from `stores.json`.
- `CourierTrackRepository` – keeps `CourierTrack` objects in memory.
- `StoreEntranceRepository` – keeps `StoreEntranceLog` instances and last entrance times.

Controllers and services work with repositories, not with storage details.

---

## 8. Possible Extensions

- Persist data in a relational database instead of in-memory maps.
- Add pagination and filtering (`from`, `to`) to `/couriers/{id}/entrances`.
- Expose an endpoint listing all stores.
- Add authentication / API key support.
- Containerize with Docker for easier deployment.
