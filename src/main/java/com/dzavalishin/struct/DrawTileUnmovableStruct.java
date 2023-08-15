package com.dzavalishin.struct;

public class DrawTileUnmovableStruct 
{
	public final int image;
	public final int subcoord_x;
	public final int subcoord_y;
	public final int width;
	public final int height;
	public final int z_size;
	public final int unused;

	public DrawTileUnmovableStruct(int i, int j, int k, int l, int m, int n, int o) {
		image = i;
		subcoord_x = j;
		subcoord_y = k;
		width = l;
		height = m;
		z_size = n;
		unused = o;
	}
	
}
