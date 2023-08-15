package com.dzavalishin.game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.function.Consumer;

import com.dzavalishin.ids.PlayerID;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.enums.StationClassID;
import com.dzavalishin.enums.TileTypes;
import com.dzavalishin.ifaces.IPoolItem;
import com.dzavalishin.ifaces.IPoolItemFactory;
import com.dzavalishin.struct.DrawTileSeqStruct;
import com.dzavalishin.struct.DrawTileSprites;
import com.dzavalishin.struct.Point;
import com.dzavalishin.util.BitOps;
import com.dzavalishin.xui.Gfx;
import com.dzavalishin.xui.ViewPort;

public class WayPoint implements IPoolItem
{
	private static final long serialVersionUID = 1L;
	
	public static final int RAIL_TYPE_WAYPOINT = 0xC4;
	public static final int RAIL_WAYPOINT_TRACK_MASK = 1;

	public static final int WAYPOINT_POOL_BLOCK_SIZE_BITS = 3;       /* In bits, so (1 << 3) == 8 */
	public static final int WAYPOINT_POOL_MAX_BLOCKS      = 8000;

	public static final int MAX_WAYPOINTS_PER_TOWN        = 64;


	public TileIndex xy;      ///< Tile of WayPoint
	public int index;      ///< Index of WayPoint

	public int town_index; ///< Town associated with the WayPoint
	public int town_cn;      ///< The Nth WayPoint for this town (consecutive number)
	public StringID string;   ///< If this is zero (i.e. no custom name), town + town_cn is used for naming

	public ViewportSign sign; ///< Dimensions of sign (not saved)
	public int build_date; ///< Date of construction

	public int stat_id;      ///< ID of WayPoint within the WayPoint class (not saved)
	public int grfid;      ///< ID of GRF file
	public int localidx;     ///< Index of station within GRF file

	public int deleted;      ///< Delete counter. If greater than 0 then it is decremented until it reaches 0; the WayPoint is then is deleted.


	public boolean isValid()
	{
		return xy != null;
	}

	static final IPoolItemFactory<WayPoint> factory = new IPoolItemFactory<WayPoint>()
	{		
		private static final long serialVersionUID = 1L;

		@Override
		public WayPoint createObject() {
			return new WayPoint();
		}
	};

	public static Iterator<WayPoint> getIterator()
	{
		return Global.gs._waypoints.getIterator(); // pool.values().iterator();
	}

	public static void forEach( Consumer<WayPoint> c )
	{
		Global.gs._waypoints.forEach(c);
	}
	
	
	private void clear()
	{
		xy = null;
		town_index = 0;
		town_cn = 0;
		string = null;
		sign = null;
		build_date = 0;
		stat_id = 0;
		grfid = 0;
		localidx = 0;
		deleted = 0;
	}

	public WayPoint()
	{
		clear();
	}


	/**
	 * Get the pointer to the WayPoint with index 'index'
	 */
	public static WayPoint GetWaypoint(int index)
	{
		return Global.gs._waypoints.GetItemFromPool(index);
	}

	/**
	 * Get the current size of the WaypointPool
	 */
	private static int GetWaypointPoolSize()
	{
		return Global.gs._waypoints.total_items();
	}

	public static boolean IsWaypointIndex(int index)
	{
		return (index > 0) && (index < GetWaypointPoolSize());
	}

	//#define FOR_ALL_WAYPOINTS_FROM(wp, start) for (wp = GetWaypoint(start); wp != null; wp = (wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(wp.index + 1) : null)

	//#define FOR_ALL_WAYPOINTS(wp) FOR_ALL_WAYPOINTS_FROM(wp, 0)



	public static boolean IsRailWaypoint(TileIndex tile)
	{
		return (tile.getMap().m5 & 0xFC) == 0xC4;
	}

	/**
	 * Fetch a WayPoint by tile
	 * @param tile Tile of WayPoint
	 * @return WayPoint
	 */
	public static WayPoint GetWaypointByTile(TileIndex tile)
	{
		assert(tile.IsTileType( TileTypes.MP_RAILWAY) && IsRailWaypoint(tile));
		return GetWaypoint(tile.getMap().m2);
	}


	@Override
	public void setIndex(int index) {
		this.index = index;	
	}

