package game.console;

/**
 * Console interface
 */
public interface Console {
    /**
     * print message
     *
     * @param str message
     * @param color massage color
     */
    void println(String str, ConsoleColor color);

    /**
     * print debug message
     *
     * @param str message
     */
    void debug(String str);

    /**
     * init console
     */
    void init();

    /**
     * close console
     */
    void close();

    /**
     * resize console
     */
    void resize();

    /**
     * switch console state
     */
    void switchState();

    /**
     * @return true if console mode is full size
     */
    boolean isFullSize();
}
