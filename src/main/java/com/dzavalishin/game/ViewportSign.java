package com.dzavalishin.game;

import java.io.Serializable;

import com.dzavalishin.struct.Rect;
import com.dzavalishin.struct.StringSpriteToDraw;
import com.dzavalishin.xui.ViewPort;

public class ViewportSign implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int left;
	private int top;
	private int width_1;
	private int width_2;
	

	public StringSpriteToDraw draw(int str, int p1, int p2, int p3) {
		return ViewPort.AddStringToDraw(left + 1, top + 1, str, p1, p2, p3);		
	}

	public StringSpriteToDraw draw(int x, int y, int str, int p1, int p2, int p3) {
		return ViewPort.AddStringToDraw(left + x, top + y, str, p1, p2, p3);		
	}

	public int getTop() {		return top;	}
	public void setTop(int top) {		this.top = top;	}

	public int getLeft() {		return left;	}
	public void setLeft(int left) {		this.left = left;	}

	public int getWidth_1() {		return width_1;	}
	public int getWidth_2() {		return width_2;	}

	public void setWidth_1(int width_1) {		this.width_1 = width_1;	}
	public void setWidth_2(int width_2) {		this.width_2 = width_2;	}


	/**
	 * 
	 * @param rect to compare coordinates with
	 * @param w our width
	 * @param h our height
	 * @return True if intersects with given rectangle
	 */
	public boolean intersects(Rect rect, int w, int h) 
	{
		return 						
				rect.bottom > top &&
				rect.top < top + h &&
				rect.right > left &&
				rect.left < left + w;
	}
	
	public boolean pointIn(int x, int y, int w, int h) {
		return 
				y >= top &&
				y < top + h &&
				x >= left &&
				x < left + w
				;
	}

}