	/* Create a new WayPoint */
	private static WayPoint AllocateWaypoint()
	{
		WayPoint [] ret = {null};

		//for (wp = GetWaypoint(0); wp != null; wp = (wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(wp.index + 1) : null) 
		Global.gs._waypoints.forEach( wp ->
		{
			if (!wp.isValid()) {
				int index = wp.index;

				wp.clear();
				wp.index = index;

				ret[0] = wp;
			}
		});

		if( ret[0] != null ) return ret[0];
		
		/* Check if we can add a block to the pool */
		if (Global.gs._waypoints.AddBlockToPool())
			return AllocateWaypoint();

		return null;
	}

	/* Update the sign for the WayPoint */
	void UpdateWaypointSign()
	{
		Point pt = Point.RemapCoords2(xy.TileX() * 16, xy.TileY() * 16);
		Global.SetDParam(0, index);
		ViewPort.UpdateViewportSignPos(sign, pt.x, pt.y - 0x20, Str.STR_WAYPOINT_VIEWPORT);
	}

	/* Redraw the sign of a WayPoint */
	private void RedrawWaypointSign()
	{
		ViewPort.MarkAllViewportsDirty(
				sign.getLeft() - 6,
				sign.getTop(),
				sign.getLeft() + (sign.getWidth_1() << 2) + 12,
				sign.getTop() + 48);
	}

	/* Update all signs */
	static void UpdateAllWaypointSigns()
	{
		Global.gs._waypoints.forEachValid( wp -> wp.UpdateWaypointSign() );
	}

	/* Set the default name for a WayPoint */
	private void MakeDefaultWaypointName()
	{
		boolean used_waypoint[] = new boolean[MAX_WAYPOINTS_PER_TOWN];
		int i;

		town_index = Town.ClosestTownFromTile(xy, -1).index;

		//memset(used_waypoint, 0, sizeof(used_waypoint));
		//used_waypoint.clear();
		/* Find an unused WayPoint number belonging to this town */
		//for (local_wp = GetWaypoint(0); local_wp != null; local_wp = (local_wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(local_wp.index + 1) : null) 
		//Global.gs._waypoints.forEach((ii,local_wp) ->
		Global.gs._waypoints.forEach( local_wp ->
		{
			if (this == local_wp)
			{
				//continue;
				return;
			}
			
			if (local_wp.isValid() && local_wp.string.id == Str.STR_NULL && local_wp.town_index == town_index)
				used_waypoint[local_wp.town_cn] = true;
		});

		/* Find an empty spot */
		for (i = 0; used_waypoint[i] && i < MAX_WAYPOINTS_PER_TOWN; i++) 
			;

		string = new StringID( Str.STR_NULL );
		town_cn =  i;
	}

	/* Find a deleted WayPoint close to a tile. */
	private static WayPoint FindDeletedWaypointCloseTo(TileIndex tile)
	{
		WayPoint best = null;
		int thres = 8, cur_dist;

		Iterator<WayPoint> ii = getIterator();
		while(ii.hasNext())
		{
			WayPoint wp = ii.next();
			if ( (0 != wp.deleted) && wp.isValid() ) {
				cur_dist = Map.DistanceManhattan(tile, wp.xy);
				if (cur_dist < thres) {
					thres = cur_dist;
					best = wp;
				}
			}
		}

		return best;
	}

	/**
	 * Update WayPoint graphics id against saved GRFID/localidx.
	 * This is to ensure the chosen graphics are correct if GRF files are changed.
	 */
	static void UpdateAllWaypointCustomGraphics()
	{
		//for (wp = GetWaypoint(0); wp != null; wp = (wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(wp.index + 1) : null)

		WayPoint.forEach( wp ->
		{
			int i;

			if (wp.grfid != 0) 
			{
				for (i = 0; i < StationClass.GetNumCustomStations(StationClassID.STAT_CLASS_WAYP); i++) {
					final StationSpec spec = StationClass.GetCustomStation(StationClassID.STAT_CLASS_WAYP, i);
					if (spec != null && spec.grfid == wp.grfid && spec.localidx == wp.localidx) {
						wp.stat_id =  i;
						break;
					}
				}
			}
		});
		/* */
	}

