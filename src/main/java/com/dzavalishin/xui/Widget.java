package com.dzavalishin.xui;

import java.io.Serializable;

public class Widget implements Serializable 
{	
	private static final long serialVersionUID = 1L;
	
	final int type;
	final int resize_flag;
	int color;
	public int left;
	public int right;
	public int top;
	public int bottom;
	public int unkA; // Sprite or String id, depends on use
	//StringID tooltips;
	int tooltips;

	public Widget(
			int type,
			int resize_flag,
			int color,
			int left, int right, int top, int bottom,
			int unkA, 
			//StringID tooltips
			int tooltips
			) {

		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;

		this.unkA = unkA;
		this.tooltips = tooltips;
		
		this.type = type;
		this.resize_flag = resize_flag;
		this.color = color;
	}

	public Widget(Widget ww) {
		this.left        = ww.left;
		this.right       = ww.right;
		this.top         = ww.top;
		this.bottom      = ww.bottom;

		this.unkA        = ww.unkA;
		this.tooltips    = ww.tooltips;
		
		this.type        = ww.type;
		this.resize_flag = ww.resize_flag;
		this.color       = ww.color;
	}
}
