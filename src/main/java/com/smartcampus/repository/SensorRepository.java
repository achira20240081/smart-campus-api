package com.smartcampus.repository;

import com.smartcampus.model.DataStore;
import com.smartcampus.model.Sensor;

import java.util.Collection;
import java.util.Optional;

/**
 * Repository for {@link Sensor} entities.
 *
 * Provides a clean, type-safe data access interface for sensors.
 * All storage details (ConcurrentHashMap inside DataStore) are hidden
 * behind this API — the service layer sees only findAll/findById/save/delete.
 *
 * @see RoomRepository for the full repository pattern rationale.
 */
public class SensorRepository {

    private final DataStore store = DataStore.getInstance();

    /** Returns all sensors as an unordered collection. */
    public Collection<Sensor> findAll() {
        return store.getSensors().values();
    }

    /** Returns the sensor with the given ID, or empty if not found. */
    public Optional<Sensor> findById(String id) {
        return Optional.ofNullable(store.getSensor(id));
    }

    /**
     * Persists a sensor and initialises its empty reading list.
     * Safe to call multiple times — subsequent calls update the sensor record.
     */
    public void save(Sensor sensor) {
        store.saveSensor(sensor);
    }

    /**
     * Deletes a sensor and all its associated readings.
     *
     * @return {@code true} if the sensor existed and was deleted.
     */
    public boolean deleteById(String id) {
        return store.deleteSensor(id);
    }

    /** Returns {@code true} if a sensor with the given ID exists. */
    public boolean existsById(String id) {
        return store.sensorExists(id);
    }
}
