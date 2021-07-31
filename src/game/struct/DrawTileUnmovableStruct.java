package game.struct;

public class DrawTileUnmovableStruct 
{
	public int image;
	public int subcoord_x;
	public int subcoord_y;
	public int width;
	public int height;
	public int z_size;
	public int unused;

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
