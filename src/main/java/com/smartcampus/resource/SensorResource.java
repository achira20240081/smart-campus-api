package com.smartcampus.resource;

import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.model.Sensor;
import com.smartcampus.service.SensorService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sensor resource — thin HTTP adapter for {@link SensorService}.
 *
 * Endpoints:
 *   POST   /api/v1/sensors          → create sensor (validates roomId → 422)
 *   GET    /api/v1/sensors          → list all sensors (with HATEOAS _links)
 *   GET    /api/v1/sensors?type=CO2 → filter by type (@QueryParam)
 *   GET    /api/v1/sensors/{id}     → get single sensor (with HATEOAS _links)
 *   PUT    /api/v1/sensors/{id}     → update sensor (type, status, currentValue)
 *   DELETE /api/v1/sensors/{id}     → delete sensor + its readings (204)
 *   (sub-resource locator)          → /sensors/{id}/readings
 */
@Path("sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final SensorService sensorService = new SensorService();

    @Context
    private UriInfo uriInfo;

    // ------------------------------------------------------------------
    // HATEOAS helper
    // ------------------------------------------------------------------

    /**
     * Wraps a {@link Sensor} in a {@link LinkedHashMap} that includes a
     * {@code _links} object — the standard HATEOAS pattern.
     *
     * Keeps the model class clean (no presentation logic on the entity).
     * The map is serialised to JSON by Jackson exactly as a Sensor would be,
     * with an additional {@code _links} field appended at the end.
     */
    private Map<String, Object> sensorWithLinks(Sensor s, String base) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",           s.getId());
        map.put("type",         s.getType());
        map.put("status",       s.getStatus());
        map.put("currentValue", s.getCurrentValue());
        map.put("roomId",       s.getRoomId());

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",     base + "sensors/" + s.getId());
        links.put("readings", base + "sensors/" + s.getId() + "/readings");
        links.put("room",     base + "rooms/"   + s.getRoomId());
        map.put("_links", links);
        return map;
    }

    // ------------------------------------------------------------------
    // POST /api/v1/sensors
    // ------------------------------------------------------------------

    /**
     * Creates a new sensor. Returns 201 Created with HATEOAS links.
     * Delegates all validation (422 roomId check) to SensorService.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        try {
            Sensor created = sensorService.create(sensor);
            URI location = uriInfo.getAbsolutePathBuilder()
                                  .path(created.getId())
                                  .build();
            String base = uriInfo.getBaseUri().toString();
            return Response.created(location)
                           .entity(sensorWithLinks(created, base))
                           .build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "BAD_REQUEST", e.getMessage(), null))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(409, "CONFLICT", e.getMessage(), null))
                    .build();
            // LinkedResourceNotFoundException (422) propagates to its @Provider mapper
        }
    }

    // ------------------------------------------------------------------
    // GET /api/v1/sensors  and  GET /api/v1/sensors?type=CO2
    // ------------------------------------------------------------------

    /**
     * Lists all sensors, optionally filtered by type (case-insensitive).
     * Each sensor in the list includes HATEOAS {@code _links}.
     *
     * @QueryParam("type") — binds the ?type= query string parameter.
     * If omitted (null), all sensors are returned.
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        String base = uriInfo.getBaseUri().toString();
        List<Map<String, Object>> result = sensorService.findAll(type)
                .stream()
                .map(s -> sensorWithLinks(s, base))
                .collect(Collectors.toList());
        return Response.ok(result).build();
    }

    // ------------------------------------------------------------------
    // GET /api/v1/sensors/{id}
    // ------------------------------------------------------------------

    /** Returns a single sensor with HATEOAS links. 404 via mapper if missing. */
    @GET
    @Path("{id}")
    public Response getSensor(@PathParam("id") String id) {
        Sensor sensor = sensorService.findById(id);
        String base = uriInfo.getBaseUri().toString();
        return Response.ok(sensorWithLinks(sensor, base)).build();
    }

    // ------------------------------------------------------------------
    // PUT /api/v1/sensors/{id}
    // ------------------------------------------------------------------

    /**
     * Updates a sensor's mutable fields: {@code type}, {@code status},
     * {@code currentValue}.  Only fields present in the body are applied;
     * {@code id} and {@code roomId} are immutable via this endpoint.
     *
     * Returns 200 OK with the updated sensor body (+ HATEOAS links).
     * ResourceNotFoundException propagates → 404 via mapper.
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSensor(@PathParam("id") String id, Sensor updates) {
        try {
            Sensor updated = sensorService.update(id, updates);
            String base = uriInfo.getBaseUri().toString();
            return Response.ok(sensorWithLinks(updated, base)).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "BAD_REQUEST", e.getMessage(), null))
                    .build();
        }
        // ResourceNotFoundException (404) propagates to its mapper
    }

    // ------------------------------------------------------------------
    // DELETE /api/v1/sensors/{id}
    // ------------------------------------------------------------------

    /**
     * Deletes a sensor and all its historical readings.
     * Also removes the sensor ID from the parent Room's {@code sensorIds}.
     *
     * Returns 204 No Content on success.
     * ResourceNotFoundException propagates → 404 via mapper.
     */
    @DELETE
    @Path("{id}")
    public Response deleteSensor(@PathParam("id") String id) {
        sensorService.delete(id); // throws 404 if missing
        return Response.noContent().build(); // 204
    }

    // ------------------------------------------------------------------
    // Sub-resource locator: /api/v1/sensors/{id}/readings
    // ------------------------------------------------------------------

    /**
     * Sub-resource locator — delegates /readings sub-paths to
     * {@link SensorReadingResource}.
     *
     * A sub-resource locator is a @Path method with NO HTTP verb annotation.
     * The JAX-RS runtime calls this method to obtain the object that will
     * handle the remaining path segments.
     */
    @Path("{id}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("id") String id) {
        // Validate sensor exists before delegating (throws 404 if missing)
        sensorService.findById(id);
        return new SensorReadingResource(id);
    }
}
