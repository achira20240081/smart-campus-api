package com.smartcampus.exception;

/**
 * Thrown when a resource referenced INSIDE a JSON request payload
 * (a "linked" or "foreign-key" resource) does not exist.
 *
 * Maps to HTTP 422 Unprocessable Entity via LinkedResourceNotFoundExceptionMapper.
 *
 * -----------------------------------------------------------------------
 * WHY 422 INSTEAD OF 404?
 * -----------------------------------------------------------------------
 * HTTP 404 means the requested URL itself was not found on the server.
 * In the scenario below the URL IS valid (POST /api/v1/sensors exists),
 * but the payload contains a roomId that references a non-existent Room:
 *
 *   POST /api/v1/sensors
 *   { "id": "NEW-001", "type": "CO2", "roomId": "GHOST-999" }
 *
 * The request is syntactically correct JSON and arrived at a known URL —
 * so 404 would be semantically wrong. 422 explicitly signals that the
 * server understood the content type and structure but was unable to
 * process the contained instructions because a referenced entity does
 * not exist. This gives the client a clear signal to fix the payload,
 * not the URL.
 * -----------------------------------------------------------------------
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    private final String resourceType;  // e.g., "Room"
    private final String resourceId;    // e.g., "GHOST-999"

    public LinkedResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " with id '" + resourceId + "' was not found.");
        this.resourceType = resourceType;
        this.resourceId   = resourceId;
    }

    public String getResourceType() { return resourceType; }
    public String getResourceId()   { return resourceId; }
}
