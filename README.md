
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



## Q&A: Service Architecture & Setup

### Question 1.1
**In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.**

Resource classes in this Smart Campus Sensor API are lifecycle managed considerate of default behaviour of JAX-RS, which is request-scoped. This implies that a new object of each resource type will be created whenever an incoming HTTP request comes and will be destroyed once the response is sent. Resource classes are not singletons in the runtime. 

This design will have every request running concurrently without the unwanted exchange of instance variables between parallel requests. Consequently, resource classes themselves will be thread-safe because their state is not shared. 

This does however come with a limitation: any information stored in a resource instance is temporary and will be lost after the request is finished. To overcome this, the system encounters a DataStore singleton which is declared in the project architecture. This DataStore remains constant to the application lifecycle, and is replicated among all resource instances. 

As this shared DataStore may be accessed by a number of requests at the same time, thread safety is vital. A normal HashMap would not work since the concurrent access may introduce race conditions and may corrupt data. As such, the system relies on ConcurrentHashMap, which offers thread safety and permits many threads to read and write simultaneously without an explicit synchronization. 

This architectural choice guarantees the data persistence as well as the safe multitasking access, which is the key to the multi-user REST API environment. 

Although the default lifecycle of JAX-RS is a request-scoped lifecycle, a singleton lifecycle can be used with the annotation of singleton. When this happens only one instance of the resource class is shared among all requests. Nonetheless, this demands a lot of attention on the shared state to prevent concurrency problems. 

Also, concurrentHashMap is only thread-safe in an individual operation, whereas more compound operations (e.g. checkthenact, i.e. checking whether a key exists and then inserting a value) are not. To illustrate, it is possible that doing containsKey then put will still cause a race condition with a high degree of concurrency. Although this restriction is tolerable at this scale of coursework, atomic methods, like putIfAbsent or explicit synchronization, would be needed in a production-grade system.

### Question 1.2
**Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**

Hypermedia (or HATEOAS Hypermedia as the Engine of Application State) is a characteristic of an advanced design of RESTful API. The implementation of hypermedia in this Smart Campus Sensor API is with the Discovery endpoint on /api/v1, which calls to return structured links to the main resources of rooms and sensors. 

The API also has links to navigation in its JSON responses rather than using static documentation. This permits the client applications to dynamically find the available operations and resources by traversing the links given. 

The key advantage of this method is that it renders the API self-descriptive. All a client has to do is know the base URL, and all the other endpoints can be discovered on-demand. This lessens reliance on external documentation and lessens integration. 

Besides, hypermedia encourages laxity in the bond between the client and the server. Given that the endpoint URLs are not hardcoded by the clients, any alterations in the API structure (version changes or change in paths) will not cause existing clients to fail. Navigation is done through links that are dynamically generated by the server. 

In general, hypermedia usage enhances flexibility, maintainability and usability, which makes the API more resilient and user-friendly. 

Advanced RESTful APIs have a feature called hypermedia (HATEOAS) whereby clients can navigate the system by using links in the responses of the requests. 

HATEOAS is applied in two ways in this API. To begin with, the Discovery endpoint (GET /api/v1) contains top-level links to resources, like rooms and sensors. Second, every resource response has a links section with links to related endpoints, e.g.: 

```json
"_links": { 
  "self": "/api/v1/sensors/TEMP-001", 
  "readings": "/api/v1/sensors/TEMP-001/readings", 
  "room": "/api/v1/rooms/LIB-301" 
} 
```

It enables clients to explore and navigate the API dynamically without the need to hardcode URLs to make them less reliant on documentation and enable better flexibility. 

In general, HATEOAS causes the API to be self descriptive, loosely coupled, and easier to maintain.

---

## Q&A: Part 2: Room Management

### Question 2.1
**When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing**

In the design of the API to respond with a list of rooms, there are two possible options: to respond with room IDs or respond with entire room objects. 

