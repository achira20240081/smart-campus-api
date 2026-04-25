package com.smartcampus.exception.mapper;

import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.exception.ResourceNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps {@link ResourceNotFoundException} → HTTP 404 Not Found.
 * Triggered when a URL path ID (e.g. /rooms/XYZ) does not exist.
 */
@Provider
public class ResourceNotFoundExceptionMapper
        implements ExceptionMapper<ResourceNotFoundException> {

    @Override
    public Response toResponse(ResourceNotFoundException ex) {
        ErrorResponse body = new ErrorResponse(
            404, "NOT_FOUND", ex.getMessage(),
            "Check that the ID in the URL path is correct.");
        return Response.status(Response.Status.NOT_FOUND)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body).build();
    }
}
