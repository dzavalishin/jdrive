package game;

import java.io.Serializable;

import game.enums.TransportType;
import game.ids.PlayerID;
import game.struct.TileDesc;

public abstract class AbstractTileIndex implements Comparable<AbstractTileIndex>, Serializable 
{

	private static final long serialVersionUID = 7916111916243111518L;

	protected int tile;
	
	/** static  TileIndex TileXY(int x, int y)
	 * 
	 * @param x
	 * @param y
	 */
	public AbstractTileIndex(int x, int y)
	{
		tile = (y * Global.MapSizeX()) + x;
		assert( tile >= 0 ); 
		// TODO assert < max
	}

	public AbstractTileIndex(AbstractTileIndex src)
	{
		tile = src.tile;
	}
	
	public AbstractTileIndex(int tile)
	{
		this.tile = tile;
	}
	
	
	
	public AbstractTile me() { return Global.gs._m[tile]; }
	
	public int getTileIndex() {
		return tile;
	}

	// -------------------------------------------------------------------
	// Coordinates
	// -------------------------------------------------------------------
	
	public int TileX()
	{
		return tile & Global.MapMaxX();
	}

	public int getX()
	{
		return tile & Global.MapMaxX();
	}

	public int TileY()
	{
		return tile >> Global.MapLogX();
	}

	public int getY()
	{
		return tile >> Global.MapLogX();
	}

	
	
	// -------------------------------------------------------------------
	// Owner
	// -------------------------------------------------------------------
	
	public PlayerID GetTileOwner()
	{
		//assert(tile < MapSize());
		//assert(!IsTileType(TileTypes.MP_HOUSE));
		//assert(!IsTileType(TileTypes.MP_VOID));
		//assert(!IsTileType(TileTypes.MP_INDUSTRY));

		//return new Owner(Global.gs._m[tile].m1);
		//return PlayerID.get(Global.gs._m[tile].owner);
		
		return me().getOwner();
	}

	public void SetTileOwner(PlayerID owner)
	{
		//assert(tile < MapSize());
		//assert(!IsTileType(TileTypes.MP_HOUSE));
		//assert(!IsTileType(TileTypes.MP_VOID));
		//assert(!IsTileType(TileTypes.MP_INDUSTRY));

		//Global.gs._m[tile].owner = owner.id;
		me().setOwner(owner);
	}

	@Deprecated
	public void SetTileOwner(int owner)
	{
		//assert(tile < MapSize());
		//assert(!IsTileType(TileTypes.MP_HOUSE));
		//assert(!IsTileType(TileTypes.MP_VOID));
		//assert(!IsTileType(TileTypes.MP_INDUSTRY));

		//Global.gs._m[tile].owner = owner;
		me().setOwner(owner);
	}


	// -------------------------------------------------------------------
	// Object
	// -------------------------------------------------------------------
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractTileIndex) {
			AbstractTileIndex him = (AbstractTileIndex) obj;
			return him.tile == tile;
		}
		return super.equals(obj);
	}


	@Override
	public int compareTo(AbstractTileIndex o) {
		return this.tile - o.tile;
	}

	@Override
	public String toString() {
		return String.format("%d.%d", getX(), getY() );
	}

	@Override
	public int hashCode() {
		return tile;
	}
	
	
	// -------------------------------------------------------------------
	// Tile type specific ops - main entry points
	// -------------------------------------------------------------------

	
	public int GetTileTrackStatus(TransportType mode)
	{
		return me().GetTileTrackStatus(this, mode);
	}

	public void ChangeTileOwner(int old_player, int new_player)
	{
		me().ChangeTileOwner(this, old_player, new_player);
	}

	public AcceptedCargo GetAcceptedCargo()
	{
		return me().GetAcceptedCargo(this);
	}

	public void AnimateTile()
	{
		me().AnimateTile(this);
	}

	public void ClickTile()
	{
		me().ClickTile(this);
	}

	public void DrawTile(TileInfo ti)
	{
		me().DrawTile(this,ti);
	}

	public TileDesc GetTileDesc()
	{
		return me().GetTileDesc(this);
	}
	
	
}
