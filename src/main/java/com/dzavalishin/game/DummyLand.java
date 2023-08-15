package com.dzavalishin.game;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.enums.Owner;
import com.dzavalishin.enums.TransportType;
import com.dzavalishin.ifaces.TileTypeProcs;
import com.dzavalishin.struct.TileDesc;
import com.dzavalishin.xui.ViewPort;

public class DummyLand {



	static void DrawTile_Dummy(TileInfo ti)
	{
		ViewPort.DrawGroundSpriteAt(Sprite.SPR_SHADOW_CELL, ti.x, ti.y, ti.z);
	}


	static int GetSlopeZ_Dummy(final TileInfo  ti)
	{
		return Landscape.GetPartialZ(ti.x & 0xF, ti.y & 0xF, ti.tileh) + ti.z;
	}

	static int GetSlopeTileh_Dummy(final TileInfo  ti)
	{
		return ti.tileh;
	}

	static int ClearTile_Dummy(TileIndex tile, byte flags)
	{
		return Cmd.return_cmd_error(Str.STR_0001_OFF_EDGE_OF_MAP);
	}


	static AcceptedCargo GetAcceptedCargo_Dummy(TileIndex tile)
	{
		/* not used */
        return new AcceptedCargo();
	}

	static TileDesc GetTileDesc_Dummy(TileIndex tile )
	{
		TileDesc td = new TileDesc();
		td.str = Str.STR_EMPTY;
		td.owner = Owner.OWNER_NONE;
		return td;
	}

	static void AnimateTile_Dummy(TileIndex tile)
	{
		/* not used */
	}

	static void TileLoop_Dummy(TileIndex tile)
	{
		/* not used */
	}

	static void ClickTile_Dummy(TileIndex tile)
	{
		/* not used */
	}

	static void ChangeTileOwner_Dummy(TileIndex tile, PlayerID old_player, PlayerID new_player)
	{
		/* not used */
	}

	static int GetTileTrackStatus_Dummy(TileIndex tile, /*int*/ TransportType mode)
	{
		return 0;
	}

	final static TileTypeProcs _tile_type_dummy_procs = new TileTypeProcs(
		DummyLand::DrawTile_Dummy,						/* draw_tile_proc */
		DummyLand::GetSlopeZ_Dummy,					/* get_slope_z_proc */
		DummyLand::ClearTile_Dummy,					/* clear_tile_proc */
		DummyLand::GetAcceptedCargo_Dummy,		/* get_accepted_cargo_proc */
		DummyLand::GetTileDesc_Dummy,				/* get_tile_desc_proc */
		DummyLand::GetTileTrackStatus_Dummy,	/* get_tile_track_status_proc */
		DummyLand::ClickTile_Dummy,					/* click_tile_proc */
		DummyLand::AnimateTile_Dummy,				/* animate_tile_proc */
		DummyLand::TileLoop_Dummy,						/* tile_loop_clear */
		DummyLand::ChangeTileOwner_Dummy,		/* change_tile_owner_clear */
		null,											/* get_produced_cargo_proc */
		null,											/* vehicle_enter_tile_proc */
		null,											/* vehicle_leave_tile_proc */
		DummyLand::GetSlopeTileh_Dummy			/* get_slope_tileh_proc */
	);

}
