package game;

import java.io.Serializable;

import game.enums.Owner;
import game.enums.TransportType;
import game.ids.PlayerID;
import game.struct.TileDesc;

public abstract class AbstractTile implements Serializable 
{

	private static final long serialVersionUID = 3975933183704216840L;

	private int owner; // Was m1
	public int height;
	
	public AbstractTile() {
		owner = Owner.OWNER_NONE;
		height = 0;
	}
	
	
	
	public boolean isClear() {
		return false; // by default is not clear
	}
	
	
	
	public PlayerID getOwner() {
		return PlayerID.get(owner);
	}

	public void setOwner(PlayerID o) {
		owner = o.id;		
	}

	@Deprecated
	public void setOwner(int o) {
		owner = o;		
	}
	
	// -------------------------------------------------------------------
	// Tile type specific ops - main entry points
	// -------------------------------------------------------------------

	public abstract int GetTileTrackStatus( AbstractTileIndex ti, TransportType mode );
	public abstract void ChangeTileOwner( AbstractTileIndex ti, int old_player, int new_player );
	public abstract AcceptedCargo GetAcceptedCargo( AbstractTileIndex ti );
	public abstract void AnimateTile( AbstractTileIndex ti );
	public abstract void ClickTile( AbstractTileIndex ti );
	public abstract void DrawTile(AbstractTileIndex ti, TileInfo tInfo );
	public abstract TileDesc GetTileDesc( AbstractTileIndex ti );

	
}
