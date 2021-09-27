package game.console.parameters;

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
