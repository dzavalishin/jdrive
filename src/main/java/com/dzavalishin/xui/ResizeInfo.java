package com.dzavalishin.xui;

import java.io.Serializable;

public class ResizeInfo implements Serializable
{
	public int width; /* Minimum width and height */
	public int height;

	public int step_width; /* In how big steps the width and height go */
	public int step_height;
}