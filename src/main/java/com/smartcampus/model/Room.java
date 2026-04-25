package com.smartcampus.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical room in the Smart Campus system.
 * Each room can contain multiple sensors identified by their IDs.
 *
 * DESIGN NOTE — using IDs rather than embedded Sensor objects:
 * sensorIds stores String IDs rather than full Sensor objects. This avoids
 * circular references (Sensor.roomId → Room → sensorIds → Sensor → ...) which
 * would cause infinite recursion during JSON serialisation. It also keeps
 * payloads small and lets clients fetch full sensor details on demand —
 * a common REST pattern that reduces over-fetching.
 */
public class Room {

    private String id;                                         // Unique identifier, e.g., "LIB-301"
    private String name;                                       // Human-readable name
    private int    capacity;                                   // Maximum occupancy
    private List<String> sensorIds = new ArrayList<>();        // IDs of deployed sensors

    // Default constructor — required for Jackson to deserialise incoming JSON
    public Room() {}

    public Room(String id, String name, int capacity) {
        this.id       = id;
        this.name     = name;
        this.capacity = capacity;
    }

    // -----------------------------------------------------------------------
    // Getters & Setters
    // -----------------------------------------------------------------------

    public String getId()                        { return id; }
    public void   setId(String id)               { this.id = id; }

    public String getName()                      { return name; }
    public void   setName(String name)           { this.name = name; }

    public int    getCapacity()                  { return capacity; }
    public void   setCapacity(int capacity)      { this.capacity = capacity; }

    public List<String> getSensorIds()           { return sensorIds; }
    public void         setSensorIds(List<String> sensorIds) { this.sensorIds = sensorIds; }
}
