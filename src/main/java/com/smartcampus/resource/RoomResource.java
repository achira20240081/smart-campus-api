package com.smartcampus.resource;

import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.model.Room;
import com.smartcampus.service.RoomService;

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
 * Room resource — thin HTTP adapter for {@link RoomService}.
 *
 * This class is responsible ONLY for:
 *   1. Parsing HTTP request data (@PathParam, @QueryParam, request body).
 *   2. Delegating to {@link RoomService} for ALL business logic.
 *   3. Building the HTTP response (status code, Location header, body).
 *
 * Endpoints:
 *   GET    /api/v1/rooms         → list all rooms (with HATEOAS _links)
 *   POST   /api/v1/rooms         → create a room  (201 + Location + _links)
 *   GET    /api/v1/rooms/{id}    → get a single room (with HATEOAS _links)
 *   DELETE /api/v1/rooms/{id}    → delete a room (409 if sensors exist)
 */
@Path("rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final RoomService roomService = new RoomService();

    @Context
    private UriInfo uriInfo;

    // ------------------------------------------------------------------
    // HATEOAS helper
    // ------------------------------------------------------------------

    /**
     * Wraps a {@link Room} in a {@link LinkedHashMap} that includes a
     * {@code _links} object — the standard HATEOAS presentation pattern.
     *
     * Keeping this in the resource class respects the separation of concerns:
     *   - model/Room is a pure data class (no HTTP knowledge).
     *   - resource/RoomResource owns all HTTP presentation including hypermedia.
     *
     * @param room the room entity to wrap
     * @param base the base URI (e.g. "http://localhost:8080/api/v1/")
     */
    private Map<String, Object> roomWithLinks(Room room, String base) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",        room.getId());
        map.put("name",      room.getName());
        map.put("capacity",  room.getCapacity());
        map.put("sensorIds", room.getSensorIds());

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",    base + "rooms/" + room.getId());
        links.put("sensors", base + "sensors");
        map.put("_links", links);
        return map;
    }

    // ------------------------------------------------------------------
    // GET /api/v1/rooms
    // ------------------------------------------------------------------

    /** Returns all rooms, each with HATEOAS _links. Always 200 OK. */
    @GET
    public Response getAllRooms() {
        String base = uriInfo.getBaseUri().toString();
        List<Map<String, Object>> result = roomService.findAll()
                .stream()
                .map(r -> roomWithLinks(r, base))
                .collect(Collectors.toList());
        return Response.ok(result).build();
    }

    // ------------------------------------------------------------------
    // POST /api/v1/rooms
    // ------------------------------------------------------------------

    /**
     * Creates a new room.
     * Returns 201 Created with Location header and HATEOAS _links body.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        try {
            Room created = roomService.create(room);
            URI location = uriInfo.getAbsolutePathBuilder()
                                  .path(created.getId())
                                  .build();
            String base = uriInfo.getBaseUri().toString();
            return Response.created(location)
                           .entity(roomWithLinks(created, base))
                           .build();

        } catch (IllegalArgumentException e) {
            // Missing required field → 400
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "BAD_REQUEST", e.getMessage(), null))
                    .build();
        } catch (IllegalStateException e) {
            // Duplicate ID → 409
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse(409, "CONFLICT", e.getMessage(), null))
                    .build();
        }
    }

    // ------------------------------------------------------------------
    // GET /api/v1/rooms/{id}
    // ------------------------------------------------------------------

    /** Returns a single room with HATEOAS _links. 404 via mapper if missing. */
    @GET
    @Path("{id}")
    public Response getRoom(@PathParam("id") String id) {
        Room room = roomService.findById(id);   // throws 404 if missing
        String base = uriInfo.getBaseUri().toString();
        return Response.ok(roomWithLinks(room, base)).build();
    }

    // ------------------------------------------------------------------
    // DELETE /api/v1/rooms/{id}
    // ------------------------------------------------------------------

    /**
     * Deletes a room.
     * Returns 204 No Content on success.
     * RoomNotEmptyException → 409 via mapper if sensors still exist.
     * ResourceNotFoundException → 404 via mapper if room not found.
     */
    @DELETE
    @Path("{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        roomService.delete(id);   // throws 404 or 409 via mappers
        return Response.noContent().build();  // 204 No Content
    }
}
