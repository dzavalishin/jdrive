package com.dzavalishin.parameters;

/**
 * Converter from String to Boolean
 */
public class BooleanValueResolver implements ValueResolver {
    @Override
    public Boolean resolve(String rawValue) {
        return Boolean.getBoolean(rawValue);
    }
}
