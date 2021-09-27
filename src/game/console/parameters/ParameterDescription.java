package game.console.parameters;

/**
 * Parameter description interface
 */
public interface ParameterDescription {
    /**
     * returns parameter name
     *
     * @return parameter name
     */
    String getName();

    /**
     * returns parameter documentation
     *
     * @return parameter documentation
     */
    String getDocumentation();
}
