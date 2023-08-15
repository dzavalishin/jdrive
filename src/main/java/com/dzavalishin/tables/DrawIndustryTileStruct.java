package com.dzavalishin.tables;

public class DrawIndustryTileStruct {
	public final int sprite_1;
	public final int sprite_2;

	public final int subtile_x;
	public final int subtile_y;
	public final int width;
	public final int height;
	public final int dz;
	public final int proc;

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