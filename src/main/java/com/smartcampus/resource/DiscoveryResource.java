package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery resource — GET /api/v1
 *
 * Returns API metadata and HATEOAS hypermedia links.
 * This is the entry point: a client who knows only the base URL can follow
 * the returned links to explore the entire API surface without hard-coded paths.
 *
 * No service class is needed here — the response is purely derived from
 * the request URI (no business logic or data access involved).
 */
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @Context
    private UriInfo uriInfo;

    /**
     * GET /api/v1
     * Returns version, contact, status, and HATEOAS links to all collections.
     */
    @GET
    public Response discover() {
        String base = uriInfo.getBaseUri().toString();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("version",     "1.0.0");
        response.put("description", "Smart Campus Sensor Management API");
        response.put("contact",     "admin@smartcampus.ac.uk");
        response.put("status",      "UP");

        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",    base);
        links.put("rooms",   base + "rooms");
        links.put("sensors", base + "sensors");
        response.put("links", links);

        return Response.ok(response).build();
    }
}
