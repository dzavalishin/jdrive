package game.ifaces;

import java.io.Serializable;
import java.util.function.Consumer;

import game.xui.Window;

@FunctionalInterface
public interface OnButtonClick extends Consumer<Window>, Serializable {} 
