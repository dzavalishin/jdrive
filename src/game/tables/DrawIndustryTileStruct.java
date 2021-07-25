package game.tables;

public class DrawIndustryTileStruct {
	public int sprite_1;
	public int sprite_2;

	public int subtile_x;
	public int subtile_y;
	public int width;
	public int height;
	public int dz;
	public int proc;

	// 	#define M(s1, s2, sx, sy, w, h, dz, p) { s1, s2, sx, sy, w - 1, h - 1, dz, p }
	public DrawIndustryTileStruct(int s1, int s2, int sx, int sy, int w, int h, int dz, int p) 
	{
		 sprite_1 = s1;
		 sprite_2 = s2;

		 subtile_x = sx;
		 subtile_y = sy;
		 width = w-1;
		 height = h-1;
		 this.dz = dz;
		 proc = p;
	}

}