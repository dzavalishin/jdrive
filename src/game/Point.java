package game;

public class Point {
	int x,y;

	public static Point RemapCoords(int x, int y, int z) {
		Point pt = new Point();
		//#if !defined(NEW_ROTATION)
		pt.x = (y - x) * 2;
		pt.y = y + x - z;
		/*#else
		pt.x = (x + y) * 2;
		pt.y = x - y - z;
		#endif*/
		return pt;
	}
}
