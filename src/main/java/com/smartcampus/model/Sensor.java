package com.smartcampus.model;

/**
 * Represents an IoT sensor deployed within a campus room.
 *
 * Status lifecycle: ACTIVE → MAINTENANCE → OFFLINE
 *   ACTIVE      – sensor is operational and can accept new readings.
 *   MAINTENANCE – sensor is undergoing scheduled calibration or repair.
 *   OFFLINE     – sensor is powered down or unreachable.
 *
 * Only ACTIVE sensors accept new SensorReading submissions (enforced by
 * SensorReadingResource which throws SensorUnavailableException / HTTP 403
 * for any other status).
 */
public class Sensor {

    private String id;            // Unique identifier, e.g., "TEMP-001"
    private String type;          // Measurement category: "Temperature", "CO2", "Occupancy"
    private String status;        // Operational state: ACTIVE | MAINTENANCE | OFFLINE
    private double currentValue;  // Most recent measurement; updated on every new reading
    private String roomId;        // Foreign key — the Room this sensor belongs to

    // Default constructor — required for Jackson deserialisation
    public Sensor() {}

    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id           = id;
        this.type         = type;
        this.status       = status;
        this.currentValue = currentValue;
        this.roomId       = roomId;
    }

    // -----------------------------------------------------------------------
    // Getters & Setters
    // -----------------------------------------------------------------------

    public String getId()                           { return id; }
    public void   setId(String id)                  { this.id = id; }

    public String getType()                         { return type; }
    public void   setType(String type)              { this.type = type; }

    public String getStatus()                       { return status; }
    public void   setStatus(String status)          { this.status = status; }

    public double getCurrentValue()                 { return currentValue; }
    public void   setCurrentValue(double v)         { this.currentValue = v; }

    public String getRoomId()                       { return roomId; }
    public void   setRoomId(String roomId)          { this.roomId = roomId; }
}
