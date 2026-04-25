package com.smartcampus.repository;

import com.smartcampus.model.DataStore;
import com.smartcampus.model.SensorReading;

import java.util.List;

/**
 * Repository for {@link SensorReading} entities.
 *
 * Readings are stored as an ordered list per sensor (key = sensorId).
 * The insertion order is preserved, giving clients a chronological history.
 *
 * @see RoomRepository for the full repository pattern rationale.
 */
public class SensorReadingRepository {

    private final DataStore store = DataStore.getInstance();

    /**
     * Returns all readings for a sensor in insertion (chronological) order.
     * Returns an empty list if the sensor has no readings.
     *
     * @param sensorId the ID of the parent sensor
     */
    public List<SensorReading> findBySensorId(String sensorId) {
        return store.getReadings(sensorId);
    }

    /**
     * Appends a reading to the sensor's history list.
     *
     * @param sensorId the ID of the parent sensor
     * @param reading  the reading to persist
     */
    public void save(String sensorId, SensorReading reading) {
        store.addReading(sensorId, reading);
    }
}
