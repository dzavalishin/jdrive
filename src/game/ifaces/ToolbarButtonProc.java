package game.ifaces;

import java.util.function.Consumer;

import game.xui.Window;

@FunctionalInterface
interface ToolbarButtonProc extends Consumer<Window> {}