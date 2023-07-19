package game;

import java.io.Serializable;

import game.enums.Owner;
import game.enums.TileTypes;
import game.enums.TransportType;
import game.ids.PlayerID;
import game.struct.TileDesc;

/**
 * 
 * This class implements tile as it was in original game.
 * 
 * Must be killed sooner or later.
 * 
 * @author dz
 *
 */

public class Tile extends AbstractTile implements Serializable 
{

	private static final long serialVersionUID = -6829939704826964776L;
	
	//byte type_height;
	public int type;

	//public int m1; // owner?

	public int m2;

	//byte m3;
	//byte m4;
	//byte m5;
	//byte extra;

	// being bytes they're treated as negative too often
	public int m3;
	public int m4;
	public int m5;
	public int extra;
	
	public int anim; // m1 was used as animation status for some tiles, hence the replacement
	
	// TODO use DiaginalDirections instead
	public static final int DIAGDIR_NE  = 0;      /* Northeast, upper right on your monitor */
	public static final int DIAGDIR_SE  = 1;
	public static final int DIAGDIR_SW  = 2;
	public static final int DIAGDIR_NW  = 3;
	public static final int DIAGDIR_END = 4;
	public static final int INVALID_DIAGDIR = 0xFF;
	
	@Deprecated
	public byte get_type_height() {
		return (byte) (((type << 4) & 0xF0) | (height & 0x0F));
	}
	
	@Deprecated
	public void set_type_height(byte b) {
		type = b >> 4;
		height = b & 0xF;		
	}

	public Tile() {
		type        = TileTypes.MP_CLEAR.ordinal();
		//height      = 0;
		//m1          = Owner.OWNER_NONE;
		m2          = 0;
		m3          = 0;
		m4          = 0;
		m5          = 3;
		extra       = 0;
	}

	// -------------------------------------------------------------------
	// Tile type specific ops - main entry points
	// -------------------------------------------------------------------

	
	@Override
	public int GetTileTrackStatus(AbstractTileIndex ti, TransportType mode)
	{
		return Landscape._tile_type_procs[type].get_tile_track_status_proc.applyAsInt((TileIndex) ti, mode);
	}

	@Override
	public void ChangeTileOwner(AbstractTileIndex ti, int old_player, int new_player)
	{
		Landscape._tile_type_procs[type].change_tile_owner_proc.apply((TileIndex) ti, PlayerID.get( old_player ), PlayerID.get( new_player) );
	}

	@Override
	public AcceptedCargo GetAcceptedCargo(AbstractTileIndex ti)
	{
		return Landscape._tile_type_procs[type].get_accepted_cargo_proc.apply((TileIndex) ti);
	}

	@Override
	public void AnimateTile(AbstractTileIndex ti)
	{
		Landscape._tile_type_procs[type].animate_tile_proc.accept((TileIndex) ti);
	}

	@Override
	public void ClickTile(AbstractTileIndex ti)
	{
		Landscape._tile_type_procs[type].click_tile_proc.accept((TileIndex) ti);
	}

	public void DrawTile(AbstractTileIndex ti, TileInfo tInfo)
	{
		Landscape._tile_type_procs[tInfo.type].draw_tile_proc.accept(tInfo);
	}

	@Override
	public TileDesc GetTileDesc(AbstractTileIndex ti)
	{
		return Landscape._tile_type_procs[type].get_tile_desc_proc.apply((TileIndex) ti);
	}
	

} 

