import game.util.BitOps;

public class Station implements IPoolItem
{

	TileIndex xy;
	RoadStop bus_stops;
	RoadStop truck_stops;
	TileIndex train_tile;
	TileIndex airport_tile;
	TileIndex dock_tile;
	Town town;
	int string_id;

	ViewportSign sign;

	int had_vehicle_of_type;

	byte time_since_load;
	byte time_since_unload;
	byte delete_ctr;
	PlayerID owner;
	byte facilities;
	byte airport_type;

	// trainstation width/height
	byte trainst_w, trainst_h;

	byte class_id; // custom graphics station class
	byte stat_id; // custom graphics station id in the @class_id class
	int build_date;

	//int airport_flags;
	int airport_flags;
	//StationID index;
	int index;
	VehicleQueue airport_queue;			// airport queue
	VehicleQueue helicopter_queue;			// airport queue

	VehicleID last_vehicle;
	GoodsEntry goods[] = new GoodsEntry[Global.NUM_CARGO];

	// Stuff that is no longer used, but needed for conversion 
	//TileIndex bus_tile_obsolete;
	//TileIndex lorry_tile_obsolete;

	//byte truck_stop_status_obsolete;
	//byte bus_stop_status_obsolete;
	//byte blocked_months_obsolete;




    private void StationInitialize(TileIndex tile)
    {
    	// GoodsEntry ge;
    
        xy = tile;
        airport_tile = dock_tile = train_tile = 0;
        bus_stops = truck_stops = null;
        had_vehicle_of_type = 0;
        time_since_load = (byte) 255;
        time_since_unload = (byte) 255;
        delete_ctr = 0;
        facilities = 0;
    
        last_vehicle = INVALID_VEHICLE;
    
        //for (ge = goods; ge != endof(goods); ge++) 
        for(GoodsEntry ge : goods)
        {
            ge.waiting_acceptance = 0;
            ge.days_since_pickup = 0;
            ge.enroute_from = INVALID_STATION;
            ge.rating = (byte) 175;
            ge.last_speed = 0;
            ge.last_age = (byte) 0xFF;
            ge.feeder_profit = 0;
        }
    
        airport_queue = VehicleQueue.new_VQueue();
        helicopter_queue = VehicleQueue.new_VQueue();
    
        _global_station_sort_dirty = true; // build a new station
    }
    
