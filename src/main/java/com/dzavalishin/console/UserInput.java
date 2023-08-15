package com.dzavalishin.console;

import java.util.List;
import java.util.stream.Stream;

/**
 * Interface for parse user input
 */
public interface UserInput {
    /**
     * returns raw user input
     *
     * @return raw user input
     */
    String getRawInput();

    /**
     * returns words of user input
     *
     * @return words of user input
     */
    Stream<String> words();

    /**
     * returns input parameters
     *
     * <pre>Examples:
     * help 1 -> ["1"]
     * alias h1 "help 1" -> ["h1", "help 1"]
     * </pre>
     *
     * @return input parameters
     */
    List<String> parameters();
}
