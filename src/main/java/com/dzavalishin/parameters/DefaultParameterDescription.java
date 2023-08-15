package com.dzavalishin.parameters;

/**
 * Default {@link ParameterDescription} implementation
 */
public class DefaultParameterDescription implements ParameterDescription {
   private final String name;
   private final String description;

    /**
     * ctor
     *
     * @param name parameter name
     */
    public DefaultParameterDescription(String name) {
        this(name, name);
    }

    /**
     * ctor
     *
     * @param name parameter name
     * @param description parameter description
     */
    public DefaultParameterDescription(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDocumentation() {
        return description;
    }
}
