package com.smartcampus.service;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.repository.SensorRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class containing all business logic for {@link Sensor} operations.
 *
 * Key responsibilities:
 *   - Validates that the referenced {@code roomId} actually exists before
 *     creating a sensor (throws {@link LinkedResourceNotFoundException} → 422).
 *   - Maintains the bidirectional Room ↔ Sensor link via {@link RoomService}.
 *   - Provides type-safe filtering by sensor type for the @QueryParam endpoint.
 *
 * @see RoomService for the full service layer rationale.
 */
public class SensorService {

    private final SensorRepository sensorRepository = new SensorRepository();
    // RoomService is used here to validate the linked room and maintain the
    // bidirectional sensorIds reference on the Room entity.
    private final RoomService roomService = new RoomService();

    // -----------------------------------------------------------------------
    // Read operations
    // -----------------------------------------------------------------------

    /**
     * Returns all sensors, optionally filtered by type (case-insensitive).
     *
     * @param type optional type filter (e.g., "CO2"); pass {@code null} for all.
     */
    public List<Sensor> findAll(String type) {
        List<Sensor> all = new ArrayList<>(sensorRepository.findAll());
        if (type == null || type.isBlank()) {
            return all;
        }
        return all.stream()
                  .filter(s -> s.getType().equalsIgnoreCase(type))
                  .collect(Collectors.toList());
    }

    /**
     * Returns a sensor by ID.
     *
     * @throws ResourceNotFoundException (→ 404) if the sensor does not exist.
     */
    public Sensor findById(String id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", id));
    }

    // -----------------------------------------------------------------------
    // Write operations
    // -----------------------------------------------------------------------

    /**
     * Validates and persists a new sensor.
     *
     * Validation rules (in order):
     *   1. {@code id} is required.
     *   2. {@code roomId} is required.
     *   3. The referenced {@code roomId} must exist → throws 422 if not.
     *   4. {@code id} must be unique → throws 409 if duplicate.
     *
     * Side effect: adds the sensor ID to the parent Room's {@code sensorIds} list
     * to maintain the bidirectional Room ↔ Sensor reference.
     *
     * @param sensor the sensor to create
     * @return the persisted sensor
     */
    public Sensor create(Sensor sensor) {
        // --- Field validation ---
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            throw new IllegalArgumentException("Sensor 'id' is required.");
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            throw new IllegalArgumentException("Sensor 'roomId' is required.");
        }

        // --- Linked resource validation (422 if room doesn't exist) ---
        if (!roomService.exists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        // --- Uniqueness check (409 if duplicate) ---
        if (sensorRepository.existsById(sensor.getId())) {
            throw new IllegalStateException(
                    "Sensor with id '" + sensor.getId() + "' already exists.");
        }

        // Default status to ACTIVE if not supplied
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        sensorRepository.save(sensor);

        // Maintain bidirectional link: Room.sensorIds must include this sensor
        roomService.addSensorToRoom(sensor.getRoomId(), sensor.getId());

        return sensor;
    }

    /** Returns {@code true} if a sensor with the given ID exists. */
    public boolean exists(String id) {
        return sensorRepository.existsById(id);
    }

    // -----------------------------------------------------------------------
    // Update operation (PUT)
    // -----------------------------------------------------------------------

    /**
     * Updates a sensor's mutable fields in-place.
     *
     * Rules:
     *   - Sensor must exist → {@link ResourceNotFoundException} (404).
     *   - {@code status}, if provided, must be ACTIVE | MAINTENANCE | OFFLINE.
     *   - {@code currentValue} is always applied (0.0 is a valid reading).
     *   - {@code roomId} changes are not supported via this method; integrity
     *     would require moving sensorIds across two room objects atomically.
     *
     * @param id      path ID of the sensor to update
     * @param updates partial or full sensor payload from the PUT body
     * @return the updated sensor
     */
    public Sensor update(String id, Sensor updates) {
        Sensor existing = findById(id); // throws 404 if missing

        if (updates.getType() != null && !updates.getType().isBlank()) {
            existing.setType(updates.getType());
        }
        if (updates.getStatus() != null && !updates.getStatus().isBlank()) {
            String s = updates.getStatus().toUpperCase();
            if (!s.equals("ACTIVE") && !s.equals("MAINTENANCE") && !s.equals("OFFLINE")) {
                throw new IllegalArgumentException(
                    "Invalid status '" + updates.getStatus() +
                    "'. Must be ACTIVE, MAINTENANCE, or OFFLINE.");
            }
            existing.setStatus(s);
        }
        // currentValue is a primitive double — always update it
        existing.setCurrentValue(updates.getCurrentValue());

        return existing;
    }

    // -----------------------------------------------------------------------
    // Delete operation (DELETE)
    // -----------------------------------------------------------------------

    /**
     * Deletes a sensor and all its associated readings.
     *
     * Side effect: removes the sensor ID from the parent Room's
     * {@code sensorIds} list to keep the bidirectional Room ↔ Sensor link
     * consistent. This mirrors the add performed in {@link #create}.
     *
     * @throws ResourceNotFoundException (→ 404) if sensor does not exist.
     */
    public void delete(String id) {
        Sensor sensor = findById(id); // throws 404 if missing

        // Maintain bidirectional integrity: remove from parent room's list
        roomService.removeSensorFromRoom(sensor.getRoomId(), id);

        // Delete the sensor record and all its historical readings
        sensorRepository.deleteById(id);
    }
}
