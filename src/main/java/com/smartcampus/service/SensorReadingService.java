package com.smartcampus.service;

import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.repository.SensorReadingRepository;
import com.smartcampus.repository.SensorRepository;
import com.smartcampus.util.IdGenerator;

import java.util.List;

/**
 * Service class containing all business logic for {@link SensorReading} operations.
 *
 * Key responsibilities:
 *   - Enforces the ACTIVE status rule: only sensors in the ACTIVE state can
 *     receive new readings (throws {@link SensorUnavailableException} → 403).
 *   - Auto-assigns a UUID and current timestamp when the client omits them.
 *   - Updates the parent Sensor's {@code currentValue} as a side effect of
 *     every successful reading submission, keeping the summary field in sync.
 *
 * @see RoomService for the full service layer rationale.
 */
public class SensorReadingService {

    private final SensorReadingRepository readingRepository = new SensorReadingRepository();
    // SensorRepository is needed to retrieve the sensor object for status checks
    // and to update its currentValue after a new reading is accepted.
    private final SensorRepository sensorRepository = new SensorRepository();

    // -----------------------------------------------------------------------
    // Read operations
    // -----------------------------------------------------------------------

    /**
     * Returns the full reading history for a sensor in chronological order.
     *
     * @param sensorId the ID of the parent sensor
     * @throws ResourceNotFoundException (→ 404) if the sensor does not exist
     */
    public List<SensorReading> findAllBySensorId(String sensorId) {
        ensureSensorExists(sensorId);
        return readingRepository.findBySensorId(sensorId);
    }

    // -----------------------------------------------------------------------
    // Write operations
    // -----------------------------------------------------------------------

    /**
     * Validates and persists a new reading for the specified sensor.
     *
     * Business rules (applied in order):
     *   1. Sensor must exist → {@link ResourceNotFoundException} (404) if not.
     *   2. Sensor status must be {@code ACTIVE} →
     *      {@link SensorUnavailableException} (403) if MAINTENANCE or OFFLINE.
     *   3. {@code id} is auto-assigned (UUID) if the client omits it.
     *   4. {@code timestamp} is auto-assigned (epoch-ms) if the client omits it.
     *
     * Side effect: the parent Sensor's {@code currentValue} is updated to match
     * the new reading's value, so {@code GET /sensors/{id}} always returns the
     * latest measurement without requiring a separate readings fetch.
     *
     * @param sensorId the ID of the parent sensor
     * @param reading  the reading submitted by the client
     * @return the persisted (and enriched) reading object
     */
    public SensorReading create(String sensorId, SensorReading reading) {
        // Rule 1: sensor must exist
        Sensor sensor = ensureSensorExists(sensorId);

        // Rule 2: sensor must be ACTIVE
        if (!"ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        // Rule 3: auto-assign UUID if omitted
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(IdGenerator.newId());
        }

        // Rule 4: auto-assign timestamp if omitted
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Persist the reading
        readingRepository.save(sensorId, reading);

        // Side effect: keep the sensor's summary currentValue up to date
        sensor.setCurrentValue(reading.getValue());

        return reading;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Sensor ensureSensorExists(String sensorId) {
        return sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", sensorId));
    }
}
