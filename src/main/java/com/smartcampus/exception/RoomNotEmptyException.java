package com.smartcampus.exception;

/**
 * Thrown when a client attempts to DELETE a Room that still has Sensor(s)
 * assigned to it.
 *
 * Maps to HTTP 409 Conflict via RoomNotEmptyExceptionMapper.
 *
 * Business rule: a Room cannot be deleted while it contains sensors because
 * orphaned sensors would have a dangling roomId reference. The client must
 * first remove or reassign all sensors, then delete the room.
 */
public class RoomNotEmptyException extends RuntimeException {

    private final String roomId;

    public RoomNotEmptyException(String roomId) {
        super("Room '" + roomId + "' still has sensors assigned to it. "
            + "Remove all sensors from the room before deleting it.");
        this.roomId = roomId;
    }

    public String getRoomId() { return roomId; }
}