	/** Convert existing rail to WayPoint. Eg build a WayPoint station over
	 * piece of rail
	 * @param x,y coordinates where WayPoint will be built
	 * @param p1 graphics for WayPoint type, 0 indicates standard graphics
	 * @param p2 unused
	 *
	 * TODO When checking for the tile slope,
	 * distingush between "Flat land required" and "land sloped in wrong direction"
	 */
	static int CmdBuildTrainWaypoint(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		WayPoint wp;
		int tileh;
		int dir;

		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);

		/* if custom gfx are used, make sure it is within bounds */
		if (p1 >= StationClass.GetNumCustomStations(StationClassID.STAT_CLASS_WAYP)) return Cmd.CMD_ERROR;

		//if (!tile.IsTileType(TileTypes.MP_RAILWAY) || ((dir = 0, tile.getMap().m5 != 1) && (dir = 1, tile.getMap().m5 != 2)))
		//	return Cmd.return_cmd_error(Str.STR_1005_NO_SUITABLE_RAILROAD_TRACK);

		if (!tile.IsTileType(TileTypes.MP_RAILWAY))
			return Cmd.return_cmd_error(Str.STR_1005_NO_SUITABLE_RAILROAD_TRACK);

		if(tile.getMap().m5 == 1)
			dir = 0;
		else if(tile.getMap().m5 == 2)
			dir = 1;
		else
			return Cmd.return_cmd_error(Str.STR_1005_NO_SUITABLE_RAILROAD_TRACK);
		
		
		if (!tile.CheckTileOwnership())
			return Cmd.CMD_ERROR;

		if (!tile.EnsureNoVehicle()) return Cmd.CMD_ERROR;

		tileh = tile.GetTileSlope(null);
		if (tileh != 0) {
			if (!Global._patches.build_on_slopes ||  TileIndex.IsSteepTileh(tileh) || 0==(tileh & (0x3 << dir)) || 0==(tileh & ~(0x3 << dir)))
				return Cmd.return_cmd_error(Str.STR_0007_FLAT_LAND_REQUIRED);
		}

		/* Check if there is an already existing, deleted, WayPoint close to us that we can reuse. */
		wp = FindDeletedWaypointCloseTo(tile);
		if (wp == null) {
			wp = AllocateWaypoint();
			if (wp == null) return Cmd.CMD_ERROR;

			wp.town_index = 0;
			wp.string = new StringID(Str.STR_NULL);
			wp.town_cn = 0;
		}

		if(0 != (flags & Cmd.DC_EXEC)) {
			StationSpec spec = null;
			boolean reserved = Pbs.PBSTileReserved(tile) != 0;
			Landscape.ModifyTile(tile, TileTypes.MP_NOCHANGE,
					TileTypes.MP_MAP2 | TileTypes.MP_MAP5, wp.index, RAIL_TYPE_WAYPOINT | dir);

			if (BitOps.GB(p1, 0, 8) < StationClass.GetNumCustomStations(StationClassID.STAT_CLASS_WAYP))
				spec = StationClass.GetCustomStation(StationClassID.STAT_CLASS_WAYP, BitOps.GB(p1, 0, 8));

			if (spec != null) {
				//SETBIT(Global._m[tile.getTile()].m3, 4);
				tile.setBit_m3(4);
				wp.stat_id =  BitOps.GB(p1, 0, 8);
				wp.grfid = spec.grfid;
				wp.localidx =  spec.localidx;
			} else {
				// Specified custom graphics do not exist, so use default.
				//CLRBIT(Global._m[tile.getTile()].m3, 4);
				tile.clrBit_m3(4);
				wp.stat_id = 0;
				wp.grfid = 0;
				wp.localidx = 0;
			}

			if (reserved) {
				Pbs.PBSReserveTrack(tile, dir);
			} else {
				Pbs.PBSClearTrack(tile, dir);
			}

			wp.deleted = 0;
			wp.xy = tile;
			wp.build_date = Global.get_date();

			if (wp.town_index == Str.STR_NULL)
				wp.MakeDefaultWaypointName();

			wp.UpdateWaypointSign();
			wp.RedrawWaypointSign();
		}

