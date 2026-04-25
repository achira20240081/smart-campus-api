package com.smartcampus.exception;

/**
 * Thrown when a directly-addressed resource (identified in the URL path)
 * is not found in the data store.
 *
 * Maps to HTTP 404 Not Found via ResourceNotFoundExceptionMapper.
 *
 * Example: GET /api/v1/rooms/UNKNOWN-ID → 404
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;  // e.g., "Room", "Sensor"
    private final String resourceId;    // e.g., "LIB-999"

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " with id '" + resourceId + "' does not exist.");
        this.resourceType = resourceType;
        this.resourceId   = resourceId;
    }

    public String getResourceType() { return resourceType; }
    public String getResourceId()   { return resourceId; }
}
