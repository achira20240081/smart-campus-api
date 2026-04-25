package com.smartcampus.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application configuration class.
 *
 * @ApplicationPath("/api/v1") sets the base URI segment for ALL endpoints.
 * Placing this in the dedicated {@code config} package follows the separation
 * of concerns principle: infrastructure/bootstrap code is kept completely
 * separate from business logic (service), data access (repository), and
 * presentation (resource) layers.
 *
 * By extending {@link Application} with no method overrides, Jersey performs
 * a full classpath scan and auto-registers every class annotated with
 * {@code @Path}, {@code @Provider}, or {@code @ApplicationPath}.
 *
 * -----------------------------------------------------------------------
 * JAX-RS LIFECYCLE — Singleton vs Per-Request
 * -----------------------------------------------------------------------
 * Default lifecycle: JAX-RS creates a NEW resource instance per HTTP request.
 *   + Thread-safe automatically — no shared mutable state between requests.
 *   – Small instantiation cost each request (negligible for stateless classes).
 *
 * Singleton lifecycle: ONE instance serves all requests.
 *   + Zero instantiation overhead.
 *   – Developer must ensure thread-safety manually (e.g., ConcurrentHashMap).
 *
 * This project uses the default per-request lifecycle for resource classes
 * and a manual Singleton pattern for the DataStore / Repositories.
 * -----------------------------------------------------------------------
 */
@ApplicationPath("/api/v1")
public class ApplicationConfig extends Application {
    // No overrides — Jersey auto-scans the classpath for all annotated classes.
}
