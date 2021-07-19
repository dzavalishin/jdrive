package game;

public class Point {
	int x,y;

	public Point(int i, int j) {
		x = i;
		y = j;
	}

	public static Point RemapCoords(int x, int y, int z) {
		Point pt = new Point((y - x) * 2, y + x - z);
		//#if !defined(NEW_ROTATION)
		//pt.x = ;
		//pt.y = ;
		/*#else
		pt.x = (x + y) * 2;
		pt.y = x - y - z;
		#endif*/
		return pt;
	}
	
	public static Point RemapCoords2(int x, int y)
	{
		return RemapCoords(x, y, Landscape.GetSlopeZ(x, y));
	}

}
