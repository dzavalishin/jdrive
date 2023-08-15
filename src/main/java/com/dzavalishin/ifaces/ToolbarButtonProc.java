package com.dzavalishin.ifaces;

import java.util.function.Consumer;

import com.dzavalishin.xui.Window;

@FunctionalInterface
public
interface ToolbarButtonProc extends Consumer<Window> {}