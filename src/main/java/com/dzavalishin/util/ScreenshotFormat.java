package com.dzavalishin.util;

abstract class ScreenshotFormat {
	final String name;
	final String extension;
	
	protected ScreenshotFormat(String name, String extension) {
		this.name = name;
		this.extension = extension;
	}

	public abstract boolean proc(String name, ScreenshotCallback getter, Object userData, 
			int width, int height, int i, Colour[] curPalette);
}