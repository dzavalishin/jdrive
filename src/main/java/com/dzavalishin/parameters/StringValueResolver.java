package com.dzavalishin.parameters;

/**
 * Stub converter from String to String
 */
public class StringValueResolver implements ValueResolver {
    @Override
    public String resolve(String rawValue) {
        return rawValue;
    }
}
