package com.dzavalishin.parameters;

/**
 * Default parameter implementation
 */
public class DefaultParameter implements Parameter {
    private final ParameterDescription description;
    private final Type type;

    /**
     * ctor
     *
     * @param description parameter description
     * @param type parameter type
     */
    public DefaultParameter(ParameterDescription description, Type type) {
        this.description = description;
        this.type = type;
    }

    @Override
    public ParameterDescription getDescription() {
        return description;
    }

    @Override
    public Type getType() {
        return type;
    }
}
