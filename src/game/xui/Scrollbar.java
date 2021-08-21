package game.xui;

import game.struct.Point;
import game.util.BitOps;

public class Scrollbar 
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

		if (count != 0) top += height * pos / count;

		if (cap > count) cap = count;
		if (count != 0) bottom -= (count - pos - cap) * height / count;

		Point pt = new Point(top, bottom - 1);
		return pt;
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
	
	
}