The size of the response has been minimized by returning only IDs which might appear advantageous at first face value to network efficiency. Nevertheless, this is likely to cause a higher network consumption. Clients will usually demand more information like room name and capacity and this means that they will have to make individual requests per room. This leads to the famous N+1 request problem, where one request causes another request, and so on. 

Returning full room objects, on the other hand, is a response that contains all the needed information. This will slightly increase the size of the payload, but drastically decrease the amount of API calls needed, leading to better performance. 

On the client side, this means more complexity since the client has to handle numerous asynchronous requests and combine the results, since only the IDs are returned. Conversely, full object returns make client logic simpler and enable data to be shown instantly, leading to an improved user experience. 

Hence, in this implementation, it is more effective and realistic to revert full room objects.

### Question 2.2
**Is the DELETE operation idempotent in your implementation ? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**

DELETE is an idempotent operation with this Smart Campus Sensor API. Operation idempency is when a series of requests leading to the same end-state on a server are considered idempent. 

As a DELETE request is submitted to delete a room, the system first verifies that the room is in the DataStore. In case the room is present and is not equipped with any related sensors, the room is removed successfully, and the API sends an HTTP 204 No Content reply. 

In case the same DELETE request is sent again, there will be no room in the system anymore. The API in this scenario gives an HTTP 404 Not Found response. Even though the answer is different, the end result of server is the same since the room is already obliterated. 

Also, in case the sensors are still assigned to the room, the system will not allow deletion, it will provide the HTTP 409 Conflict response. Further repetition of the same request in this state yields the same response without changing the server state. 

Thus, the DELETE operation meets the idempotency definition, with repeated request not resulting in any more side effects. 

The final state of the server determines HTTP idempotency, as opposed to the response code. Thus, it is still valid idempotent behaviour to send back 204 on the first request and 404 on the subsequent requests.

---

## Q&A: Part 3: Sensor Operations & Linking

### Question 3.1
**We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?**

The Smart Campus Sensor API specifically consumes Media type.APPLICATION_JSON) annotation to make all the incoming requests of the type of JSON. 

Sending a request with a different content type, e.g., text/plain or application/xml, the JAX-RS runtime identifies discrepancy between the Content-Type header of the request and the format assumed in the annotation. 

Consequently, the request is denied prior to accessing the resource method. The server will automatically reply with an HTTP 415 Unsupported Media Type status code. 

The behaviour is used to make sure that only supported data types are handled, avoiding any error when deserializing and mapping request data to Java objects. It is also an improved way of security, since all the data that comes in is in a predictable and verified pattern. 

Also, the annotation @Produces is similar to the annotation @Consumes, but it allows the server to decide the response format based on the Accept header of the client. This will guarantee appropriate content negotiation between a server and a client.

### Question 3.2
**You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?**

This API uses @QueryParam to implement filtering on collections, where the client can use query parameters to filter collections, e.g., /api/v1/sensors?type=CO2. 

Another design would be to incorporate the value of the filter in the URL path, e.g. /api/v1/sensors/type/CO2. Nonetheless, this method considers type as a distinct resource, which is not true as it is only a property of a sensor and not a stand-alone resource. 

It would be more fitting to use query parameters since it explicitly shows that the client is undertaking a filtering operation on a collection. It is also more flexible, with multiple filters being easily combined, such as:/api/v1/sensors?type=CO2&status=ACTIVE. 

Moreover, query parameters are optional and hence the same endpoint may be used to support the filtered and unfiltered requests. This leads to a more scalable, cleaner and more RESTful API design.

---

## Q&A: Part 4: Deep Nesting with Sub-Resources

### Question 4.1
**Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive con troller class?**

This API utilizes the Sub-Resource Locator pattern in order to deal with nested resources like sensor readings within sensors. 

The API does not embed all logic in one resource class instead of assigning sensor reading operations to a specific SensorReadingResource class. The SensorResource class has a locator method which returns an instance of this sub-resource. 

