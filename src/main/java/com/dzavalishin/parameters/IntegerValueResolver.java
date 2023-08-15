package com.dzavalishin.parameters;

/**
 * Converter from String to Integer
 */
public class IntegerValueResolver implements ValueResolver {
    @Override
    public Integer resolve(String rawValue) {
        if (rawValue.startsWith("0x"))
            return Integer.parseInt(rawValue.substring(2), 16);
        return Integer.parseInt(rawValue);
    }
}
