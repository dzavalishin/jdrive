package com.dzavalishin.parameters;

/**
 * Command parameter interface
 */
public interface Parameter {
    /**
     * returns parameter description
     *
     * @return parameter description
     */
    ParameterDescription getDescription();

    /**
     * returns parameter type
     *
     * @return parameter type
     */
    Type getType();
}
