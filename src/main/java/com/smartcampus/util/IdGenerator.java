package com.smartcampus.util;

import java.util.UUID;

/**
 * Utility class for generating unique identifiers.
 *
 * WHY A DEDICATED UTIL CLASS?
 * ----------------------------
 * Calling {@code UUID.randomUUID().toString()} inline throughout the codebase
 * creates hidden coupling to a specific ID generation strategy. Centralising
 * it here means:
 *
 *  1. Strategy is swappable — e.g., switching from UUID v4 to UUID v7 (time-
 *     ordered) or a custom snowflake ID requires changing only this class.
 *  2. Testability — tests can mock or spy on IdGenerator to produce predictable
 *     IDs instead of random UUIDs, making assertions deterministic.
 *  3. Readability — {@code IdGenerator.newId()} is more expressive than an
 *     inline UUID call scattered across multiple service classes.
 *
 * The class is final with a private constructor to prevent instantiation
 * (pure static utility — no state, no inheritance).
 */
public final class IdGenerator {

    private IdGenerator() {
        // Utility class — do not instantiate
    }

    /**
     * Generates a new random UUID string.
     *
     * @return a 36-character UUID string, e.g. "550e8400-e29b-41d4-a716-446655440000"
     */
    public static String newId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a compact UUID without hyphens (useful for URL path segments).
     *
     * @return a 32-character hex string, e.g. "550e8400e29b41d4a716446655440000"
     */
    public static String newCompactId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