    // Update the virtual coords needed to draw the station sign.
    // st = Station to update for.
    private void UpdateStationVirtCoord()
    {
        Point pt = Point.RemapCoords2(xy.TileX() * 16, xy.TileY() * 16);
    
        pt.y -= 32;
        if (facilities & FACIL_AIRPORT && airport_type == AT_OILRIG) pt.y -= 16;
    
        Global.SetDParam(0, index);
        Global.SetDParam(1, facilities);
        UpdateViewportSignPos(sign, pt.x, pt.y, STR_305C_0);
    }

    
    // Update the virtual coords needed to draw the station sign for all stations.
void UpdateAllStationVirtCoord()
{
	//Station st;

	//FOR_ALL_STATIONS(st) 
	_station_pool.forEach( (i,st) ->
	{
		if (st.xy != null) st.UpdateStationVirtCoord();
	});
}

// Update the station virt coords while making the modified parts dirty.
private void UpdateStationVirtCoordDirty()
{
	MarkStationDirty();
	UpdateStationVirtCoord();
	MarkStationDirty();
}



// Get a mask of the cargo types that the station accepts.
private int GetAcceptanceMask()
{
	int mask = 0;
	int i;

	for (i = 0; i != Global.NUM_CARGO; i++) {
		if (goods[i].waiting_acceptance & 0x8000) mask |= 1 << i;
	}
	return mask;
}

// Items contains the two cargo names that are to be accepted or rejected.
// msg is the string id of the message to display.
private void ShowRejectOrAcceptNews(int items, StringID msg)
{
	if (items > 0) {
		Global.SetDParam(2, BitOps.GB(items, 16, 16));
		Global.SetDParam(1, BitOps.GB(items,  0, 16));
		Global.SetDParam(0, index);
		AddNewsItem(msg + ((items >> 16)?1:0), NEWS_FLAGS(NM_SMALL, NF_VIEWPORT|NF_TILE, NT_ACCEPTANCE, 0), xy, 0);
	}
}


CT_

// Get a list of the cargo types being produced around the tile.
static void GetProductionAroundTiles(AcceptedCargo produced, TileIndex tile0,
	int w, int h, int rad)
{
	int x,y;
	int x1,y1,x2,y2;
	int xc,yc;

	//memset(produced, 0, sizeof(AcceptedCargo));
	produced.clear();

	x = tile0.TileX();
	y = tile0.TileY();

	// expand the region by rad tiles on each side
	// while making sure that we remain inside the board.
	x2 = Integer.min(x + w + rad, Global.MapSizeX());
	x1 = Integer.max(x - rad, 0);

	y2 = Integer.min(y + h + rad, Global.MapSizeY());
	y1 = Integer.max(y - rad, 0);

	assert(x1 < x2);
	assert(y1 < y2);
	assert(w > 0);
	assert(h > 0);

	for (yc = y1; yc != y2; yc++) {
		for (xc = x1; xc != x2; xc++) {
			if (!(IS_INSIDE_1D(xc, x, w) && IS_INSIDE_1D(yc, y, h))) {
				//GetProducedCargoProc gpc;
				TileIndex tile1 = new TileIndex(xc, yc);

				/*
				gpc = _tile_type_procs[GetTileType(tile1)].get_produced_cargo_proc;
				if (gpc != null) {
					byte cargos[2] = { AcceptedCargo.CT_INVALID, AcceptedCargo.CT_INVALID };

					gpc(tile1, cargos);
					*/
				{
					byte cargos[2] = { AcceptedCargo.CT_INVALID, AcceptedCargo.CT_INVALID };
					_tile_type_procs[tile1.GetTileType().ordinal()].get_produced_cargo_proc(tile1, cargos);
					if (cargos[0] != AcceptedCargo.CT_INVALID) {
						produced[cargos[0]]++;
						if (cargos[1] != AcceptedCargo.CT_INVALID) {
							produced[cargos[1]]++;
						}
					}
				}
			}
		}
	}
}

// Get a list of the cargo types that are accepted around the tile.
static void GetAcceptanceAroundTiles(AcceptedCargo accepts, TileIndex tile0,
	int w, int h, int rad)
{
	int x,y;
	int x1,y1,x2,y2;
	int xc,yc;

	//memset(accepts, 0, sizeof(AcceptedCargo));
	accepts.clear();

	x = tile0.TileX();
	y = tile0.TileY();

	// expand the region by rad tiles on each side
	// while making sure that we remain inside the board.
	x2 = Integer.min(x + w + rad, Global.MapSizeX());
	y2 = Integer.min(y + h + rad, Global.MapSizeY());
	x1 = Integer.max(x - rad, 0);
	y1 = Integer.max(y - rad, 0);

	assert(x1 < x2);
	assert(y1 < y2);
	assert(w > 0);
	assert(h > 0);

	for (yc = y1; yc != y2; yc++) {
		for (xc = x1; xc != x2; xc++) {
			TileIndex tile1 = new TileIndex(xc, yc);

			if (!tile1.IsTileType(TileTypes.MP_STATION)) {
				AcceptedCargo ac;
				int i;

				GetAcceptedCargo(tile1, ac);
				for (i = 0; i < lengthof(ac); ++i) accepts[i] += ac[i];
			}
		}
	}
}





// Update the acceptance for a station.
// show_msg controls whether to display a message that acceptance was changed.
private void UpdateStationAcceptance(boolean show_msg)
{
	int old_acc, new_acc;
	RoadStop cur_rs;
	int i;
	ottd_Rectangle rect;
	int rad;
	AcceptedCargo accepts;

	rect.min_x = Global.MapSizeX();
	rect.min_y = Global.MapSizeY();
	rect.max_x = rect.max_y = 0;
	// Don't update acceptance for a buoy
	if (IsBuoy()) return;

	/* old accepted goods types */
	old_acc = GetAcceptanceMask();

	// Put all the tiles that span an area in the table.
	if (train_tile != null) {
		rect.MergePoint(train_tile);
		rect.MergePoint(
			train_tile + TileDiffXY(trainst_w - 1, trainst_h - 1)
		);
	}

	if (airport_tile != null) {
		rect.MergePoint( airport_tile);
		rect.MergePoint(
			airport_tile + TileDiffXY(
				_airport_size_x[airport_type] - 1,
				_airport_size_y[airport_type] - 1
			)
		);
	}

	if (dock_tile != null) rect.MergePoint( dock_tile);

	for (cur_rs = bus_stops; cur_rs != null; cur_rs = cur_rs.next) {
		rect.MergePoint( cur_rs.xy);
	}

	for (cur_rs = truck_stops; cur_rs != null; cur_rs = cur_rs.next) {
		rect.MergePoint( cur_rs.xy);
	}

	rad = (Global._patches.modified_catchment) ? FindCatchmentRadius(st) : 4;

	// And retrieve the acceptance.
	if (rect.max_x >= rect.min_x) {
		GetAcceptanceAroundTiles(
			accepts,
			new TileIndex(rect.min_x, rect.min_y),
			rect.max_x - rect.min_x + 1,
			rect.max_y - rect.min_y + 1,
			rad
		);
	} else {
		//memset(accepts, 0, sizeof(accepts));
		accepts.clear();
	}

	// Adjust in case our station only accepts fewer kinds of goods
	for (i = 0; i != Global.NUM_CARGO; i++) {
		int amt = Integer.min(accepts[i], 15);

		// Make sure the station can accept the goods type.
		if ((i != AcceptedCargo.CT_PASSENGERS && !(facilities & (byte)~FACIL_BUS_STOP)) ||
				(i == AcceptedCargo.CT_PASSENGERS && !(facilities & (byte)~FACIL_TRUCK_STOP)))
			amt = 0;

		goods[i].waiting_acceptance = BitOps.RETSB(goods[i].waiting_acceptance, 12, 4, amt);
	}

	// Only show a message in case the acceptance was actually changed.
	new_acc = GetAcceptanceMask(st);
	if (old_acc == new_acc)
		return;

	// show a message to report that the acceptance was changed?
	if (show_msg && owner == Global._local_player && facilities) {
		int accept=0, reject=0; /* these contain two string ids each */
		final StringID[] str = _cargoc.names_s;

		int si = 0;
		do {
			if (new_acc & 1) {
				if (!(old_acc & 1)) accept = (accept << 16) | str[si];
			} else {
				if (old_acc & 1) reject = (reject << 16) | str[si];
			}
		} while (si++,(new_acc>>=1) != (old_acc>>=1));

		ShowRejectOrAcceptNews(st, accept, STR_3040_NOW_ACCEPTS);
		ShowRejectOrAcceptNews(st, reject, STR_303E_NO_LONGER_ACCEPTS);
	}

	// redraw the station view since acceptance changed
	InvalidateWindowWidget(WC_STATION_VIEW, index, 4);
}

// This is called right after a station was deleted.
// It checks if the whole station is free of substations, and if so, the station will be
// deleted after a little while.
private void DeleteStationIfEmpty()
{
	if (facilities == 0) {
		delete_ctr = 0;
		InvalidateWindow(WC_STATION_LIST, owner);
	}
}

	private IPoolItemFactory<Station> factory = new IPoolItemFactory<Station>() {
		
		@Override
		public Station createObject() {
			// TODO Auto-generated method stub
			return new Station();
		}
	};
	private static MemoryPool<Station> _station_pool = new MemoryPool<Station>(factory);

	@Override
	public void setIndex(int index) {
		this.index = index;
	}


	/**
	 * Get the pointer to the station with index 'index'
	 */
	static Station GetStation(int index)
	{
		return _station_pool.GetItemFromPool(index);
	}

	/**
	 * Get the current size of the StationPool
	 */
	static int GetStationPoolSize()
	{
		return _station_pool.total_items();
	}

	static boolean IsStationIndex(int index)
	{
		return index < GetStationPoolSize();
	}
	
	
	
}




class ottd_Rectangle {
	int min_x;
	int min_y;
	int max_x;
	int max_y;


    void MergePoint(TileIndex tile)
    {
	int x = tile.TileX();
	int y = tile.TileY();

	if (min_x > x) min_x = x;
	if (min_y > y) min_y = y;
	if (max_x < x) max_x = x;
	if (max_y < y) max_y = y;
    }

}

