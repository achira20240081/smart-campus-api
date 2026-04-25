package com.smartcampus.repository;

import com.smartcampus.model.DataStore;
import com.smartcampus.model.Room;

import java.util.Collection;
import java.util.Optional;

/**
 * Repository for {@link Room} entities.
 *
 * WHY A REPOSITORY LAYER?
 * -------------------------
 * The Repository pattern abstracts the data storage mechanism from the rest
 * of the application. Today the storage is an in-memory ConcurrentHashMap
 * inside DataStore. In a real production system, this class would be the
 * ONLY class that changes when switching to a JPA/Hibernate database — the
 * service and resource layers remain completely untouched.
 *
 * This gives us:
 *   1. Testability  — unit tests can mock RoomRepository without needing a
 *                     real DataStore or database.
 *   2. Replaceability — storage technology is swappable behind a stable API.
 *   3. Single Responsibility — data access code lives here only; business
 *                     rules live exclusively in RoomService.
 *
 * Uses {@link Optional} return types to make null-safety explicit at the
 * call site — callers must consciously handle the "not found" case.
 */
public class RoomRepository {

    // Delegate to the thread-safe singleton DataStore
    private final DataStore store = DataStore.getInstance();

    /** Returns all rooms as an unordered collection. */
    public Collection<Room> findAll() {
        return store.getRooms().values();
    }

    /** Returns the room with the given ID, or empty if not found. */
    public Optional<Room> findById(String id) {
        return Optional.ofNullable(store.getRoom(id));
    }

    /** Persists (inserts or updates) a room. */
    public void save(Room room) {
        store.saveRoom(room);
    }

    /**
     * Deletes the room with the given ID.
     *
     * @return {@code true} if the room existed and was deleted;
     *         {@code false} if no room with that ID was found.
     */
    public boolean deleteById(String id) {
        return store.deleteRoom(id);
    }

    /** Returns {@code true} if a room with the given ID exists. */
    public boolean existsById(String id) {
        return store.roomExists(id);
    }
}
