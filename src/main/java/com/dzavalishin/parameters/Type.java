package com.dzavalishin.parameters;

/**
 * Parameter's and value's types
 */
public enum Type {
    STRING  (new StringValueResolver()),
    INTEGER (new IntegerValueResolver()),
    BOOLEAN (new BooleanValueResolver()),
    ;

    private final ValueResolver resolver;

    Type(ValueResolver valueResolver) {
        this.resolver = valueResolver;
    }

    /**
     * resolve parameter
     *
     * @param value raw parameter value
     * @param <T> parameter type
     * @return parsed parameter value
     */
    public <T> T resolve(String value) {
        return resolver.resolve(value);
    }
}
