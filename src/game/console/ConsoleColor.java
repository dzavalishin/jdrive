package game.console;

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
}
