public class WayPoint
{
	public static final int RAIL_TYPE_WAYPOINT = 0xC4;
	public static final int RAIL_WAYPOINT_TRACK_MASK = 1;

	/* Max waypoints: 64000 (8 * 8000) */
	public static final int WAYPOINT_POOL_BLOCK_SIZE_BITS = 3;       /* In bits, so (1 << 3) == 8 */
	public static final int WAYPOINT_POOL_MAX_BLOCKS      = 8000;

	public static final int MAX_WAYPOINTS_PER_TOWN        = 64;


	TileIndex xy;      ///< Tile of waypoint
	int index;      ///< Index of waypoint

	int town_index; ///< Town associated with the waypoint
	byte town_cn;      ///< The Nth waypoint for this town (consecutive number)
	StringID string;   ///< If this is zero (i.e. no custom name), town + town_cn is used for naming

	ViewportSign sign; ///< Dimensions of sign (not saved)
	int build_date; ///< Date of construction

	byte stat_id;      ///< ID of waypoint within the waypoint class (not saved)
	int grfid;      ///< ID of GRF file
	byte localidx;     ///< Index of station within GRF file

	byte deleted;      ///< Delete counter. If greater than 0 then it is decremented until it reaches 0; the waypoint is then is deleted.




    private static MemoryPool<WayPoint> _waypoint_pool = new MemoryPool();
    /* Initialize the town-pool */
    //MemoryPool _waypoint_pool = { "Waypoints", WAYPOINT_POOL_MAX_BLOCKS, WAYPOINT_POOL_BLOCK_SIZE_BITS, sizeof(Waypoint), &WaypointPoolNewBlock, 0, 0, null };

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
 * Get the pointer to the waypoint with index 'index'
 */
private  Waypoint GetWaypoint(uint index)
{
	return (Waypoint)_waypoint_pool.GetItemFromPool(index);
}

/**
 * Get the current size of the WaypointPool
 */
private static int GetWaypointPoolSize()
{
	return _waypoint_pool.total_items();
}

private  boolean IsWaypointIndex(uint index)
{
	return index < GetWaypointPoolSize();
}

//#define FOR_ALL_WAYPOINTS_FROM(wp, start) for (wp = GetWaypoint(start); wp != null; wp = (wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(wp.index + 1) : null)

//#define FOR_ALL_WAYPOINTS(wp) FOR_ALL_WAYPOINTS_FROM(wp, 0)



private static boolean IsRailWaypoint(TileIndex tile)
{
	return (_m[tile].m5 & 0xFC) == 0xC4;
}

/**
 * Fetch a waypoint by tile
 * @param tile Tile of waypoint
 * @return Waypoint
 */
private static Waypoint GetWaypointByTile(TileIndex tile)
{
	assert(IsTileType(tile, MP_RAILWAY) && IsRailWaypoint(tile));
	return GetWaypoint(_m[tile].m2);
}


// TODO NOT CALLED
/**
 * Called if a new block is added to the waypoint-pool
 * /
private void WaypointPoolNewBlock(uint start_item)
{
	Waypoint wp;

	//FOR_ALL_WAYPOINTS_FROM(wp, start_item)
    for (wp = GetWaypoint(start_item); wp != null; wp = (wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(wp.index + 1) : null)
		wp.index = start_item++;
}*/


/* Create a new waypoint */
private static Waypoint AllocateWaypoint()
{
	//Waypoint wp;

	//for (wp = GetWaypoint(0); wp != null; wp = (wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(wp.index + 1) : null) 
    _waypoint_pool.forEach((i,wp) ->
    {
		if (wp.xy == 0) {
			int index = wp.index;

			//memset(wp, 0, sizeof(Waypoint));
            wp.clear();
			wp.index = index;

			return wp;
		}
	});

	/* Check if we can add a block to the pool */
	if (_waypoint_pool.AddBlockToPool())
		return AllocateWaypoint();

	return null;
}

/* Update the sign for the waypoint */
void UpdateWaypointSign()
{
	Point pt = RemapCoords2(TileX(xy) * 16, TileY(xy) * 16);
	SetDParam(0, index);
	UpdateViewportSignPos(sign, pt.x, pt.y - 0x20, STR_WAYPOINT_VIEWPORT);
}

/* Redraw the sign of a waypoint */
private void RedrawWaypointSign()
{
	MarkAllViewportsDirty(
		sign.left - 6,
		sign.top,
		sign.left + (sign.width_1 << 2) + 12,
		sign.top + 48);
}

/* Update all signs */
static void UpdateAllWaypointSigns()
{
	//Waypoint wp;

	//for (wp = GetWaypoint(0); wp != null; wp = (wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(wp.index + 1) : null) 
    _waypoint_pool.forEach((i,wp) ->
    {
		if (wp.xy)
			UpdateWaypointSign(wp);
	});
}

/* Set the default name for a waypoint */
private void MakeDefaultWaypointName()
{
	//Waypoint local_wp;
	boolean used_waypoint[] = new boolean[MAX_WAYPOINTS_PER_TOWN];
	int i;

	town_index = ClosestTownFromTile(xy, (uint)-1).index;

	memset(used_waypoint, 0, sizeof(used_waypoint));

	/* Find an unused waypoint number belonging to this town */
	//for (local_wp = GetWaypoint(0); local_wp != null; local_wp = (local_wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(local_wp.index + 1) : null) 
    _waypoint_pool.forEach((i,local_wp) ->
    {
		if (this == local_wp)
			continue;

		if (local_wp.xy && local_wp.string == STR_null && local_wp.town_index == town_index)
			used_waypoint[local_wp.town_cn] = true;
	});

	/* Find an empty spot */
	for (i = 0; used_waypoint[i] && i < MAX_WAYPOINTS_PER_TOWN; i++) {}

	string = STR_null;
	town_cn = i;
}

/* Find a deleted waypoint close to a tile. */
private static Waypoint FindDeletedWaypointCloseTo(TileIndex tile)
{
	Waypoint best = null;
	uint thres = 8, cur_dist;

	//for (wp = GetWaypoint(0); wp != null; wp = (wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(wp.index + 1) : null) 
    _waypoint_pool.forEach((i,wp) ->
    {
		if (wp.deleted && wp.xy) {
			cur_dist = DistanceManhattan(tile, wp.xy);
			if (cur_dist < thres) {
				thres = cur_dist;
				best = wp;
			}
		}
	});

	return best;
}

/**
 * Update waypoint graphics id against saved GRFID/localidx.
 * This is to ensure the chosen graphics are correct if GRF files are changed.
 */
static void UpdateAllWaypointCustomGraphics()
{
	Waypoint wp;

	//for (wp = GetWaypoint(0); wp != null; wp = (wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(wp.index + 1) : null) 
    _waypoint_pool.forEach((i,wp) ->
    {
		uint i;

		if (wp.grfid != 0) 
        {
		for (i = 0; i < GetNumCustomStations(STAT_CLASS_WAYP); i++) {
			final StationSpec spec = GetCustomStation(STAT_CLASS_WAYP, i);
			if (spec != null && spec.grfid == wp.grfid && spec.localidx == wp.localidx) {
				wp.stat_id = i;
				break;
			}
		}
        }
	});
}

/** Convert existing rail to waypoint. Eg build a waypoint station over
 * piece of rail
 * @param x,y coordinates where waypoint will be built
 * @param p1 graphics for waypoint type, 0 indicates standard graphics
 * @param p2 unused
 *
 * @todo When checking for the tile slope,
 * distingush between "Flat land required" and "land sloped in wrong direction"
 */
static int CmdBuildTrainWaypoint(int x, int y, int flags, int p1, int p2)
{
	TileIndex tile = TileVirtXY(x, y);
	Waypoint wp;
	uint tileh;
	uint dir;

	SET_EXPENSES_TYPE(EXPENSES_CONSTRUCTION);

	/* if custom gfx are used, make sure it is within bounds */
	if (p1 >= GetNumCustomStations(STAT_CLASS_WAYP)) return CMD_ERROR;

	if (!IsTileType(tile, MP_RAILWAY) || ((dir = 0, _m[tile].m5 != 1) && (dir = 1, _m[tile].m5 != 2)))
		return_cmd_error(STR_1005_NO_SUITABLE_RAILROAD_TRACK);

	if (!CheckTileOwnership(tile))
		return CMD_ERROR;

	if (!EnsureNoVehicle(tile)) return CMD_ERROR;

	tileh = GetTileSlope(tile, null);
	if (tileh != 0) {
		if (!_patches.build_on_slopes || IsSteepTileh(tileh) || !(tileh & (0x3 << dir)) || !(tileh & ~(0x3 << dir)))
			return_cmd_error(STR_0007_FLAT_LAND_REQUIRED);
	}

	/* Check if there is an already existing, deleted, waypoint close to us that we can reuse. */
	wp = FindDeletedWaypointCloseTo(tile);
	if (wp == null) {
		wp = AllocateWaypoint();
		if (wp == null) return CMD_ERROR;

		wp.town_index = 0;
		wp.string = STR_null;
		wp.town_cn = 0;
	}

	if (flags & DC_EXEC) {
		final StationSpec spec = null;
		bool reserved = PBSTileReserved(tile) != 0;
		ModifyTile(tile, MP_MAP2 | MP_MAP5, wp.index, RAIL_TYPE_WAYPOINT | dir);

		if (GB(p1, 0, 8) < GetNumCustomStations(STAT_CLASS_WAYP))
			spec = GetCustomStation(STAT_CLASS_WAYP, GB(p1, 0, 8));

		if (spec != null) {
			SETBIT(_m[tile].m3, 4);
			wp.stat_id = GB(p1, 0, 8);
			wp.grfid = spec.grfid;
			wp.localidx = spec.localidx;
		} else {
			// Specified custom graphics do not exist, so use default.
			CLRBIT(_m[tile].m3, 4);
			wp.stat_id = 0;
			wp.grfid = 0;
			wp.localidx = 0;
		}

		if (reserved) {
			PBSReserveTrack(tile, dir);
		} else {
			PBSClearTrack(tile, dir);
		}

		wp.deleted = 0;
		wp.xy = tile;
		wp.build_date = _date;

		if (wp.town_index == STR_null)
			MakeDefaultWaypointName(wp);

		UpdateWaypointSign(wp);
		RedrawWaypointSign(wp);
	}

	return _price.build_train_depot;
}

/* Internal handler to delete a waypoint */
private void DoDeleteWaypoint()
{
	Order order;

	xy = 0;

	order.type = OT_GOTO_WAYPOINT;
	order.station = wp.index;
	DeleteDestinationFromVehicleOrder(order);

	if (string != STR_null)
		DeleteName(string);

	RedrawWaypointSign();
}

/* Daily loop for waypoints */
static void WaypointsDailyLoop()
{
    _waypoint_pool.forEach((i,wp) -> {
		if (wp.deleted && !--wp.deleted)
			wp.DoDeleteWaypoint();

    });
    /*
    Waypoint wp;

	// Check if we need to delete a waypoint 
	for (wp = GetWaypoint(0); wp != null; wp = (wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(wp.index + 1) : null) 
    {
		if (wp.deleted && !--wp.deleted) {
			wp.DoDeleteWaypoint();
		}
	}
    */
}

/* Remove a waypoint */
static int RemoveTrainWaypoint(TileIndex tile, int flags, bool justremove)
{
	Waypoint wp;

	/* Make sure it's a waypoint */
	if (!IsTileType(tile, MP_RAILWAY) || !IsRailWaypoint(tile))
		return CMD_ERROR;

	if (!CheckTileOwnership(tile) && !(_current_player == OWNER_WATER))
		return CMD_ERROR;

	if (!EnsureNoVehicle(tile))
		return CMD_ERROR;

	if (flags & DC_EXEC) {
		int direction = _m[tile].m5 & RAIL_WAYPOINT_TRACK_MASK;

		wp = GetWaypointByTile(tile);

		wp.deleted = 30; // let it live for this many days before we do the actual deletion.
		RedrawWaypointSign(wp);

		if (justremove) {
			bool reserved = PBSTileReserved(tile) != 0;
			ModifyTile(tile, MP_MAP2_CLEAR | MP_MAP5, 1<<direction);
			CLRBIT(_m[tile].m3, 4);
			_m[tile].m4 = 0;
			if (reserved) {
				PBSReserveTrack(tile, direction);
			} else {
				PBSClearTrack(tile, direction);
			}
		} else {
			DoClearSquare(tile);
			SetSignalsOnBothDir(tile, direction);
		}
	}

	return _price.remove_train_depot;
}

/** Delete a waypoint
 * @param x,y coordinates where waypoint is to be deleted
 * @param p1 unused
 * @param p2 unused
 */
static int CmdRemoveTrainWaypoint(int x, int y, int flags, int p1, int p2)
{
	TileIndex tile = TileVirtXY(x, y);
	SET_EXPENSES_TYPE(EXPENSES_CONSTRUCTION);
	return RemoveTrainWaypoint(tile, flags, true);
}

/** Rename a waypoint.
 * @param x,y unused
 * @param p1 id of waypoint
 * @param p2 unused
 */
static int CmdRenameWaypoint(int x, int y, int flags, int p1, int p2)
{
	Waypoint wp;
	StringID str;

	if (!IsWaypointIndex(p1)) return CMD_ERROR;

	if (_cmd_text[0] != '\0') {
		str = AllocateNameUnique(_cmd_text, 0);
		if (str == 0)
			return CMD_ERROR;

		if (flags & DC_EXEC) {
			wp = GetWaypoint(p1);
			if (wp.string != STR_null)
				DeleteName(wp.string);

			wp.string = str;
			wp.town_cn = 0;

			UpdateWaypointSign(wp);
			MarkWholeScreenDirty();
		} else {
			DeleteName(str);
		}
	} else {
		if (flags & DC_EXEC) {
			wp = GetWaypoint(p1);
			if (wp.string != STR_null)
				DeleteName(wp.string);

			MakeDefaultWaypointName(wp);
			UpdateWaypointSign(wp);
			MarkWholeScreenDirty();
		}
	}
	return 0;
}

/* This hacks together some dummy one-shot Station structure for a waypoint. */
Station ComposeWaypointStation(TileIndex tile)
{
	Waypoint wp = GetWaypointByTile(tile);
	static Station stat;

	stat.train_tile = stat.xy = wp.xy;
	stat.town = GetTown(wp.town_index);
	stat.string_id = wp.string == STR_null ? /* FIXME? */ 0 : wp.string;
	stat.build_date = wp.build_date;
	stat.class_id = 6;
	stat.stat_id = wp.stat_id;

	return stat;
}

//extern uint16 _custom_sprites_base;


/* Draw a waypoint */
static void DrawWaypointSprite(int x, int y, int stat_id, RailType railtype)
{
	final StationSpec stat;
	int relocation;
	final DrawTileSprites cust;
	final DrawTileSeqStruct seq;
	final RailtypeInfo rti = GetRailTypeInfo(railtype);
	int ormod, img;

	ormod = SPRITE_PALETTE(PLAYER_SPRITE_COLOR(_local_player));

	x += 33;
	y += 17;

	stat = GetCustomStation(STAT_CLASS_WAYP, stat_id);
	if (stat == null) {
		// stat is null for default waypoints and when waypoint graphics are
		// not loaded.
		DrawDefaultWaypointSprite(x, y, railtype);
		return;
	}

	relocation = GetCustomStationRelocation(stat, null, 1);
	// emulate station tile - open with building
	// add 1 to get the other direction
	cust = &stat.renderdata[2];

	img = cust.ground_sprite;
	img += (img < _custom_sprites_base) ? rti.total_offset : railtype;

	if (img & PALETTE_MODIFIER_COLOR) img = (img & SPRITE_MASK);
	DrawSprite(img, x, y);

	foreach_draw_tile_seq(seq, cust.seq) {
		Point pt = RemapCoords(seq.delta_x, seq.delta_y, seq.delta_z);
		int image = seq.image + relocation;
		DrawSprite((image & SPRITE_MASK) | ormod, x + pt.x, y + pt.y);
	}
}

/* Fix savegames which stored waypoints in their old format * /
void FixOldWaypoints()
{
	Waypoint *wp;

	// Convert the old 'town_or_string', to 'string' / 'town' / 'town_cn' * /
	for (wp = GetWaypoint(0); wp != null; wp = (wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(wp.index + 1) : null) 
    {
		if (wp.xy == 0)
			continue;

		wp.town_index = ClosestTownFromTile(wp.xy, (uint)-1).index;
		wp.town_cn = 0;
		if (wp.string & 0xC000) {
			wp.town_cn = wp.string & 0x3F;
			wp.string = STR_null;
		}
	}
}
*/

static void InitializeWaypoints()
{
	_waypoint_pool.CleanPool();
	_waypoint_pool.AddBlockToPool();
}
/*
static final SaveLoad _waypoint_desc[] = {
	SLE_CONDVAR(Waypoint, xy, SLE_FILE_U16 | SLE_VAR_U32, 0, 5),
	SLE_CONDVAR(Waypoint, xy, SLE_int, 6, 255),
	SLE_CONDVAR(Waypoint, town_index, SLE_UINT16, 12, 255),
	SLE_CONDVAR(Waypoint, town_cn, SLE_UINT8, 12, 255),
	SLE_VAR(Waypoint, string, SLE_UINT16),
	SLE_VAR(Waypoint, deleted, SLE_UINT8),

	SLE_CONDVAR(Waypoint, build_date, SLE_UINT16,  3, 255),
	SLE_CONDVAR(Waypoint, localidx,   SLE_UINT8,   3, 255),
	SLE_CONDVAR(Waypoint, grfid,      SLE_int, 17, 255),

	SLE_END()
};
*/
private static void Save_WAYP()
{
	Waypoint wp;

	for (wp = GetWaypoint(0); wp != null; wp = (wp.index + 1 < GetWaypointPoolSize()) ? GetWaypoint(wp.index + 1) : null) 
    {
		if (wp.xy != 0) {
			SlSetArrayIndex(wp.index);
			SlObject(wp, _waypoint_desc);
		}
	}
}

private static void Load_WAYP()
{
	int index;

	while ((index = SlIterateArray()) != -1) {
		Waypoint wp;

		if (!AddBlockIfNeeded(&_waypoint_pool, index))
			error("Waypoints: failed loading savegame: too many waypoints");

		wp = GetWaypoint(index);
		SlObject(wp, _waypoint_desc);
	}
}

final ChunkHandler _waypoint_chunk_handlers[] = {
	{ 'CHKP', Save_WAYP, Load_WAYP, CH_ARRAY | CH_LAST},
};


}