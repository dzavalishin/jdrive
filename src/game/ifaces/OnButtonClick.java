package game.ifaces;

import java.util.function.Consumer;

import game.xui.Window;

@FunctionalInterface
public interface OnButtonClick extends Consumer<Window> {} 
