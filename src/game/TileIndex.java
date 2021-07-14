package game;

public class TileIndex {
	private int tile;
	
	/** static inline TileIndex TileXY(uint x, uint y)
	 * 
	 * @param x
	 * @param y
	 */
	public TileIndex(int x, int y)
	{
		tile = (y * Global.MapSizeX()) + x;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TileIndex) {
			TileIndex him = (TileIndex) obj;
			return him.tile == tile;
		}
		return super.equals(obj);
	}
	
	public TileIndex(int tile)
	{
		this.tile = tile;
	}
	
	public static TileIndex INVALID_TILE = new TileIndex(-1);
	
	int TileX()
	{
		return tile & Global.MapMaxX();
	}

	int TileY()
	{
		return tile >> Global.MapLogX();
	}
	
	
	/* Approximation of the length of a straight track, relative to a diagonal
	 * track (ie the size of a tile side). #defined instead of const so it can
	 * stay integer. (no runtime float operations) Is this needed?
	 * Watch out! There are _no_ brackets around here, to prevent intermediate
	 * rounding! Be careful when using this!
	 * This value should be sqrt(2)/2 ~ 0.7071 */
	public static int STRAIGHT_TRACK_LENGTH = 7071/10000;
	
	
}
