package com.dzavalishin.variables;

import com.dzavalishin.parameters.Type;

/**
 * Console variable interface
 */
public interface Variable {
    /**
     * @return variable name
     */
    String getName();

    /**
     * @return variable description
     */
    String getDescription();

    /**
     * @return variable type
     */
    Type getType();

    /**
     * @return variable raw value
     */
    String rawValue();

    /**
     * returns variable value converted to type
     *
     * @param <T> variable type
     * @return variable value
     */
    default <T> T getValue() {
        return getType().resolve(rawValue());
    }
}
