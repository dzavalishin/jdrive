package com.dzavalishin.struct;

import com.dzavalishin.game.Landscape;
import com.dzavalishin.game.TileIndex;
import com.dzavalishin.game.TileInfo;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.xui.ViewPort;

/**
 * 
 * 
 * @implNote Used in hash map, must have corresp methods
 * 
 * @author dz
 *
 */
public class Point implements Comparable<Point> {
	public int x;
	public int y;

	public Point(int i, int j) {
		x = i;
		y = j;
	}


	public Point(Point p) {
		x = p.x;
		y = p.y;
	}


	public static Point RemapCoords(int x, int y, int z) {
		//#if !defined(NEW_ROTATION)
		//pt.x = ;
		//pt.y = ;
		/*#else
		pt.x = (x + y) * 2;
		pt.y = x - y - z;
		#endif*/
		return new Point((y - x) * 2, y + x - z);
	}

	public static Point RemapCoords2(int x, int y)
	{
		return RemapCoords(x, y, Landscape.GetSlopeZ(x, y));
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Point) {
			Point p = (Point) obj;
			return p.x == x && p.y == y;
		}
		return super.equals(obj);
	}

	@Override
	public int compareTo(Point o) 
	{
		if( y < o.y ) return -1;
		if( y > o.y ) return 1;
		if( x < o.x ) return -1;
		if( x > o.x ) return 1;
		return 0;
	}

	@Override
	public int hashCode() {
		
		//return x + y; diagonal ones will have same code
		return x + (y << 8);
	}


	/**
	 * Remap tile index into screen x,y
	 * @param tile to find coordinates for
	 * @return Point of tile center in screen coordinates.
	 */
	public static Point RemapCoords(TileIndex tile)
	{
		Point pt = Point.RemapCoords(
				tile.getX()* TileInfo.TILE_SIZE,
				tile.getY()*TileInfo.TILE_SIZE, 
				tile.GetTileZ() );
		return new Point(pt.x, pt.y+12);
	}

	
	@Override
	public String toString() {		
		return String.format("%d.%d", x, y);
	}


	/**
	 * TODO make viewport to be child of ScreenSquare, replace ViewPort type with ScreenSquare 
	 * @param vp 
	 * @return true if this point coordinates are inside this viewport.
	 */
	public boolean isInside(ViewPort vp) 
	{
		return BitOps.IS_INSIDE_1D(x, vp.getVirtual_left(), vp.getVirtual_width()) &&
				BitOps.IS_INSIDE_1D(y, vp.getVirtual_top(), vp.getVirtual_height()) ;
	}
	
}
