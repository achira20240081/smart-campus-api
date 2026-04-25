package com.smartcampus.exception.mapper;

import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.exception.RoomNotEmptyException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps {@link RoomNotEmptyException} → HTTP 409 Conflict.
 * Triggered when a client attempts to DELETE a room that still has sensors.
 */
@Provider
public class RoomNotEmptyExceptionMapper
        implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        ErrorResponse body = new ErrorResponse(
            409, "CONFLICT", ex.getMessage(),
            "Remove or reassign all sensors in room '" + ex.getRoomId()
                + "' before deleting it.");
        return Response.status(Response.Status.CONFLICT)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body).build();
    }
}