Separation of concerns is one of the benefits of this approach. The code is more readable, maintainable, and debuggable since each resource class has a defined portion of the system to which it is dedicated. 

Also, this pattern makes it impossible to create huge and complicated controller classes. When all the nested paths are managed within one class, then it will be hard to manage as the API expands. 

The locator approach also makes sure that context, e.g. sensorId, has been checked prior to control transfer to the sub-resource. This saves unnecessary validation code and enhances performance. 

In general, the Sub-Resource Locator pattern increases the modularity, scalability, and maintainability of the API. 

One important technical feature of a sub-resource locator is that it lacks an annotation with an HTTP method, e.g., @GET or @POST. Rather, it is only defined with a @Path annotation. JAX-RS runtime invokes this method in order to get a sub-resource instance, and execute the HTTP method on the returned object. This allows runtime polymorphism and provides dynamic request processing. 

Also, to make the sub-resource testable, it is better to separate it into a separate class. Unit testing is more efficient since the SensorReadingResource can be tested without the entire HTTP server environment.

---

## Q&A: Part 5: Advanced Error Handling, Exception Mapping & Logging

### Question 5.2
**Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**

When the problem in question is in the request body and not the endpoint, HTTP 422 Unprocessable Entity is more semantically appropriate than HTTP 404 Not Found. 

In this API, in case a client tries to create a sensor with a roomId that does not exist, the endpoint will be /api/v1/sensors and the request will be in the right format. But the request cannot be processed since it contains the name of a resource that does not exist. 

An HTTP 404 would be a false indication that the endpoint is not present. Conversely, HTTP 422 distinctly states that the server has received the request but is unable to handle it as the data is not valid. This difference aids the client developers to comprehend the kind of the error and take the corrective action accordingly.

### Question 5.4
**From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**

It is highly insecure to expose internal Java stack traces to API consumers. Stack traces include information regarding the internal set up of the application such as the name of the classes, package structures, file paths and structures used in the application. 

This information can be used by an attacker to study the system architecture and determine the possible vulnerabilities. As an example, information about the precise libraries and versions in use can enable an attacker to take advantage of the known security vulnerabilities. 

Stack traces can also disclose sensitive data like server settings or database specifics, exposing the target of attacks. 

The API addresses these risks by employing a GlobalExceptionMapper to deal with unforeseen errors. This makes sure that the information about any error is recorded internally to facilitate debugging but only generic and safe replies (like HTTP 500 Internal Server Error) is sent to the clients. 

Internal file paths may also be revealed by stack traces, and these may provide an insight into directory structures. This data can help attackers to conduct path traversal or target attacks on the system.

### Question 5.5
**Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single re source method?**

This API uses JAX-RS filters to implement logging, namely the Api Logging Filter. This is better, instead of manually entering logging statements in every resource method. 

The filters also enable centralized logging so that all the requests sent to the server and the answers returned to the server are always logged. This minimizes duplication of code and adheres to DRY (Don’t Repeat Yourself) principle. 

This allows resource classes to be clean and concentrate on their original tasks by decoupling logging and business logic. This enhances readability, maintainability and testability of the code. 

Also, all modifications to logging behaviour may be done in one place, without changing a number of resource classes. This increases the flexibility of the system and also makes it easier to maintain. 

JAX-RS filters also allow ordering with annotations like @Priority so that developers can control the order of execution of a series of filters. Moreover, filters can be automatically applied to all existing and future endpoints, and no resource classes have to be modified. This guarantees that there is uniform behaviour in the whole API and makes long term maintenance easier. 

The API will not allow deleting a room when there are still sensors assigned to it, since this will result in orphaned data and inconsistency. 

When this kind of a DELETE request is executed, an exception of type Room Not Empty Exception is thrown which is processed by an exception mapper that returns an HTTP 409 Conflict response. 

The reply contains a straight forward JSON message describing the problem and telling the client to remove sensors prior to deleting the room. 

This makes sure that business rules are implemented and meaningful feedback is given to the client.
