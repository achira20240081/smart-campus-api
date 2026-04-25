
https://drive.google.com/file/d/1CKA_-ujpihjidW04I7E7aIOVGbgDyqHa/view?usp=sharing



# Smart Campus Sensor API

> A production-quality RESTful API built with **Java + JAX-RS (Jersey 3)** and an embedded **Grizzly** HTTP server.  
> No Spring Boot. No database. Pure Jakarta EE REST.

---

## Table of Contents

1. [API Overview](#api-overview)
2. [Architecture](#architecture)
3. [Project Structure](#project-structure)
4. [How to Build & Run](#how-to-build--run)
5. [Endpoint Documentation](#endpoint-documentation)
6. [Example curl Commands](#example-curl-commands)
7. [Error Handling](#error-handling)
8. [Design Decisions](#design-decisions)

---

## API Overview

The **Smart Campus Sensor API** manages IoT sensors deployed across university rooms.  
It exposes a RESTful interface for:

| Concern | Description |
|---|---|
| **Rooms** | Physical spaces on campus (Library, Labs, Lecture Halls) |
| **Sensors** | IoT devices (Temperature, CO2, Occupancy) installed in rooms |
| **Sensor Readings** | Time-series measurements recorded by each sensor |

**Base URL:** `http://localhost:8080/api/v1`

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                  HTTP Client (curl / Postman)            │
└──────────────────────────┬──────────────────────────────┘
                           │ HTTP
┌──────────────────────────▼──────────────────────────────┐
│         Grizzly Embedded HTTP Server (port 8080)         │
├─────────────────────────────────────────────────────────┤
│                   Jersey JAX-RS Runtime                  │
│  ┌───────────────────────────────────────────────────┐  │
│  │  ApiLoggingFilter  (ContainerRequest/ResponseFilter│  │
│  └───────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐   │
│  │   Exception Mappers  (404 / 409 / 422 / 403 / 500│   │
│  └──────────────────────────────────────────────────┘   │
│  ┌─────────────────┐ ┌───────────────┐ ┌────────────┐   │
│  │  DiscoveryRes.  │ │  RoomResource │ │SensorResour│   │
│  │  GET /api/v1    │ │  /rooms       │ │/sensors    │   │
│  └─────────────────┘ └───────────────┘ └─────┬──────┘   │
│                                              │ sub-res   │
│                                     ┌────────▼────────┐  │
│                                     │SensorReadingRes.│  │
│                                     │/sensors/{id}/   │  │
│                                     │  readings       │  │
│                                     └─────────────────┘  │
├─────────────────────────────────────────────────────────┤
│              DataStore (Singleton / ConcurrentHashMap)   │
│    rooms: Map<String,Room>  sensors: Map<String,Sensor>  │
│    sensorReadings: Map<String, List<SensorReading>>      │
└─────────────────────────────────────────────────────────┘
```

### Key Technology Choices

| Technology | Role | Why |
|---|---|---|
| **JAX-RS 3.1 (Jersey)** | REST framework | Standard Jakarta EE — no vendor lock-in |
| **Grizzly HTTP Server** | Embedded server | Lightweight, no app server needed |
| **Jackson** | JSON serialisation | Industry standard, auto-wired by Jersey |
| **ConcurrentHashMap** | In-memory storage | Thread-safe without explicit locks |
| **HK2** | Dependency Injection | Required for @Context injection in Jersey |

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── com/smartcampus/
    │           ├── Main.java                          ← Grizzly server entry point
    │           ├── app/
    │           │   └── ApplicationConfig.java         ← @ApplicationPath("/api/v1")
    │           ├── model/
    │           │   ├── Room.java                      ← Room entity
    │           │   ├── Sensor.java                    ← Sensor entity
    │           │   ├── SensorReading.java             ← Reading entity
    │           │   └── DataStore.java                 ← Singleton in-memory store
    │           ├── resource/
    │           │   ├── DiscoveryResource.java         ← GET /api/v1
    │           │   ├── RoomResource.java              ← /rooms CRUD
    │           │   ├── SensorResource.java            ← /sensors + sub-resource locator
    │           │   └── SensorReadingResource.java     ← /sensors/{id}/readings
    │           ├── exception/
    │           │   ├── ErrorResponse.java             ← Standard JSON error body
    │           │   ├── ResourceNotFoundException.java       → 404
    │           │   ├── RoomNotEmptyException.java           → 409
    │           │   ├── LinkedResourceNotFoundException.java → 422
    │           │   ├── SensorUnavailableException.java      → 403
    │           │   ├── ResourceNotFoundExceptionMapper.java
    │           │   ├── RoomNotEmptyExceptionMapper.java
    │           │   ├── LinkedResourceNotFoundExceptionMapper.java
    │           │   ├── SensorUnavailableExceptionMapper.java
    │           │   └── GlobalExceptionMapper.java           → 500
    │           └── filter/
    │               └── ApiLoggingFilter.java          ← Request + Response logging
    └── test/
        └── java/
            └── com/smartcampus/
                └── DataStoreTest.java                 ← JUnit 5 unit tests
```

---

## How to Build & Run

### Prerequisites

| Tool | Minimum Version |
|---|---|
| Java JDK | 17 |
| Apache Maven | 3.8 |

### 1. Build the Fat JAR

```bash
mvn clean package -q
```

This produces `target/smart-campus-api-1.0.0.jar` — a single executable JAR containing all dependencies.

### 2. Run the Server

```bash
java -jar target/smart-campus-api-1.0.0.jar
```

You should see:

```
INFO: Smart Campus Sensor API started successfully
INFO: Base URI : http://localhost:8080/api/v1/
INFO: Rooms    : GET http://localhost:8080/api/v1/rooms
INFO: Sensors  : GET http://localhost:8080/api/v1/sensors
INFO: Press CTRL+C to stop the server.
```

### 3. Run Tests

```bash
mvn test
```

---

## Endpoint Documentation

### Discovery

| Method | Path | Description | Response |
|---|---|---|---|
| `GET` | `/api/v1` | API metadata + HATEOAS links | `200 OK` |

**Response body:**
```json
{
  "version": "1.0.0",
  "description": "Smart Campus Sensor Management API",
  "contact": "admin@smartcampus.ac.uk",
  "status": "UP",
  "links": {
    "self": "http://localhost:8080/api/v1/",
    "rooms": "http://localhost:8080/api/v1/rooms",
    "sensors": "http://localhost:8080/api/v1/sensors"
  }
}
```

---

### Rooms — `/api/v1/rooms`

| Method | Path | Description | Success | Error |
|---|---|---|---|---|
| `GET` | `/rooms` | List all rooms | `200 OK` | — |
| `POST` | `/rooms` | Create a room | `201 Created` | `400`, `409` |
| `GET` | `/rooms/{id}` | Get room by ID | `200 OK` | `404` |
| `DELETE` | `/rooms/{id}` | Delete a room | `204 No Content` | `404`, `409` |

**Room object:**
```json
{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "capacity": 30,
  "sensorIds": ["TEMP-001"]
}
```

---

### Sensors — `/api/v1/sensors`

| Method | Path | Description | Success | Error |
|---|---|---|---|---|
| `POST` | `/sensors` | Create a sensor | `201 Created` | `400`, `409`, `422` |
| `GET` | `/sensors` | List all sensors | `200 OK` | — |
| `GET` | `/sensors?type=CO2` | Filter by type | `200 OK` | — |
| `GET` | `/sensors/{id}` | Get sensor by ID | `200 OK` | `404` |
| `PUT` | `/sensors/{id}` | Update sensor (type/status/value) | `200 OK` | `400`, `404` |
| `DELETE` | `/sensors/{id}` | Delete sensor + readings | `204 No Content` | `404` |

**Sensor object:**
```json
{
  "id": "TEMP-001",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 21.5,
  "roomId": "LIB-301"
}
```

**Status values:** `ACTIVE` | `MAINTENANCE` | `OFFLINE`

**Sensor response (with HATEOAS _links):**
```json
{
  "id": "TEMP-001",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 21.5,
  "roomId": "LIB-301",
  "_links": {
    "self": "http://localhost:8080/api/v1/sensors/TEMP-001",
    "readings": "http://localhost:8080/api/v1/sensors/TEMP-001/readings",
    "room": "http://localhost:8080/api/v1/rooms/LIB-301"
  }
}
```

**PUT body — send only the fields you want to change:**
```json
{ "status": "MAINTENANCE" }
```

---

### Sensor Readings — `/api/v1/sensors/{id}/readings`

| Method | Path | Description | Success | Error |
|---|---|---|---|---|
| `GET` | `/sensors/{id}/readings` | Get all readings | `200 OK` | `404` |
| `POST` | `/sensors/{id}/readings` | Add new reading | `201 Created` | `403`, `404` |

**SensorReading object:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1698825600000,
  "value": 22.5
}
```

> **Note:** `id` and `timestamp` are auto-assigned by the server if omitted in the request body.

---

## Example curl Commands

### 1. Discover the API

```bash
curl -X GET http://localhost:8080/api/v1
```

### 2. List all rooms

```bash
curl -X GET http://localhost:8080/api/v1/rooms
```

### 3. Get a specific room

```bash
curl -X GET http://localhost:8080/api/v1/rooms/LIB-301
```

### 4. Create a new room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id":"ENG-201","name":"Engineering Lab","capacity":40}'
```

### 5. Try to delete a room that has sensors (triggers 409 Conflict)

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

**Expected response (HTTP 409):**
```json
{
  "timestamp": "2024-11-01T10:00:00Z",
  "status": 409,
  "error": "CONFLICT",
  "message": "Room 'LIB-301' still has sensors assigned to it...",
  "detail": "Remove all sensors from the room before deleting it."
}
```

### 6. List all sensors

```bash
curl -X GET http://localhost:8080/api/v1/sensors
```

### 7. Filter sensors by type

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 8. Create a new sensor (validates roomId)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"HUMID-004","type":"Humidity","status":"ACTIVE","currentValue":55.0,"roomId":"LIB-301"}'
```

### 9. Try to create a sensor with a non-existent roomId (triggers 422)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"GHOST-001","type":"CO2","roomId":"NONEXISTENT-999"}'
```

**Expected response (HTTP 422):**
```json
{
  "status": 422,
  "error": "UNPROCESSABLE_ENTITY",
  "message": "Room with id 'NONEXISTENT-999' was not found."
}
```

### 10. Get all readings for a sensor

```bash
curl -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

### 11. Add a new reading to an ACTIVE sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value": 23.1}'
```

### 12. Try to add a reading to a MAINTENANCE sensor (triggers 403)

```bash
curl -X POST http://localhost:8080/api/v1/sensors/OCC-003/readings \
     -H "Content-Type: application/json" \
     -d '{"value": 15}'
```

**Expected response (HTTP 403):**
```json
{
  "status": 403,
  "error": "FORBIDDEN",
  "message": "Sensor 'OCC-003' is currently in 'MAINTENANCE' state and cannot accept new readings."
}
```

---

## Error Handling

All error responses use a consistent JSON schema:

```json
{
  "timestamp": "2024-11-01T10:23:45Z",
  "status": 409,
  "error": "CONFLICT",
  "message": "Human-readable description of the problem.",
  "detail": "Corrective action hint for the API consumer."
}
```

| Exception Class | HTTP Status | Trigger |
|---|---|---|
| `ResourceNotFoundException` | `404 Not Found` | URL path ID doesn't exist |
| `RoomNotEmptyException` | `409 Conflict` | DELETE room with sensors |
| `LinkedResourceNotFoundException` | `422 Unprocessable Entity` | Payload references non-existent room |
| `SensorUnavailableException` | `403 Forbidden` | POST reading to non-ACTIVE sensor |
| `GlobalExceptionMapper` | `500 Internal Server Error` | Any unhandled exception |

---

## Design Decisions

### Why ConcurrentHashMap over HashMap?
A REST API handles concurrent requests. Plain `HashMap` is not thread-safe — simultaneous writes corrupt the internal structure. `ConcurrentHashMap` uses segment-level locking internally, preventing race conditions with minimal performance overhead.

### Why a Singleton DataStore?
JAX-RS resource classes are **request-scoped by default** — a new instance is created per request. Instance fields would be lost between requests. The singleton DataStore lives for the entire JVM lifetime, shared safely across all resource instances.

### Why 422 instead of 404 for invalid roomId?
`404 Not Found` means the **URL** was not found. When `POST /sensors` arrives with an invalid `roomId` in the body, the URL `/sensors` is perfectly valid. The problem is inside the payload — a reference to a non-existent entity. `422 Unprocessable Entity` is semantically correct: the server understood the request but cannot process the instructions because a referenced resource doesn't exist.

### Why filters for logging?
Placing `Logger.info()` in every resource method violates the DRY principle, creates inconsistencies, and mixes infrastructure with business logic. A `@Provider` filter applies automatically to every request/response with zero modification to resource classes — following the Separation of Concerns principle.
