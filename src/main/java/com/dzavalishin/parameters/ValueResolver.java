package com.dzavalishin.parameters;

/**
 * Converter to certain type interface
 */
public interface ValueResolver {
    /**
     * resolve value from string to certain type
     *
     * @param rawValue raw value
     * @param <T> result type
     * @return resolved value
     */
    <T> T resolve(String rawValue);
}
