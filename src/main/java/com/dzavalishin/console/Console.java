package com.dzavalishin.console;

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
     * print message
     *
     * @param str message
     * @param color massage color
     */
    void println(String str, int color);

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
     * clear console
     */
    void clear();

    /**
     * @return true if console mode is full size
     */
    boolean isFullSize();
    
	/**
	 * Execute a given command passed to us. First chop it up into
	 * individual tokens (seperated by spaces), then execute it if possible
	 * @param cmdstr string to be parsed and executed
	 */
	void IConsoleCmdExec(final String cmdstr);

}
