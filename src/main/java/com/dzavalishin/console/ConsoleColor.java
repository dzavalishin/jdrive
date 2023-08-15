package com.dzavalishin.console;

import java.util.Arrays;
import java.util.Optional;

/**
 * Console colors
 */
public enum ConsoleColor {
    WHITE(1),
    RED(3),
    BLUE(13),
    ORANGE(2),
    BROWN(5),
    ;

    private final int colorCode;

    ConsoleColor(int colorCode) {
        this.colorCode = colorCode;
    }

    /**
     * @return color code
     */
    public int getColorCode() {
        return colorCode;
    }

    /**
     * find console color by color code
     *
     * @return console color
     */
    public static Optional<ConsoleColor> fromInteger(int colorCode) {
        return Arrays.stream(ConsoleColor.values()).filter(c -> c.getColorCode() == colorCode).findFirst();
    }
}
