package com.dzavalishin.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Single line user input parser implementation
 */
public class SingleLineUserInput implements UserInput {
    private final String inputString;

    public SingleLineUserInput(String inputString) {
        this.inputString = inputString;
    }

    @Override
    public String getRawInput() {
        return inputString;
    }

    @Override
    public Stream<String> words() {
        if (!inputString.startsWith("#")) {
            return Arrays.stream(inputString.split(" "));
        }
        return Stream.empty();
    }

    @Override
    public List<String> parameters() {
        List<String> result = new ArrayList<>();
        StringBuilder parameter = new StringBuilder();
        boolean quotedParameter = false;
        for (String w: words().skip(1).collect(Collectors.toList())) {
            if (w.startsWith("\"")) {
                parameter = new StringBuilder(w.substring(1) + " ");
                quotedParameter = true;
                if (w.endsWith("\"")) {
                    result.add(parameter.substring(0, parameter.length() - 2));
                    quotedParameter = false;
                }
            } else {
                if (!quotedParameter) {
                   result.add(w);
                } else if (w.endsWith("\"")) {
                    result.add(parameter.append(" ").append(w, 0, w.length() - 1).toString());
                    quotedParameter = false;
                } else {
                    parameter.append(" ").append(w);
                }
            }
        }
        return result;
    }
}
