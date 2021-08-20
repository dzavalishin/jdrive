package game;

import java.io.Serializable;

public class ViewportSign implements Serializable 
{
	int left;
	int top;
	int width_1, width_2;
	public int getWidth_2() {
		return width_2;
	}
}
