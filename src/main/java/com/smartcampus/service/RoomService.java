package com.smartcampus.service;

import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.repository.RoomRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class containing all business logic for {@link Room} operations.
 *
 * WHY A SERVICE LAYER?
 * ----------------------
 * The service layer sits between the resource (presentation) layer and the
 * repository (data access) layer. It is responsible for:
 *
 *   1. Business rules — e.g., "a room cannot be deleted while it has sensors"
 *      lives HERE, not in the resource class or the repository.
 *   2. Orchestration — service methods coordinate multiple repository calls
 *      within a single logical operation.
 *   3. Reusability — two different resource classes could call the same service
 *      method without duplicating logic.
 *   4. Testability — services can be unit-tested with a mock RoomRepository,
 *      completely independent of HTTP concerns.
 *
 * The resource class becomes a thin adapter: it handles HTTP parsing/response
 * building and delegates ALL logic to the service.
 */
public class RoomService {

    private final RoomRepository roomRepository = new RoomRepository();

    // -----------------------------------------------------------------------
    // Read operations
    // -----------------------------------------------------------------------

    /** Returns all rooms as an ordered list. */
    public List<Room> findAll() {
        return new ArrayList<>(roomRepository.findAll());
    }

    /**
     * Returns a room by ID.
     *
     * @throws ResourceNotFoundException (→ HTTP 404) if the room does not exist.
     */
    public Room findById(String id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }

    // -----------------------------------------------------------------------
    // Write operations
    // -----------------------------------------------------------------------

    /**
     * Validates and persists a new room.
     *
     * Validation rules:
     *   - {@code id} and {@code name} are required (non-blank).
     *   - {@code id} must be unique (no existing room with the same ID).
     *
     * @param room the room to create
     * @throws IllegalArgumentException if required fields are missing
     * @throws IllegalStateException    if the room ID already exists
     */
    public Room create(Room room) {
        // Field validation
        if (room.getId() == null || room.getId().isBlank()) {
            throw new IllegalArgumentException("Room 'id' is required.");
        }
        if (room.getName() == null || room.getName().isBlank()) {
            throw new IllegalArgumentException("Room 'name' is required.");
        }
        // Uniqueness check
        if (roomRepository.existsById(room.getId())) {
            throw new IllegalStateException("Room with id '" + room.getId() + "' already exists.");
        }

        roomRepository.save(room);
        return room;
    }

    /**
     * Deletes a room by ID.
     *
     * Business rule: a room cannot be deleted while it still has sensors
     * assigned. Doing so would leave sensors with a dangling roomId reference.
     *
     * @throws ResourceNotFoundException (→ 404) if the room does not exist.
     * @throws RoomNotEmptyException     (→ 409) if sensors are still assigned.
     */
    public void delete(String id) {
        Room room = findById(id);  // throws 404 if missing

        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(id);  // throws 409
        }

        roomRepository.deleteById(id);
    }

    // -----------------------------------------------------------------------
    // Helper used by SensorService to maintain bidirectional links
    // -----------------------------------------------------------------------

    /** Returns {@code true} if a room with the given ID exists. */
    public boolean exists(String id) {
        return roomRepository.existsById(id);
    }

    /** Adds a sensor ID to the room's sensorIds list. */
    public void addSensorToRoom(String roomId, String sensorId) {
        roomRepository.findById(roomId).ifPresent(r -> r.getSensorIds().add(sensorId));
    }

    /** Removes a sensor ID from the room's sensorIds list. */
    public void removeSensorFromRoom(String roomId, String sensorId) {
        roomRepository.findById(roomId).ifPresent(r -> r.getSensorIds().remove(sensorId));
    }
}
