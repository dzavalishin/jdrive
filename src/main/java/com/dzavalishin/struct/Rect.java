package com.dzavalishin.struct;

import com.dzavalishin.xui.DrawPixelInfo;

public class Rect 
{
	public int left;
	public int top;
	public int right;
	public int bottom;

	public Rect(DrawPixelInfo dpi) {
		left = dpi.left;
		top = dpi.top;
		right = left + dpi.width;
		bottom = top + dpi.height;
	}

	public Rect() {
		left =
		top =
		right =
		bottom = 0;
	}

}
