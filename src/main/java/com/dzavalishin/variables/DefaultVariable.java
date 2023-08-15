package com.dzavalishin.variables;

import com.dzavalishin.parameters.Type;

import java.util.function.Supplier;

/**
 * Default console variable implementation
 */
public class DefaultVariable implements Variable {
    private final String name;
    private final Type type;
    private final String description;
    private final Supplier<String> rawValueSupplier;

    /**
     * ctor
     *
     * @param name variable name
     * @param type variable type
     * @param description variable description
     * @param rawValueSupplier variable supplier
     */
    public DefaultVariable(String name,
                           Type type,
                           String description,
                           Supplier<String> rawValueSupplier) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.rawValueSupplier = rawValueSupplier;
    }

    /**
     * ctor
     *
     * @param name variable name
     * @param type variable type
     * @param description variable description
     * @param rawValue variable rawValue
     */
    public DefaultVariable(String name,
                           Type type,
                           String description,
                           String rawValue) {
        this(name, type, description, () -> rawValue);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String rawValue() {
        return rawValueSupplier.get();
    }
}
