package game;

import java.io.Serializable;

import game.enums.TransportType;
import game.struct.TileDesc;
import game.util.BitOps;

/**
 * Implements clear tile - one that has nothing built on.
 * 
 * 
 * @author dz
 *
 */

public class ClearTile extends AbstractTile implements Serializable 
{
	// TODO Incomplete class

	// BitOps.GB(m5, 2, 3)
	
	/*
	 * Str.STR_080B_ROUGH_LAND, // Grass
		Str.STR_080A_ROCKS,
		Str.STR_080E_FIELDS,
		Str.STR_080F_SNOW_COVERED_LAND,
		Str.STR_0810_DESERT,
		0,
		0,
		Str.STR_080C_BARE_LAND,
		
	 */
	
	int landType; 
	
	// if (landType == 0)
	// BitOps.GB(m5, 0, 2)
	int grassType; 
	
	// BitOps.GB(m4, 5, 3)
	int fenceType1 = 0;
	// BitOps.GB(m4, 2, 3)
	int fenceType2 = 0;
	
	// for landType == 3 == snow land
	// m3
	//? int snowState = 0;
	
	//Clear::GetSlopeZ_Clear,					/* get_slope_z_proc */
	//Clear::ClearTile_Clear,					/* clear_tile_proc */
	//Clear::GetTileTrackStatus_Clear,	/* get_tile_track_status_proc */
	//Clear::TileLoop_Clear,						/* tile_loop_clear */
	//Clear::ChangeTileOwner_Clear,		/* change_tile_owner_clear */
	//null,											/* get_produced_cargo_proc */
	//null,											/* vehicle_enter_tile_proc */
	//null,											/* vehicle_leave_tile_proc */
	//Clear::GetSlopeTileh_Clear			/* get_slope_tileh_proc */
	
	
	@Override
	public boolean isClear() {
		return true;
	}

	@Override
	public int GetTileTrackStatus(AbstractTileIndex ti, TransportType mode) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void ChangeTileOwner(AbstractTileIndex ti, int old_player, int new_player) {
		// TODO Auto-generated method stub
		
	}

	/** Unused */
	@Override
	public AcceptedCargo GetAcceptedCargo(AbstractTileIndex ti) {
		return new AcceptedCargo();
	}

	@SuppressWarnings("EmptyMethod")
	@Override
	public void AnimateTile(AbstractTileIndex ti) {
		// empty		
	}

	@Override
	public void ClickTile(AbstractTileIndex ti) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void DrawTile(AbstractTileIndex ti, TileInfo tInfo) {
		Clear.DrawTile_Clear(tInfo);		
	}

	@Override
	public TileDesc GetTileDesc(AbstractTileIndex ti) {
		return 	Clear.GetTileDesc_Clear((TileIndex) ti); // TODO rewrite me
	}
}
