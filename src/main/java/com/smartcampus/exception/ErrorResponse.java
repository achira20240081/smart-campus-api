package com.smartcampus.exception;

import java.time.Instant;

/**
 * Standard JSON error body returned by ALL exception mappers in this API.
 *
 * Every error response follows the same consistent structure so that API
 * consumers can parse errors generically without branching on status code.
 *
 * Example JSON:
 * {
 *   "timestamp": "2024-11-01T10:23:45Z",
 *   "status": 409,
 *   "error": "CONFLICT",
 *   "message": "Room LIB-301 still has sensors assigned to it.",
 *   "detail":  "Remove all sensors from the room before deleting it."
 * }
 */
public class ErrorResponse {

    private String timestamp;  // ISO-8601 UTC time of the error event
    private int    status;     // HTTP status code (mirrors the response status line)
    private String error;      // Short machine-readable error code, e.g. "NOT_FOUND"
    private String message;    // Human-readable explanation of the problem
    private String detail;     // Optional extra context / corrective action hint

    public ErrorResponse(int status, String error, String message, String detail) {
        this.timestamp = Instant.now().toString();
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.detail    = detail;
    }

    // Getters (Jackson needs these to serialise the object to JSON)
    public String getTimestamp() { return timestamp; }
    public int    getStatus()    { return status; }
    public String getError()     { return error; }
    public String getMessage()   { return message; }
    public String getDetail()    { return detail; }
}
