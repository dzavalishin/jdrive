package com.dzavalishin.console;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Console factory
 */
public enum ConsoleFactory {
    INSTANCE;

    private final AtomicReference<Console> currentConsole = new AtomicReference<>();

    /**
     * returns console, makes it if console not exist
     *
     * @return console object
     */
    public synchronized Console getConsole() {
        if (currentConsole.get() == null) {
            Console console = new DefaultConsole();
            console.init();
            currentConsole.set(console);
        }
        return currentConsole.get();
    }

    /**
     * returns current console object
     *
     * @return current console object
     */
    public synchronized Optional<Console> getCurrentConsole() {
        return Optional.ofNullable(currentConsole.get());
    }

    /**
     * close current console
     */
    public synchronized void closeConsole() {
        getCurrentConsole().ifPresent(c -> {
            c.close();
        });
    }
}
