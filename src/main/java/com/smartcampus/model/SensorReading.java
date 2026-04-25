package com.smartcampus.model;

import java.util.UUID;

/**
 * Represents a single timestamped measurement captured by a sensor.
 *
 * A UUID is auto-assigned on construction so that every reading has a globally
 * unique identity — important when readings might be synchronised to a central
 * data-lake or replicated across nodes.
 *
 * timestamp is stored as epoch-milliseconds (long) rather than a formatted
 * string to avoid timezone ambiguity and to allow efficient range queries.
 */
public class SensorReading {

    private String id;         // UUID — uniquely identifies this reading event
    private long   timestamp;  // Epoch-ms when the reading was captured
    private double value;      // The actual metric value recorded by the hardware

    // Default constructor — required for Jackson to deserialise request bodies
    public SensorReading() {}

    /**
     * Convenience constructor: server assigns UUID + current timestamp.
     * Used when adding seeded / synthetic readings.
     */
    public SensorReading(double value) {
        this.id        = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.value     = value;
    }

    /**
     * Full constructor for cases where all fields are explicitly set
     * (e.g., deserialising data from an external source).
     */
    public SensorReading(String id, long timestamp, double value) {
        this.id        = id;
        this.timestamp = timestamp;
        this.value     = value;
    }

    // -----------------------------------------------------------------------
    // Getters & Setters
    // -----------------------------------------------------------------------

    public String getId()                      { return id; }
    public void   setId(String id)             { this.id = id; }

    public long   getTimestamp()               { return timestamp; }
    public void   setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue()                   { return value; }
    public void   setValue(double value)       { this.value = value; }
}
