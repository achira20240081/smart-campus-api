package com.smartcampus.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe, in-memory data store — the "database" for this API.
 *
 * -----------------------------------------------------------------------
 * WHY A SINGLETON PATTERN?
 * -----------------------------------------------------------------------
 * JAX-RS resource classes are, by default, request-scoped: the JAX-RS
 * runtime creates a NEW instance for every incoming HTTP request. This means
 * any instance fields on a resource class would be discarded after each
 * request — making it impossible to share state between requests.
 *
 * The DataStore singleton solves this: it is created once when the JVM
 * loads the class and shared by every resource class instance for the
 * lifetime of the application.
 *
 * -----------------------------------------------------------------------
 * WHY ConcurrentHashMap INSTEAD OF HashMap?
 * -----------------------------------------------------------------------
 * Plain HashMap is NOT thread-safe. A REST API can receive many concurrent
 * requests. Two simultaneous POST /rooms requests could both call
 * rooms.put() at exactly the same moment, corrupting the internal hash
 * table structure. ConcurrentHashMap uses segment-level (bucket-level)
 * locking internally, so:
 *   – Concurrent reads never block each other.
 *   – Concurrent writes on *different* keys do not block each other.
 *   – Writes on the *same* key are serialised correctly.
 * This gives thread safety without explicit synchronised blocks.
 *
 * -----------------------------------------------------------------------
 * LIFECYCLE NOTE
 * -----------------------------------------------------------------------
 * All data is lost when the server restarts because it lives in JVM heap
 * memory. For persistence, the store layer would be replaced with a
 * repository implementation backed by a relational or document database —
 * but the resource classes would remain unchanged (Dependency Inversion).
 */
public class DataStore {

    // Eager initialisation — instance is created when class is loaded.
    // Safe for single-class-loader environments (standard JVM).
    private static final DataStore INSTANCE = new DataStore();

    // Primary collections — keyed by entity ID for O(1) lookup
    private final Map<String, Room>          rooms          = new ConcurrentHashMap<>();
    private final Map<String, Sensor>        sensors        = new ConcurrentHashMap<>();
    // Key: sensorId → ordered list of readings for that sensor
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {
        seedData();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // -----------------------------------------------------------------------
    // Room CRUD helpers
    // -----------------------------------------------------------------------

    public Map<String, Room> getRooms()          { return rooms; }
    public Room  getRoom(String id)              { return rooms.get(id); }
    public void  saveRoom(Room room)             { rooms.put(room.getId(), room); }
    public boolean deleteRoom(String id)         { return rooms.remove(id) != null; }
    public boolean roomExists(String id)         { return rooms.containsKey(id); }

    // -----------------------------------------------------------------------
    // Sensor CRUD helpers
    // -----------------------------------------------------------------------

    public Map<String, Sensor> getSensors()      { return sensors; }
    public Sensor getSensor(String id)           { return sensors.get(id); }

    /** Persist a sensor and initialise its reading list. */
    public void saveSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        // Ensure the reading list is always initialised on first save
        sensorReadings.putIfAbsent(sensor.getId(), new ArrayList<>());
    }

    /** Deletes a sensor and removes all its historical readings. */
    public boolean deleteSensor(String id) {
        sensorReadings.remove(id);
        return sensors.remove(id) != null;
    }

    public boolean sensorExists(String id)       { return sensors.containsKey(id); }

    // -----------------------------------------------------------------------
    // Sensor Reading helpers
    // -----------------------------------------------------------------------

    public List<SensorReading> getReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        // computeIfAbsent is atomic — safe under concurrent access
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }

    // -----------------------------------------------------------------------
    // Seed data — pre-populates the store for demo/testing
    // -----------------------------------------------------------------------

    private void seedData() {
        // --- Rooms ---
        Room r1 = new Room("LIB-301",  "Library Quiet Study",     30);
        Room r2 = new Room("LAB-101",  "Computer Science Lab",    50);
        Room r3 = new Room("HALL-B2",  "Main Lecture Hall B",    200);
        saveRoom(r1);
        saveRoom(r2);
        saveRoom(r3);

        // --- Sensors ---
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE",      21.5,  "LIB-301");
        Sensor s2 = new Sensor("CO2-002",  "CO2",         "ACTIVE",      412.0, "LAB-101");
        Sensor s3 = new Sensor("OCC-003",  "Occupancy",   "MAINTENANCE", 0.0,   "HALL-B2");
        saveSensor(s1);
        saveSensor(s2);
        saveSensor(s3);

        // --- Link sensors to rooms (maintain bidirectional reference by ID) ---
        r1.getSensorIds().add("TEMP-001");
        r2.getSensorIds().add("CO2-002");
        r3.getSensorIds().add("OCC-003");

        // --- Historical readings for seeded sensors ---
        addReading("TEMP-001", new SensorReading(20.1));
        addReading("TEMP-001", new SensorReading(21.0));
        addReading("TEMP-001", new SensorReading(21.5));
        addReading("CO2-002",  new SensorReading(400.0));
        addReading("CO2-002",  new SensorReading(412.0));
    }
}
