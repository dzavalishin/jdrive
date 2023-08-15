package com.dzavalishin.commands;

/**
 * Alias interface
 * <p>Aliases are like shortcuts for complex functions, variable assignments,
 * etc. You can use a simple alias to rename a longer command (eg 'lv' for
 * 'list_vars' for example), or concatenate more commands into one
 * (eg. 'ng' for 'load %A; unpause; debug_level 5').</p>
 * <p>Aliases can parse the arguments
 * given to them in the command line.
 * <pre>
 * - "%A - %Z" substitute arguments 1 t/m 26
 * - "%+" lists all parameters keeping them seperated
 * - "%!" also lists all parameters but presenting them to the aliased command as one argument
 * - ";" allows for combining commands (see example 'ng')</pre></p>
 */
public interface Alias {
    /**
      * @return alias name
     */
    String getName();

    /**
     * @return alias command
     */
    String getCommand();
}