		return (int) Global._price.build_train_depot;
	}

	/* Internal handler to delete a WayPoint */
	private void DoDeleteWaypoint()
	{
		Order order = new Order();

		xy = null;

		order.type = Order.OT_GOTO_WAYPOINT;
		order.station = index;
		Order.DeleteDestinationFromVehicleOrder(order);

		if (string.id != Str.STR_NULL)
			Global.DeleteName(string);

		RedrawWaypointSign();
	}

	/* Daily loop for waypoints */
	public static void WaypointsDailyLoop()
	{
		//Global.gs._waypoints.forEach((i,wp) -> {
		Global.gs._waypoints.forEach( wp -> {
			if( (0 != wp.deleted) && (0 == --wp.deleted) )
				wp.DoDeleteWaypoint();

		});
		/*
    WayPoint wp;

	// Check if we need to delete a WayPoint 
	for (wp = GetWaypoint(0); wp != null; wp = (wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(wp.index + 1) : null) 
    {
		if (wp.deleted && !--wp.deleted) {
			wp.DoDeleteWaypoint();
		}
	}
		 */
	}

	/* Remove a WayPoint */
	static int RemoveTrainWaypoint(TileIndex tile, int flags, boolean justremove)
	{
		WayPoint wp;

		/* Make sure it's a WayPoint */
		if (!tile.IsTileType(TileTypes.MP_RAILWAY) || !IsRailWaypoint(tile))
			return Cmd.CMD_ERROR;

		if (!tile.CheckTileOwnership() && !(PlayerID.getCurrent().isWater()))
			return Cmd.CMD_ERROR;

		if (!tile.EnsureNoVehicle())
			return Cmd.CMD_ERROR;

		if(0 != (flags & Cmd.DC_EXEC)) {
			int direction = tile.getMap().m5 & RAIL_WAYPOINT_TRACK_MASK;

			wp = GetWaypointByTile(tile);

			wp.deleted = 30; // let it live for this many days before we do the actual deletion.
			wp.RedrawWaypointSign();

			if (justremove) {
				boolean reserved = Pbs.PBSTileReserved(tile) != 0;
				Landscape.ModifyTile(tile, TileTypes.MP_NOCHANGE, TileTypes.MP_MAP2_CLEAR | TileTypes.MP_MAP5, 1<<direction);
				//CLRBIT(_m[tile].m3, 4);
				tile.clrBit_m3(4);
				tile.getMap().m4 = 0;
				if (reserved) {
					Pbs.PBSReserveTrack(tile, direction);
				} else {
					Pbs.PBSClearTrack(tile, direction);
				}
			} else {
				Landscape.DoClearSquare(tile);
				Rail.SetSignalsOnBothDir(tile, direction);
			}
		}

		return (int) Global._price.remove_train_depot;
	}

	/** Delete a WayPoint
	 * @param x,y coordinates where WayPoint is to be deleted
	 * @param p1 unused
	 * @param p2 unused
	 */
	static int CmdRemoveTrainWaypoint(int x, int y, int flags, int p1, int p2)
	{
		TileIndex tile = TileIndex.TileVirtXY(x, y);
		Player.SET_EXPENSES_TYPE(Player.EXPENSES_CONSTRUCTION);
		return RemoveTrainWaypoint(tile, flags, true);
	}

	/** Rename a WayPoint.
	 * @param x,y unused
	 * @param p1 id of WayPoint
	 * @param p2 unused
	 */
	static int CmdRenameWaypoint(int x, int y, int flags, int p1, int p2)
	{
		WayPoint wp;
		StringID str;

		if (!IsWaypointIndex(p1)) return Cmd.CMD_ERROR;

		if (Global._cmd_text != null) {
			str = Global.AllocateNameUnique(Global._cmd_text, 0);
			if (str == null)
				return Cmd.CMD_ERROR;

			if(0 != (flags & Cmd.DC_EXEC)) {
				wp = GetWaypoint(p1);
				if (wp.string.id != Str.STR_NULL)
					Global.DeleteName(wp.string);

				wp.string = str;
				wp.town_cn = 0;

				wp.UpdateWaypointSign();
				Hal.MarkWholeScreenDirty();
			} else {
				Global.DeleteName(str);
			}
		} else {
			if(0 != (flags & Cmd.DC_EXEC)) {
				wp = GetWaypoint(p1);
				if (wp.string.id != Str.STR_NULL)
					Global.DeleteName(wp.string);

				wp.MakeDefaultWaypointName();
				wp.UpdateWaypointSign();
				Hal.MarkWholeScreenDirty();
			}
		}
		return 0;
	}

	/* This hacks together some dummy one-shot Station structure for a WayPoint. */
	static Station ComposeWaypointStation(TileIndex tile)
	{
		WayPoint wp = WayPoint.GetWaypointByTile(tile);
		Station stat = Station.AllocateStation();//new Station();

		stat.train_tile = stat.xy = wp.xy;
		stat.town = Town.GetTown(wp.town_index);
		stat.string_id = wp.string.id == Str.STR_NULL ? /* FIXME? */ 0 : wp.string.id;
		stat.build_date = wp.build_date;
		stat.class_id = 6;
		stat.stat_id = wp.stat_id;

		return stat;
	}


	/* Draw a WayPoint */
	public static void DrawWaypointSprite(int x, int y, int stat_id, /* RailType */ int railtype)
	{
		//final StationSpec stat;
		//int relocation;
		//final DrawTileSprites cust;
		//final DrawTileSeqStruct seq;
		final RailtypeInfo rti = Rail.GetRailTypeInfo(railtype);
		int ormod, img;

		ormod = Sprite.SPRITE_PALETTE(Sprite.PLAYER_SPRITE_COLOR(Global.gs._local_player));

		x += 33;
		y += 17;

		StationSpec stat = StationClass.GetCustomStation(StationClassID.STAT_CLASS_WAYP, stat_id);
		if (stat == null) {
			// stat is null for default waypoints and when WayPoint graphics are not loaded.
			Rail.DrawDefaultWaypointSprite(x, y, railtype);
			return;
		}

		int relocation = Station.GetCustomStationRelocation(stat, null, 1);
		// emulate station tile - open with building
		// add 1 to get the other direction
		DrawTileSprites cust = stat.renderdata[2];

		img = cust.ground_sprite;
		img += (img < GRFFile._custom_sprites_base) ? rti.total_offset.id : railtype;

		if(0 != (img & Sprite.PALETTE_MODIFIER_COLOR)) img = (img & Sprite.SPRITE_MASK);
		Gfx.DrawSprite(img, x, y);

		//foreach_draw_tile_seq(seq, cust.seq) 
		//for (seq = cust.seq; ( seq->delta_x) != 0x80; seq++)
		for( DrawTileSeqStruct seq : cust.seq )
		{
			if( ( seq.delta_x) == 0x80 )
				break;
			
			Point pt = Point.RemapCoords(seq.delta_x, seq.delta_y, seq.delta_z);
			int image = seq.image + relocation;
			Gfx.DrawSprite((image & Sprite.SPRITE_MASK) | ormod, x + pt.x, y + pt.y);
		}
		
	}
	
	

	static void InitializeWaypoints()
	{
		Global.gs._waypoints.CleanPool();
		Global.gs._waypoints.AddBlockToPool();
	}

	/*
	private static void Load_WAYP()
	{
		int index;

		while ((index = SlIterateArray()) != -1) {
			WayPoint wp;

			if (!AddBlockIfNeeded(&_waypoint_pool, index))
				error("Waypoints: failed loading savegame: too many waypoints");

			wp = GetWaypoint(index);
			SlObject(wp, _waypoint_desc);
		}
	}

	Chunk Handler chandler = new ChunkHandler( "CHKP", ChunkHandler.CH_ARRAY | ChunkHandler.CH_LAST ) {
		
		@Override
		void save_proc() {
			WayPoint wp;

			for (wp = GetWaypoint(0); wp != null; wp = (wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(wp.index + 1) : null) 
			{
				if (wp.isValid()) {
					SlSetArrayIndex(wp.index);
					SlObject(wp, _waypoint_desc);
				}
			}
		}
		
		@Override
		void load_proc() {

			
		}
	};
	
	final Chunk Handler _waypoint_chunk_handlers[] = {
			chandler
	};
	*/

	public static void loadGame(ObjectInputStream oin) throws ClassNotFoundException, IOException
	{
		//_waypoint_pool = (MemoryPool<WayPoint>) oin.readObject();
	}

	public static void saveGame(ObjectOutputStream oos) throws IOException 
	{
		//oos.writeObject(_waypoint_pool);		
	}

	public ViewportSign getSign() { return sign;	}
	
	

}