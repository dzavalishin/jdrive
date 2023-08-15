package com.dzavalishin.xui;

import java.io.Serializable;

import com.dzavalishin.struct.Point;
import com.dzavalishin.util.BitOps;

public class Scrollbar implements Serializable
{
	private int count;
	private int cap;
	int pos;
	
	public Scrollbar() 
	{
		count = cap = pos = 0;
	}
	
	public int getCount() {		return count;	}
	public int getCap() {		return cap;	}
	public int getPos() {		return pos;	}

	
	public void setCap(int cap) {
		this.cap = cap;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Point hittest(int top, int bottom)
	{
		top += 10;
		bottom -= 9;

		int height = (bottom - top);
		int tcount = this.count;
		int tcap = this.cap;
		int tpos = this.pos;

		if (tcount != 0) top += height * tpos / tcount;

		if (tcap > tcount) tcap = tcount;
		if (tcount != 0) bottom -= (tcount - tpos - tcap) * height / tcount;

		return new Point(top, bottom - 1);
	}

	public void up() {
		if (pos > 0) pos--;		
	}

	public void down() {
		if ((pos + cap) < count)
			pos++;		
	}

	/**
	 * Scroll with mouse wheel
	 * @param wheel
	 * @return true if changed
	 */
	public boolean wheel(int wheel) {
		if (count > cap) {
			int newpos = BitOps.clamp(pos + wheel, 0, count - cap);
			if (newpos != pos) {
				pos = newpos;
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param cap
	 * @param count
	 */
	public void setUp(int cap, int count) {
		this.cap = cap;
		this.count = count;		
	}

	public void updateCount(int num) 
	{
		count = num;
		num -= cap;
		if (num < 0) num = 0;
		if (num < pos) pos = num;
	}

	public void setPos(int i) { pos = i; }

	public void decrementPos() { pos--; }
	
	
}