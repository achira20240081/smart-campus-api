package com.smartcampus.exception.mapper;

import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps {@link LinkedResourceNotFoundException} → HTTP 422 Unprocessable Entity.
 *
 * Triggered when a request payload references a resource (e.g., roomId)
 * that does not exist. 422 is correct here because the URL is valid —
 * the problem is inside the request body.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        ErrorResponse body = new ErrorResponse(
            422, "UNPROCESSABLE_ENTITY", ex.getMessage(),
            "The '" + ex.getResourceType() + "' with id '" + ex.getResourceId()
                + "' referenced in the request body does not exist.");
        return Response.status(422)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body).build();
    }
}
