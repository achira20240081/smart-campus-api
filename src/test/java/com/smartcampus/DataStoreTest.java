package com.smartcampus;

import com.smartcampus.model.DataStore;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Unit tests for the DataStore singleton.
 *
 * These tests verify the in-memory storage layer in complete isolation —
 * no HTTP server, no JSON serialisation, no external dependencies.
 *
 * Test naming convention: methodUnderTest_scenario_expectedOutcome()
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DataStoreTest {

    private static final DataStore store = DataStore.getInstance();

    // -----------------------------------------------------------------------
    // Room tests
    // -----------------------------------------------------------------------

    @Test
    @Order(1)
    @DisplayName("Seeded rooms are present on startup")
    void getInstance_seedData_roomsExist() {
        assertTrue(store.roomExists("LIB-301"),  "Library room should be seeded");
        assertTrue(store.roomExists("LAB-101"),  "Lab room should be seeded");
        assertTrue(store.roomExists("HALL-B2"),  "Hall room should be seeded");
    }

    @Test
    @Order(2)
    @DisplayName("saveRoom persists a new room and getRoom retrieves it")
    void saveRoom_newRoom_retrievedSuccessfully() {
        Room r = new Room("TEST-001", "Test Room", 10);
        store.saveRoom(r);

        Room fetched = store.getRoom("TEST-001");
        assertNotNull(fetched,                       "Room should be retrievable after save");
        assertEquals("Test Room", fetched.getName(), "Name should match");
        assertEquals(10,          fetched.getCapacity(), "Capacity should match");
    }

    @Test
    @Order(3)
    @DisplayName("deleteRoom removes a room and returns true; subsequent call returns false")
    void deleteRoom_existingRoom_deletedSuccessfully() {
        store.saveRoom(new Room("DEL-001", "To Be Deleted", 5));
        assertTrue(store.deleteRoom("DEL-001"),  "First delete should return true");
        assertFalse(store.roomExists("DEL-001"), "Room should no longer exist");
        assertFalse(store.deleteRoom("DEL-001"), "Second delete should return false (already gone)");
    }

    @Test
    @Order(4)
    @DisplayName("getRoom returns null for an unknown ID")
    void getRoom_unknownId_returnsNull() {
        assertNull(store.getRoom("NONEXISTENT-999"),
            "getRoom should return null for an ID that was never saved");
    }

    // -----------------------------------------------------------------------
    // Sensor tests
    // -----------------------------------------------------------------------

    @Test
    @Order(5)
    @DisplayName("Seeded sensors are present on startup")
    void getInstance_seedData_sensorsExist() {
        assertTrue(store.sensorExists("TEMP-001"), "Temperature sensor should be seeded");
        assertTrue(store.sensorExists("CO2-002"),  "CO2 sensor should be seeded");
        assertTrue(store.sensorExists("OCC-003"),  "Occupancy sensor should be seeded");
    }

    @Test
    @Order(6)
    @DisplayName("saveSensor persists a sensor and initialises its reading list")
    void saveSensor_newSensor_readingListInitialised() {
        Sensor s = new Sensor("UNIT-001", "Temperature", "ACTIVE", 22.0, "LIB-301");
        store.saveSensor(s);

        assertTrue(store.sensorExists("UNIT-001"),        "Sensor should exist after save");
        assertNotNull(store.getReadings("UNIT-001"),       "Reading list should be initialised");
        assertTrue(store.getReadings("UNIT-001").isEmpty(),"Reading list should start empty");
    }

    @Test
    @Order(7)
    @DisplayName("addReading appends reading and getReadings returns it")
    void addReading_validReading_appendedToList() {
        SensorReading r = new SensorReading(99.9);
        store.addReading("UNIT-001", r);

        List<SensorReading> readings = store.getReadings("UNIT-001");
        assertFalse(readings.isEmpty(),      "Readings list should not be empty");
        assertEquals(99.9, readings.get(readings.size() - 1).getValue(), 0.001,
            "Last reading value should match what was added");
    }

    @Test
    @Order(8)
    @DisplayName("deleteSensor removes sensor and its readings")
    void deleteSensor_existingSensor_removedWithReadings() {
        Sensor s = new Sensor("DEL-SENSOR-001", "CO2", "ACTIVE", 400.0, "LAB-101");
        store.saveSensor(s);
        store.addReading("DEL-SENSOR-001", new SensorReading(400.0));

        assertTrue(store.deleteSensor("DEL-SENSOR-001"),  "Delete should return true");
        assertFalse(store.sensorExists("DEL-SENSOR-001"), "Sensor should no longer exist");
        assertTrue(store.getReadings("DEL-SENSOR-001").isEmpty(),
            "Readings should be cleared after sensor deletion");
    }

    // -----------------------------------------------------------------------
    // Sensor Reading tests
    // -----------------------------------------------------------------------

    @Test
    @Order(9)
    @DisplayName("Seeded readings exist for TEMP-001 and CO2-002")
    void getInstance_seedData_readingsExist() {
        assertFalse(store.getReadings("TEMP-001").isEmpty(), "TEMP-001 should have seeded readings");
        assertFalse(store.getReadings("CO2-002").isEmpty(),  "CO2-002 should have seeded readings");
    }

    @Test
    @Order(10)
    @DisplayName("getReadings returns empty list for sensor with no readings")
    void getReadings_sensorWithNoReadings_returnsEmptyList() {
        store.saveSensor(new Sensor("EMPTY-001", "Humidity", "ACTIVE", 0.0, "LIB-301"));
        List<SensorReading> readings = store.getReadings("EMPTY-001");
        assertNotNull(readings, "Should return an empty list, not null");
        assertTrue(readings.isEmpty(), "Should be empty");
    }
}
