package com.dzavalishin.ifaces;

import java.io.Serializable;
import java.util.function.Consumer;

import com.dzavalishin.xui.Window;

@FunctionalInterface
public interface OnButtonClick extends Consumer<Window>, Serializable {} 
