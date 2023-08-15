package com.dzavalishin.xui;

import com.dzavalishin.game.Hal;
import com.dzavalishin.util.Pixel;

public class DrawPixelInfo 
{
	public Pixel dst_ptr; // Smart pointer
	
	public int left, top;

	public int width;
	public int height;
	public int pitch;
	public int zoom;
	
	public DrawPixelInfo(DrawPixelInfo src) {
		assignFrom(src);
	}

	public DrawPixelInfo() {
		// TODO require at least dst_ptr
	}

	public void assignFrom(DrawPixelInfo dpi) 
	{
		if(dpi.dst_ptr == null) 
			dst_ptr = null;
		else 
			dst_ptr = new Pixel(dpi.dst_ptr); 
		
		left = dpi.left; 
		top = dpi.top; 
		width = dpi.width; 
		height = dpi.height;
		pitch = dpi.pitch;
		zoom = dpi.zoom;
	}

	
	static boolean FillDrawPixelInfo(DrawPixelInfo n,  DrawPixelInfo o, int left, int top, int width, int height)
	{
		int t;

		if (o == null) o = Hal._cur_dpi;

		n.zoom = 0;

		assert(width > 0);
		assert(height > 0);

		n.left = 0;
		if ((left -= o.left) < 0) {
			width += left;
			if (width < 0) return false;
			n.left = -left;
			left = 0;
		}

		if ((t=width + left - o.width) > 0) {
			width -= t;
			if (width < 0) return false;
		}
		n.width = width;

		n.top = 0;
		if ((top -= o.top) < 0) {
			height += top;
			if (height < 0) return false;
			n.top = -top;
			top = 0;
		}

		n.pitch = o.pitch;
		//n.dst_ptr = o.dst_ptr + left + top * o.pitch;
		n.dst_ptr = new Pixel(o.dst_ptr, left + top * o.pitch);

		if ((t=height + top - o.height) > 0) {
			height -= t;
			if (height < 0) return false;
		}
		n.height = height;

		return true;
	}

	public void init(int width2, int height2, byte[] screen) 
	{
		dst_ptr = new Pixel( screen );
		height = height2;
		width = width2;
		pitch = width2 * 1;
		left = 0;
		top = 0;
		zoom = 0;
	}

	public void setScreen(byte[] screen) 
	{
		dst_ptr = new Pixel( screen );
	}
	
}
