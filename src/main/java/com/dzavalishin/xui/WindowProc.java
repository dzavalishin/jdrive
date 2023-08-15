package com.dzavalishin.xui;

import java.io.Serializable;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface WindowProc extends BiConsumer<Window,WindowEvent>, Serializable {}
