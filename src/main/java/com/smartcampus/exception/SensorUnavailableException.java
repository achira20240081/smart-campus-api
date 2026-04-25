package com.smartcampus.exception;

/**
 * Thrown when a POST /readings request is made to a sensor whose status is
 * not "ACTIVE" (i.e., it is in "MAINTENANCE" or "OFFLINE" state).
 *
 * Maps to HTTP 403 Forbidden via SensorUnavailableExceptionMapper.
 *
 * 403 is appropriate because the server understood the request but is
 * refusing to fulfil it — the sensor is intentionally rejecting data
 * submission because it is not in an operational state.
 */
public class SensorUnavailableException extends RuntimeException {

    private final String sensorId;
    private final String status;

    public SensorUnavailableException(String sensorId, String status) {
        super("Sensor '" + sensorId + "' is currently in '" + status
            + "' state and cannot accept new readings.");
        this.sensorId = sensorId;
        this.status   = status;
    }

    public String getSensorId() { return sensorId; }
    public String getStatus()   { return status; }
}
