package com.smartcampus.resource;

import com.smartcampus.model.SensorReading;
import com.smartcampus.service.SensorReadingService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

/**
 * Sub-resource for sensor reading history.
 *
 * Reached via the sub-resource locator in {@link SensorResource}:
 *   GET  /api/v1/sensors/{sensorId}/readings  → list all readings
 *   POST /api/v1/sensors/{sensorId}/readings  → submit a new reading
 *
 * This class is a thin HTTP adapter — all business rules (ACTIVE-only check,
 * UUID/timestamp auto-assignment, currentValue update) live in
 * {@link SensorReadingService}.
 */
public class SensorReadingResource {

    private final String               sensorId;
    private final SensorReadingService readingService = new SensorReadingService();

    @Context
    private UriInfo uriInfo;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // ------------------------------------------------------------------
    // GET /api/v1/sensors/{sensorId}/readings
    // ------------------------------------------------------------------

    /** Returns all readings for the sensor. Always 200 OK. */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        List<SensorReading> readings = readingService.findAllBySensorId(sensorId);
        return Response.ok(readings).build();
    }

    // ------------------------------------------------------------------
    // POST /api/v1/sensors/{sensorId}/readings
    // ------------------------------------------------------------------

    /**
     * Appends a new reading. Delegates all rules to SensorReadingService:
     *   - Sensor must be ACTIVE → SensorUnavailableException (403) via mapper
     *   - id + timestamp are auto-assigned if omitted
     *   - Sensor.currentValue is updated as a side effect
     *
     * Returns 201 Created with Location: /readings/{id}
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        SensorReading created = readingService.create(sensorId, reading);
        URI location = URI.create(uriInfo.getAbsolutePath() + "/" + created.getId());
        return Response.created(location).entity(created).build();
    }
}
