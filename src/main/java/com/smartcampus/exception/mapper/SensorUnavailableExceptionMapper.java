package com.smartcampus.exception.mapper;

import com.smartcampus.exception.ErrorResponse;
import com.smartcampus.exception.SensorUnavailableException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps {@link SensorUnavailableException} → HTTP 403 Forbidden.
 * Triggered when a POST reading is attempted on a non-ACTIVE sensor.
 */
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        ErrorResponse body = new ErrorResponse(
            403, "FORBIDDEN", ex.getMessage(),
            "Set sensor status to 'ACTIVE' before submitting readings. Current: "
                + ex.getStatus());
        return Response.status(Response.Status.FORBIDDEN)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body).build();
    }
}
