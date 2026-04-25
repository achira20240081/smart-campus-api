package com.smartcampus.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Cross-cutting HTTP logging filter.
 *
 * Implements both ContainerRequestFilter (intercepts the incoming request BEFORE
 * it reaches the resource method) and ContainerResponseFilter (intercepts the
 * response AFTER the resource method returns).
 *
 * @Provider registers both filter interfaces automatically with the JAX-RS
 * runtime — no explicit registration is needed in ApplicationConfig.
 *
 * -----------------------------------------------------------------------
 * WHY FILTERS INSTEAD OF LOGGING INSIDE RESOURCE METHODS?
 * -----------------------------------------------------------------------
 * Placing Logger.info() calls inside each resource method would violate
 * several software-engineering principles:
 *
 *  1. DRY (Don't Repeat Yourself) — logging logic is defined ONCE here and
 *     automatically applied to EVERY endpoint. Adding a new endpoint
 *     automatically benefits from logging without any extra code.
 *
 *  2. Separation of Concerns — resource classes focus on business logic;
 *     infrastructure concerns (logging, authentication, CORS, rate-limiting)
 *     live in dedicated filter classes.
 *
 *  3. Consistency — every request/response is logged with the same format.
 *     Manual logging leads to inconsistencies as the codebase grows.
 *
 *  4. Maintainability — changing the log format (e.g., adding a correlation
 *     ID or timing information) requires editing one class, not dozens of
 *     resource methods.
 *
 *  5. Composability — filters can be chained, given execution priority via
 *     @Priority, and conditionally applied via @NameBinding annotations,
 *     without touching any resource code.
 * -----------------------------------------------------------------------
 */
@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(ApiLoggingFilter.class.getName());

    /**
     * Pre-matching filter — invoked BEFORE the request reaches the resource method.
     * Logs the HTTP method and full request URI for audit / debugging.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOG.info(String.format(
            "[REQUEST]  %-6s %s",
            requestContext.getMethod(),
            requestContext.getUriInfo().getRequestUri()
        ));
    }

    /**
     * Post-matching filter — invoked AFTER the resource method returns and the
     * response entity has been set. Logs the final HTTP status code and reason phrase.
     */
    @Override
    public void filter(ContainerRequestContext  requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOG.info(String.format(
            "[RESPONSE] %-6s %s → HTTP %d %s",
            requestContext.getMethod(),
            requestContext.getUriInfo().getRequestUri(),
            responseContext.getStatus(),
            responseContext.getStatusInfo().getReasonPhrase()
        ));
    }
}
