package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Application entry point.
 *
 * Starts an embedded Grizzly HTTP server that hosts the JAX-RS application.
 * No external application server (Tomcat, WildFly, etc.) is required.
 *
 * Run with:
 *   mvn package -q
 *   java -jar target/smart-campus-api-1.0.0.jar
 *
 * Or directly via Maven exec plugin:
 *   mvn exec:java -Dexec.mainClass=com.smartcampus.Main
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    /** The base URI on which Grizzly will listen. */
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();

        LOG.info("=============================================================");
        LOG.info(" Smart Campus Sensor API started successfully");
        LOG.info(" Base URI : " + BASE_URI);
        LOG.info(" Discovery: GET " + BASE_URI);
        LOG.info(" Rooms    : GET " + BASE_URI + "rooms");
        LOG.info(" Sensors  : GET " + BASE_URI + "sensors");
        LOG.info(" Press CTRL+C to stop the server.");
        LOG.info("=============================================================");

        // Keep the main thread alive; Grizzly runs its own thread pool
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutting down Smart Campus API server...");
            server.shutdownNow();
        }));

        // Block indefinitely (interrupted only by CTRL+C or shutdown hook)
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Creates and starts the Grizzly HTTP server with the Jersey JAX-RS runtime.
     *
     * ResourceConfig is the programmatic equivalent of ApplicationConfig.
     * We use it here to:
     *   1. Register all classes in the com.smartcampus package (resources, filters, mappers).
     *   2. Register JacksonFeature so JSON serialisation/deserialisation works automatically.
     *
     * @return the running HttpServer instance
     */
    public static HttpServer startServer() {
        ResourceConfig config = new ResourceConfig()
            // Auto-scan and register all @Path, @Provider annotated classes
            .packages("com.smartcampus")
            // Enable Jackson JSON support
            .register(JacksonFeature.class);

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }
}
