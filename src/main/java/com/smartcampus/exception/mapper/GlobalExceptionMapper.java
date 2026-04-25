package com.smartcampus.exception.mapper;

import com.smartcampus.exception.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global safety-net — catches every {@link Throwable} not handled by a more
 * specific mapper and returns a safe, opaque HTTP 500 response.
 *
 * Security rationale: full stack traces are logged SERVER-SIDE only.
 * They are NEVER exposed to API clients because they reveal internal paths,
 * library versions, class structures, and potential exploit vectors.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Full details go to server log only — never to the HTTP response body
        LOG.log(Level.SEVERE, "Unhandled exception caught by global safety-net mapper", ex);

        ErrorResponse body = new ErrorResponse(
            500, "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. The incident has been logged.",
            "Please contact the API administrator if the problem persists.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body).build();
    }
}
